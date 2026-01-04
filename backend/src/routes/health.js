import express from 'express';
import { z } from 'zod';
import { prisma } from '../prisma.js';
import { authRequired } from '../middleware/auth.js';
import { validate } from '../utils/validate.js';

const router = express.Router();
router.use(authRequired);

const createSchema = z.object({
  params: z.object({ petId: z.string() }),
  body: z.object({
    type: z.enum(['VACCIN','TRAITEMENT','CONSULTATION','TOILETTAGE','AUTRE']),
    title: z.string().min(1),
    description: z.string().optional(),
    eventDate: z.string().datetime()
  })
});

const updateSchema = z.object({
  params: z.object({ id: z.string() }),
  body: z.object({
    type: z.enum(['VACCIN','TRAITEMENT','CONSULTATION','TOILETTAGE','AUTRE']).optional(),
    title: z.string().min(1).optional(),
    description: z.string().optional(),
    eventDate: z.string().datetime().optional()
  })
});

// ---------------------------------------------------------------------------
// Compatibility endpoints (some tests/scripts use /health)
// These are thin aliases over the canonical routes below.
// ---------------------------------------------------------------------------

/**
 * @swagger
 * /health:
 *   post:
 *     summary: Create a health event (compat)
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [petId, type, title, eventDate]
 *             properties:
 *               petId: { type: integer }
 *               type: { type: string }
 *               title: { type: string }
 *               description: { type: string }
 *               eventDate: { type: string, format: date-time }
 *     responses:
 *       201: { description: Created }
 */
router.post('/health', authRequired, async (req, res) => {
  try {
    const body = z.object({
      petId: z.number().int().positive(),
      type: z.enum(['VACCIN','TRAITEMENT','CONSULTATION','TOILETTAGE','AUTRE']).default('AUTRE'),
      title: z.string().min(1),
      description: z.string().optional(),
      eventDate: z.string().datetime()
    }).parse(req.body);

    // Ensure the pet belongs to the current user
    const pet = await prisma.pet.findFirst({ where: { id: body.petId, userId: req.user.id } });
    if (!pet) return res.status(404).json({ message: 'Pet introuvable' });

    const created = await prisma.healthEvent.create({
      data: {
        petId: body.petId,
        type: body.type,
        title: body.title,
        description: body.description ?? null,
        eventDate: new Date(body.eventDate)
      }
    });
    return res.status(201).json(created);
  } catch (e) {
    return res.status(400).json({ message: 'Bad request', error: String(e?.message || e) });
  }
});

/**
 * @swagger
 * /health:
 *   get:
 *     summary: List health events (compat)
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: query
 *         name: petId
 *         schema: { type: integer }
 *     responses:
 *       200: { description: OK }
 */
router.get('/health', authRequired, async (req, res) => {
  const petId = req.query.petId ? Number(req.query.petId) : null;
  try {
    if (petId) {
      const pet = await prisma.pet.findFirst({ where: { id: petId, userId: req.user.id } });
      if (!pet) return res.status(404).json({ message: 'Pet introuvable' });
      const items = await prisma.healthEvent.findMany({ where: { petId }, orderBy: { id: 'desc' } });
      return res.json(items);
    }
    // If no petId provided, return events for all user's pets
    const pets = await prisma.pet.findMany({ where: { userId: req.user.id }, select: { id: true } });
    const petIds = pets.map(p => p.id);
    const items = await prisma.healthEvent.findMany({ where: { petId: { in: petIds } }, orderBy: { id: 'desc' } });
    return res.json(items);
  } catch (e) {
    return res.status(500).json({ message: 'Server error', error: String(e?.message || e) });
  }
});

/**
 * @swagger
 * /health/{id}:
 *   get:
 *     summary: Get a health event by id (compat)
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema: { type: integer }
 *     responses:
 *       200: { description: OK }
 */
router.get('/health/:id', authRequired, async (req, res) => {
  const id = Number(req.params.id);
  const item = await prisma.healthEvent.findUnique({ where: { id }, include: { pet: true } });
  if (!item || item.pet.userId !== req.user.id) return res.status(404).json({ message: 'HealthEvent introuvable' });
  return res.json(item);
});

router.put('/health/:id', authRequired, async (req, res) => {
  try {
    const id = Number(req.params.id);
    const existing = await prisma.healthEvent.findUnique({ where: { id }, include: { pet: true } });
    if (!existing || existing.pet.userId !== req.user.id) return res.status(404).json({ message: 'HealthEvent introuvable' });

    const body = z.object({
      type: z.enum(['VACCIN','TRAITEMENT','CONSULTATION','TOILETTAGE','AUTRE']).optional(),
      title: z.string().min(1).optional(),
      description: z.string().optional(),
      eventDate: z.string().datetime().optional()
    }).parse(req.body);

    const updated = await prisma.healthEvent.update({
      where: { id },
      data: {
        type: body.type,
        title: body.title,
        description: body.description,
        eventDate: body.eventDate ? new Date(body.eventDate) : undefined
      }
    });
    return res.json(updated);
  } catch (e) {
    return res.status(400).json({ message: 'Bad request', error: String(e?.message || e) });
  }
});

router.delete('/health/:id', authRequired, async (req, res) => {
  const id = Number(req.params.id);
  const existing = await prisma.healthEvent.findUnique({ where: { id }, include: { pet: true } });
  if (!existing || existing.pet.userId !== req.user.id) return res.status(404).json({ message: 'HealthEvent introuvable' });
  await prisma.healthEvent.delete({ where: { id } });
  return res.json({ ok: true });
});

/**
 * @openapi
 * /pets/{petId}/health-events:
 *   get:
 *     summary: List health events for a pet
 *     tags: [Health]
 *     security: [{ bearerAuth: [] }]
 */
router.get('/pets/:petId/health-events', async (req, res) => {
  const petId = Number(req.params.petId);
  const userId = Number(req.user.sub);
  const pet = await prisma.pet.findFirst({ where: { id: petId, userId } });
  if (!pet) return res.status(404).json({ message: 'Pet not found' });

  const events = await prisma.healthEvent.findMany({
    where: { petId },
    orderBy: { eventDate: 'desc' },
    include: { reminders: true }
  });
  res.json(events);
});

/**
 * @openapi
 * /pets/{petId}/health-events:
 *   post:
 *     summary: Create a health event for a pet
 *     tags: [Health]
 *     security: [{ bearerAuth: [] }]
 */
router.post('/pets/:petId/health-events', validate(createSchema), async (req, res) => {
  const petId = Number(req.validated.params.petId);
  const userId = Number(req.user.sub);
  const pet = await prisma.pet.findFirst({ where: { id: petId, userId } });
  if (!pet) return res.status(404).json({ message: 'Pet not found' });

  const b = req.validated.body;
  const ev = await prisma.healthEvent.create({
    data: { petId, type: b.type, title: b.title, description: b.description, eventDate: new Date(b.eventDate) }
  });
  res.status(201).json(ev);
});

router.patch('/health-events/:id', validate(updateSchema), async (req, res) => {
  const id = Number(req.validated.params.id);
  const userId = Number(req.user.sub);

  const existing = await prisma.healthEvent.findFirst({
    where: { id, pet: { userId } }
  });
  if (!existing) return res.status(404).json({ message: 'Health event not found' });

  const b = req.validated.body;
  const updated = await prisma.healthEvent.update({
    where: { id },
    data: { ...b, eventDate: b.eventDate ? new Date(b.eventDate) : undefined }
  });
  res.json(updated);
});

router.delete('/health-events/:id', async (req, res) => {
  const id = Number(req.params.id);
  const userId = Number(req.user.sub);
  const existing = await prisma.healthEvent.findFirst({ where: { id, pet: { userId } } });
  if (!existing) return res.status(404).json({ message: 'Health event not found' });

  await prisma.healthEvent.delete({ where: { id } });
  res.json({ message: 'Deleted' });
});

export default router;
