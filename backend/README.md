# PetCare Backend (Salma)

API REST PetCare (Node.js + Express + Prisma + SQLite) avec :
- Auth JWT (USER / ADMIN)
- CRUD : utilisateurs (admin), animaux, événements santé, rappels, agenda, services
- Swagger UI : `/docs`

## 1) Prérequis
- Node.js 18+ (recommandé 20+)
- npm

## 2) Installation
```bash
cd petcare-backend
cp .env.example .env
npm install
npm run prisma:generate
npm run prisma:migrate
npm run seed
npm run dev
```

API : `http://localhost:3000`  
Swagger : `http://localhost:3000/docs`

## 3) Comptes
Après `npm run seed` :
- admin: `admin@petcare.local`
- mdp: `Admin@1234`

## 4) Endpoints principaux
- POST `/auth/register`
- POST `/auth/login`
- GET `/me` (Bearer)
- GET/POST/PATCH/DELETE `/pets`
- GET/POST `/pets/:petId/health-events`
- PATCH/DELETE `/health-events/:id`
- GET/POST `/health-events/:healthEventId/reminders`
- PATCH/DELETE `/reminders/:id`
- GET `/services` (public)
- POST/PATCH/DELETE `/services` (admin)
- GET/POST/PATCH/DELETE `/agenda` (Bearer)
- GET/POST `/admin/users` (admin)

## 5) Notes FCM (notifications push)
FCM n'est pas activé dans cette version (ça demande la config Firebase + clés).
Je peux l'ajouter si tu me donnes le `firebase-service-account.json` ou le projet Firebase.
