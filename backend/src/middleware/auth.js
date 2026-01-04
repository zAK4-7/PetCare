import jwt from 'jsonwebtoken';

export function authRequired(req, res, next) {
  const header = req.headers.authorization || '';
  const token = header.startsWith('Bearer ') ? header.slice(7) : null;
  if (!token) return res.status(401).json({ message: 'Missing Bearer token' });

  try {
    const payload = jwt.verify(token, process.env.JWT_SECRET);
    req.user = payload; // { sub, role, email, name }
    return next();
  } catch (e) {
    return res.status(401).json({ message: 'Invalid or expired token' });
  }
}

export function adminOnly(req, res, next) {
  if (req.user?.role !== 'ADMIN') return res.status(403).json({ message: 'Admin only' });
  return next();
}
