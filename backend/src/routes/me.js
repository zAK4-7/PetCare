import express from 'express';
import { z } from 'zod';
import bcrypt from 'bcryptjs';
import { prisma } from '../prisma.js';
import { authRequired } from '../middleware/auth.js';
import { validate } from '../utils/validate.js';

const router = express.Router();

/**
 * =========================
 * GET /me
 * =========================
 * Retourne le profil utilisateur
 * ⚠️ ID NON exposé
 */
router.get('/', authRequired, async (req, res, next) => {
  try {
    const id = Number(req.user.sub);

    const user = await prisma.user.findUnique({
      where: { id },
      select: {
        name: true,
        email: true,
        phone: true,
        timezone: true,
        language: true,
        role: true
      }
    });

    res.json(user);
  } catch (err) {
    next(err);
  }
});

/**
 * =========================
 * PATCH /me
 * =========================
 * Mise à jour du profil utilisateur
 */
const updateSchema = z.object({
  body: z.object({
    name: z.string().min(1).optional(),
    email: z.string().email().optional(),
    phone: z.string().optional(),
    timezone: z.string().optional(),
    language: z.string().optional(),
    password: z.string().min(6).optional()
  })
});

router.patch(
  '/',
  authRequired,
  validate(updateSchema),
  async (req, res, next) => {
    try {
      const id = Number(req.user.sub);
      const body = req.validated.body;

      // 🔐 Email unique
      if (body.email) {
        const exists = await prisma.user.findFirst({
          where: {
            email: body.email,
            NOT: { id }
          }
        });
        if (exists) {
          return res.status(400).json({ message: 'Email déjà utilisé' });
        }
      }

      const data = {};

      if (body.name !== undefined) data.name = body.name;
      if (body.email !== undefined) data.email = body.email;
      if (body.phone !== undefined) data.phone = body.phone;
      if (body.timezone !== undefined) data.timezone = body.timezone;
      if (body.language !== undefined) data.language = body.language;

      // ✅ Prisma = password (pas passwordHash)
      if (body.password) {
        data.password = await bcrypt.hash(body.password, 10);
      }

      const updated = await prisma.user.update({
        where: { id },
        data,
        select: {
          name: true,
          email: true,
          phone: true,
          timezone: true,
          language: true,
          role: true
        }
      });

      res.json(updated);
    } catch (err) {
      next(err);
    }
  }
);

export default router;
