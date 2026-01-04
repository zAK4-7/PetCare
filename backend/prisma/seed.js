import 'dotenv/config';
import bcrypt from 'bcryptjs';
import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

async function main() {
  const email = 'admin@petcare.local';
  const password = 'Admin@1234';
  const hash = await bcrypt.hash(password, 10);

  const existing = await prisma.user.findUnique({ where: { email } });
  if (!existing) {
    await prisma.user.create({
      data: {
        name: 'Admin',
        email,
        password: hash,
        role: 'ADMIN',
        timezone: process.env.TZ || 'Africa/Casablanca',
        language: 'fr'
      }
    });
    console.log('✅ Admin created:', email, password);
  } else {
    console.log('ℹ️ Admin already exists:', email);
  }
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
