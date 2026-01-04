import 'dotenv/config';
import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import morgan from 'morgan';
import rateLimit from 'express-rate-limit';
import swaggerUi from 'swagger-ui-express';
import { makeSwaggerSpec } from './swagger.js';

import authRoutes from './routes/auth.js';
import meRoutes from './routes/me.js';
import petsRoutes from './routes/pets.js';
import healthRoutes from './routes/health.js';
import remindersRoutes from './routes/reminders.js';
import agendaRoutes from './routes/agenda.js';
import servicesRoutes from './routes/services.js';
import adminRoutes from './routes/admin.js';

const app = express();

// Security & basics
app.use(helmet());
app.use(express.json({ limit: '2mb' }));
app.use(morgan('dev'));

// CORS
app.use(cors({
  origin: process.env.CORS_ORIGIN === '*' ? true : (process.env.CORS_ORIGIN || true),
  credentials: true
}));

// Rate limiting
app.use(rateLimit({
  windowMs: 60 * 1000,
  max: 120
}));

// Routes
app.get('/', (req, res) => res.json({ name: 'PetCare API', status: 'ok' }));
app.use('/auth', authRoutes);
app.use('/me', meRoutes);
app.use('/pets', petsRoutes);
app.use('/', healthRoutes);
app.use('/', remindersRoutes);
app.use('/agenda', agendaRoutes);
app.use('/services', servicesRoutes);
app.use('/admin', adminRoutes);

// Swagger
const spec = makeSwaggerSpec();
app.use('/docs', swaggerUi.serve, swaggerUi.setup(spec));

// Error handler
app.use((err, req, res, next) => {
  console.error(err);
  res.status(500).json({ message: 'Server error', error: String(err?.message || err) });
});

const port = Number(process.env.PORT || 3000);

app.listen(port, "0.0.0.0", () => {
  console.log(`✅ PetCare API running on http://0.0.0.0:${port}`);
  console.log(`📚 Swagger on http://0.0.0.0:${port}/docs`);
});
