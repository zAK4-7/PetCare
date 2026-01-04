import express from 'express';
import { z } from 'zod';
import { prisma } from '../prisma.js';
import { authRequired } from '../middleware/auth.js';

const router = express.Router();

// Toutes les routes /pets protégées
router.use(authRequired);

const PetCreateSchema = z.object({
  name: z.string().min(1),
  species: z.string().min(1),
  breed: z.string().optional().nullable(),
  sex: z.string().optional().nullable(),
  birthDate: z.string().datetime().optional().nullable(),
  photoUrl: z.string().url().optional().nullable(),
  notes: z.string().optional().nullable()
});

const PetUpdateSchema = PetCreateSchema.partial().extend({
  name: z.string().min(1).optional(),
  species: z.string().min(1).optional()
});

// GET /pets - liste des pets de l'utilisateur connecté
router.get('/', async (req, res) => {
  try {
    const userId = Number(req.user.sub);

    const pets = await prisma.pet.findMany({
      where: { userId },          // ✅ FIX: userId (pas ownerId)
      orderBy: { id: 'desc' }
    });

    res.json(pets);
  } catch (e) {
    res.status(500).json({ message: 'Server error', error: String(e?.message || e) });
  }
});

// GET /pets/:id - détail d'un pet (appartient à l'utilisateur)
router.get('/:id', async (req, res) => {
  try {
    const userId = Number(req.user.sub);
    const id = Number(req.params.id);

    const pet = await prisma.pet.findFirst({
      where: { id, userId },      // ✅ FIX
      include: {
        healthEvents: { orderBy: { id: 'desc' } },
        weights: { orderBy: { id: 'desc' } },
        feedings: { orderBy: { id: 'desc' } }
      }
    });

    if (!pet) return res.status(404).json({ message: 'Pet not found' });
    res.json(pet);
  } catch (e) {
    res.status(500).json({ message: 'Server error', error: String(e?.message || e) });
  }
});

// POST /pets - créer un pet
router.post('/', async (req, res) => {
  try {
    const userId = Number(req.user.sub);

    const parsed = PetCreateSchema.safeParse(req.body);
    if (!parsed.success) {
      return res.status(400).json({ message: 'Invalid body', errors: parsed.error.flatten() });
    }

    const { name, species, breed, sex, birthDate, photoUrl, notes } = parsed.data;

    const pet = await prisma.pet.create({
      data: {
        name,
        species,
        breed: breed ?? null,
        sex: sex ?? null,
        birthDate: birthDate ? new Date(birthDate) : null,
        photoUrl: photoUrl ?? null,
        notes: notes ?? null,

        // ✅ relation: on connecte le owner (User)
        owner: { connect: { id: userId } }
        // Prisma remplira userId automatiquement via la relation
      }
    });

    res.status(201).json(pet);
  } catch (e) {
    res.status(500).json({ message: 'Erreur création pet', error: String(e?.message || e) });
  }
});

// PUT /pets/:id - update
router.put('/:id', async (req, res) => {
  try {
    const userId = Number(req.user.sub);
    const id = Number(req.params.id);

    const parsed = PetUpdateSchema.safeParse(req.body);
    if (!parsed.success) {
      return res.status(400).json({ message: 'Invalid body', errors: parsed.error.flatten() });
    }

    // ownership check
    const existing = await prisma.pet.findFirst({ where: { id, userId } }); // ✅ FIX
    if (!existing) return res.status(404).json({ message: 'Pet not found' });

    const data = { ...parsed.data };
    if (data.birthDate !== undefined) data.birthDate = data.birthDate ? new Date(data.birthDate) : null;

    const pet = await prisma.pet.update({
      where: { id },
      data: {
        name: data.name,
        species: data.species,
        breed: data.breed ?? undefined,
        sex: data.sex ?? undefined,
        birthDate: data.birthDate,
        photoUrl: data.photoUrl ?? undefined,
        notes: data.notes ?? undefined
      }
    });

    res.json(pet);
  } catch (e) {
    res.status(500).json({ message: 'Server error', error: String(e?.message || e) });
  }
});

// DELETE /pets/:id
router.delete('/:id', async (req, res) => {
  try {
    const userId = Number(req.user.sub);
    const id = Number(req.params.id);

    const existing = await prisma.pet.findFirst({ where: { id, userId } }); // ✅ FIX
    if (!existing) return res.status(404).json({ message: 'Pet not found' });

    await prisma.pet.delete({ where: { id } });
    res.json({ ok: true });
  } catch (e) {
    res.status(500).json({ message: 'Server error', error: String(e?.message || e) });
  }
});

export default router;
  