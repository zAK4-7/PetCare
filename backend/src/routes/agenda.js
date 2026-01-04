import express from 'express';
import { z } from 'zod';
import { prisma } from '../prisma.js';
import { authRequired } from '../middleware/auth.js';
import { validate } from '../utils/validate.js';

const router = express.Router();
router.use(authRequired);

const createSchema = z.object({
  body: z.object({
    title: z.string().min(1),
    type: z.string().optional(),
    startAt: z.string().datetime(),
    endAt: z.string().datetime().optional(),
    notes: z.string().optional()
  })
});

const updateSchema = z.object({
  params: z.object({ id: z.string() }),
  body: z.object({
    title: z.string().min(1).optional(),
    type: z.string().optional(),
    startAt: z.string().datetime().optional(),
    endAt: z.string().datetime().optional(),
    notes: z.string().optional()
  })
});

/**
 * @openapi
 * /agenda:
 *   get:
 *     summary: List agenda events of current user
 *     tags: [Agenda]
 *     security: [{ bearerAuth: [] }]
 */
router.get('/', async (req, res) => {
  const userId = Number(req.user.sub);
  const events = await prisma.agendaEvent.findMany({ where: { userId }, orderBy: { startAt: 'asc' } });
  res.json(events);
});

router.post('/', validate(createSchema), async (req, res) => {
  const userId = Number(req.user.sub);
  const b = req.validated.body;
  const ev = await prisma.agendaEvent.create({
    data: {
      userId,
      title: b.title,
      type: b.type,
      startAt: new Date(b.startAt),
      endAt: b.endAt ? new Date(b.endAt) : null,
      notes: b.notes
    }
  });
  res.status(201).json(ev);
});

router.patch('/:id', validate(updateSchema), async (req, res) => {
  const id = Number(req.validated.params.id);
  const userId = Number(req.user.sub);
  const existing = await prisma.agendaEvent.findFirst({ where: { id, userId } });
  if (!existing) return res.status(404).json({ message: 'Agenda event not found' });

  const b = req.validated.body;
  const updated = await prisma.agendaEvent.update({
    where: { id },
    data: {
      ...b,
      startAt: b.startAt ? new Date(b.startAt) : undefined,
      endAt: b.endAt ? new Date(b.endAt) : undefined
    }
  });
  res.json(updated);
});

router.delete('/:id', async (req, res) => {
  const id = Number(req.params.id);
  const userId = Number(req.user.sub);
  const existing = await prisma.agendaEvent.findFirst({ where: { id, userId } });
  if (!existing) return res.status(404).json({ message: 'Agenda event not found' });

  await prisma.agendaEvent.delete({ where: { id } });
  res.json({ message: 'Deleted' });
});

export default router;
