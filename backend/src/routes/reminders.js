import express from 'express';
import { z } from 'zod';
import { prisma } from '../prisma.js';
import { authRequired } from '../middleware/auth.js';
import { validate } from '../utils/validate.js';

const router = express.Router();
router.use(authRequired);

const createSchema = z.object({
  params: z.object({ healthEventId: z.string() }),
  body: z.object({
    remindAt: z.string().datetime()
  })
});

const updateSchema = z.object({
  params: z.object({ id: z.string() }),
  body: z.object({
    remindAt: z.string().datetime().optional(),
    sent: z.boolean().optional()
  })
});

/**
 * @openapi
 * /health-events/{healthEventId}/reminders:
 *   get:
 *     summary: List reminders for health event
 *     tags: [Reminders]
 *     security: [{ bearerAuth: [] }]
 */
router.get('/health-events/:healthEventId/reminders', async (req, res) => {
  const healthEventId = Number(req.params.healthEventId);
  const userId = Number(req.user.sub);

  const ev = await prisma.healthEvent.findFirst({ where: { id: healthEventId, pet: { userId } } });
  if (!ev) return res.status(404).json({ message: 'Health event not found' });

  const reminders = await prisma.reminder.findMany({ where: { healthEventId }, orderBy: { remindAt: 'asc' } });
  res.json(reminders);
});

router.post('/health-events/:healthEventId/reminders', validate(createSchema), async (req, res) => {
  const healthEventId = Number(req.validated.params.healthEventId);
  const userId = Number(req.user.sub);

  const ev = await prisma.healthEvent.findFirst({ where: { id: healthEventId, pet: { userId } } });
  if (!ev) return res.status(404).json({ message: 'Health event not found' });

  const reminder = await prisma.reminder.create({ data: { healthEventId, remindAt: new Date(req.validated.body.remindAt) } });
  res.status(201).json(reminder);
});

router.patch('/reminders/:id', validate(updateSchema), async (req, res) => {
  const id = Number(req.validated.params.id);
  const userId = Number(req.user.sub);

  const existing = await prisma.reminder.findFirst({ where: { id, healthEvent: { pet: { userId } } } });
  if (!existing) return res.status(404).json({ message: 'Reminder not found' });

  const b = req.validated.body;
  const updated = await prisma.reminder.update({
    where: { id },
    data: { ...b, remindAt: b.remindAt ? new Date(b.remindAt) : undefined }
  });
  res.json(updated);
});

router.delete('/reminders/:id', async (req, res) => {
  const id = Number(req.params.id);
  const userId = Number(req.user.sub);

  const existing = await prisma.reminder.findFirst({ where: { id, healthEvent: { pet: { userId } } } });
  if (!existing) return res.status(404).json({ message: 'Reminder not found' });

  await prisma.reminder.delete({ where: { id } });
  res.json({ message: 'Deleted' });
});

export default router;
