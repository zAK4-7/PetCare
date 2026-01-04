-- PetCare SQLite init (optionnel). Si tu utilises Prisma, ignore ce fichier.
PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS users (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  email TEXT NOT NULL UNIQUE,
  phone TEXT,
  timezone TEXT,
  language TEXT,
  password TEXT NOT NULL,
  role TEXT NOT NULL DEFAULT 'USER',
  createdAt TEXT NOT NULL DEFAULT (datetime('now')),
  updatedAt TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS pets (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  userId INTEGER NOT NULL,
  name TEXT NOT NULL,
  species TEXT NOT NULL,
  breed TEXT,
  sex TEXT,
  birthDate TEXT,
  photoUrl TEXT,
  notes TEXT,
  createdAt TEXT NOT NULL DEFAULT (datetime('now')),
  updatedAt TEXT NOT NULL DEFAULT (datetime('now')),
  FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS health_events (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  petId INTEGER NOT NULL,
  type TEXT NOT NULL,
  title TEXT NOT NULL,
  description TEXT,
  eventDate TEXT NOT NULL,
  createdAt TEXT NOT NULL DEFAULT (datetime('now')),
  updatedAt TEXT NOT NULL DEFAULT (datetime('now')),
  FOREIGN KEY(petId) REFERENCES pets(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reminders (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  healthEventId INTEGER NOT NULL,
  remindAt TEXT NOT NULL,
  sent INTEGER NOT NULL DEFAULT 0,
  createdAt TEXT NOT NULL DEFAULT (datetime('now')),
  updatedAt TEXT NOT NULL DEFAULT (datetime('now')),
  FOREIGN KEY(healthEventId) REFERENCES health_events(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS agenda_events (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  userId INTEGER NOT NULL,
  title TEXT NOT NULL,
  type TEXT,
  startAt TEXT NOT NULL,
  endAt TEXT,
  notes TEXT,
  FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS services (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  type TEXT NOT NULL,
  name TEXT NOT NULL,
  address TEXT,
  phone TEXT,
  hours TEXT,
  lat REAL,
  lng REAL,
  createdAt TEXT NOT NULL DEFAULT (datetime('now')),
  updatedAt TEXT NOT NULL DEFAULT (datetime('now'))
);
