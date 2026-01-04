import express from 'express';
import { z } from 'zod';
import bcrypt from 'bcryptjs';
import { prisma } from '../prisma.js';
import { authRequired, adminOnly } from '../middleware/auth.js';
import { validate } from '../utils/validate.js';

const router = express.Router();
router.use(authRequired, adminOnly);

const createSchema = z.object({
  body: z.object({
    name: z.string().min(2),
    email: z.string().email(),
    password: z.string().min(6),
    role: z.enum(['USER','ADMIN']).optional(),
    phone: z.string().optional(),
    timezone: z.string().optional(),
    language: z.string().optional()
  })
});

/**
 * @openapi
 * /admin/users:
 *   get:
 *     summary: List users (admin)
 *     tags: [Admin]
 *     security: [{ bearerAuth: [] }]
 */
router.get('/users', async (req, res) => {
  const users = await prisma.user.findMany({
    select: { id: true, name: true, email: true, role: true, phone: true, timezone: true, language: true, createdAt: true }
  });
  res.json(users);
});

router.post('/users', validate(createSchema), async (req, res) => {
  const b = req.validated.body;
  const exists = await prisma.user.findUnique({ where: { email: b.email } });
  if (exists) return res.status(409).json({ message: 'Email already used' });

  const hash = await bcrypt.hash(b.password, 10);
  const user = await prisma.user.create({
    data: {
      name: b.name,
      email: b.email,
      password: hash,
      role: b.role || 'USER',
      phone: b.phone,
      timezone: b.timezone,
      language: b.language
    },
    select: { id: true, name: true, email: true, role: true }
  });
  res.status(201).json(user);
});

export default router;
