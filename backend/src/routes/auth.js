import express from 'express';
import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import { z } from 'zod';
import { prisma } from '../prisma.js';
import { authRequired } from '../middleware/auth.js';
import { validate } from '../utils/validate.js';

const router = express.Router();

const registerSchema = z.object({
  body: z.object({
    name: z.string().min(2),
    email: z.string().email(),
    password: z.string().min(6),
    phone: z.string().optional(),
    timezone: z.string().optional(),
    language: z.string().optional()
  })
});

const loginSchema = z.object({
  body: z.object({
    email: z.string().email(),
    password: z.string().min(1)
  })
});

/**
 * @openapi
 * /auth/register:
 *   post:
 *     summary: Register a new user
 *     tags: [Auth]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [name, email, password]
 *     responses:
 *       201: { description: User created }
 */
router.post('/register', validate(registerSchema), async (req, res) => {
  const { name, email, password, phone, timezone, language } = req.validated.body;

  const exists = await prisma.user.findUnique({ where: { email } });
  if (exists) return res.status(409).json({ message: 'Email already used' });

  const hash = await bcrypt.hash(password, 10);
  const user = await prisma.user.create({
    data: { name, email, password: hash, phone, timezone, language, role: 'USER' },
    select: { id: true, name: true, email: true, role: true }
  });

  return res.status(201).json(user);
});

/**
 * @openapi
 * /auth/login:
 *   post:
 *     summary: Login and get JWT
 *     tags: [Auth]
 *     requestBody:
 *       required: true
 *     responses:
 *       200: { description: Token }
 */
router.post('/login', validate(loginSchema), async (req, res) => {
  const { email, password } = req.validated.body;
  const user = await prisma.user.findUnique({ where: { email } });
  if (!user) return res.status(401).json({ message: 'Invalid credentials' });

  const ok = await bcrypt.compare(password, user.password);
  if (!ok) return res.status(401).json({ message: 'Invalid credentials' });

  const token = jwt.sign(
    { sub: user.id, role: user.role, email: user.email, name: user.name },
    process.env.JWT_SECRET,
    { expiresIn: '7d' }
  );

  return res.json({ token, user: { id: user.id, name: user.name, email: user.email, role: user.role } });
});

/**
 * @openapi
 * /me:
 *   get:
 *     summary: Get current user profile
 *     tags: [Auth]
 *     security: [{ bearerAuth: [] }]
 *     responses:
 *       200: { description: Profile }
 */
router.get('/me', authRequired, async (req, res) => {
  const id = Number(req.user.sub);
  const user = await prisma.user.findUnique({
    where: { id },
    select: { id: true, name: true, email: true, phone: true, timezone: true, language: true, role: true }
  });
  return res.json(user);
});

export default router;
