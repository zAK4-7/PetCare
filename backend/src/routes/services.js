import express from 'express';
import { z } from 'zod';
import { prisma } from '../prisma.js';
import { authRequired, adminOnly } from '../middleware/auth.js';
import { validate } from '../utils/validate.js';

const router = express.Router();

const createSchema = z.object({
  body: z.object({
    type: z.enum(['VETERINAIRE','TOILETTEUR','AUTRE']),
    name: z.string().min(1),
    address: z.string().optional(),
    phone: z.string().optional(),
    hours: z.string().optional(),
    lat: z.number().optional(),
    lng: z.number().optional()
  })
});

const updateSchema = z.object({
  params: z.object({ id: z.string() }),
  body: z.object({
    type: z.enum(['VETERINAIRE','TOILETTEUR','AUTRE']).optional(),
    name: z.string().min(1).optional(),
    address: z.string().optional(),
    phone: z.string().optional(),
    hours: z.string().optional(),
    lat: z.number().optional(),
    lng: z.number().optional()
  })
});

/**
 * @openapi
 * /services:
 *   get:
 *     summary: List nearby services (basic filter by type)
 *     tags: [Services]
 *     parameters:
 *       - in: query
 *         name: type
 *         schema: { type: string }
 */
router.get('/', async (req, res) => {
  const { type } = req.query;
  const where = {};
  if (type && ['VETERINAIRE','TOILETTEUR','AUTRE'].includes(String(type))) where.type = String(type);

  // NOTE: simple list. Distance filtering can be done client-side or enhanced later.
  const services = await prisma.service.findMany({ where, orderBy: { id: 'desc' } });
  res.json(services);
});

// Admin CRUD
router.post('/', authRequired, adminOnly, validate(createSchema), async (req, res) => {
  const s = await prisma.service.create({ data: req.validated.body });
  res.status(201).json(s);
});

router.patch('/:id', authRequired, adminOnly, validate(updateSchema), async (req, res) => {
  const id = Number(req.validated.params.id);
  const existing = await prisma.service.findUnique({ where: { id } });
  if (!existing) return res.status(404).json({ message: 'Service not found' });

  const updated = await prisma.service.update({ where: { id }, data: req.validated.body });
  res.json(updated);
});

router.delete('/:id', authRequired, adminOnly, async (req, res) => {
  const id = Number(req.params.id);
  const existing = await prisma.service.findUnique({ where: { id } });
  if (!existing) return res.status(404).json({ message: 'Service not found' });

  await prisma.service.delete({ where: { id } });
  res.json({ message: 'Deleted' });
});

export default router;
