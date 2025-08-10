const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const path = require('path');
const jwt = require('jsonwebtoken');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
app.use(express.static(path.join(__dirname, 'public')));

// Mock session storage
const sessions = new Map();

// Mock authentication middleware
const authenticateToken = (req, res, next) => {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];

  if (!token) {
    return res.status(401).json({ error: 'Access token required' });
  }

  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET || 'test_secret');
    req.user = decoded;
    next();
  } catch (error) {
    return res.status(403).json({ error: 'Invalid token' });
  }
};

// Routes

// Home page
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

// Login page
app.get('/login', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'login.html'));
});

// Registration page
app.get('/register', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'register.html'));
});

// Dashboard page
app.get('/dashboard', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'dashboard.html'));
});

// Profile page
app.get('/profile', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'profile.html'));
});

// Registration module
app.get('/registration', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'registration.html'));
});

// Eligibility module
app.get('/eligibility', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'eligibility.html'));
});

// Payment module
app.get('/payments', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'payments.html'));
});

// Grievance module
app.get('/grievances', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'grievances.html'));
});

// Analytics module
app.get('/analytics', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'analytics.html'));
});

// API Routes

// Mock login endpoint
app.post('/api/auth/login', (req, res) => {
  const { email, password } = req.body;
  
  // Mock authentication logic
  if (email && password) {
    const user = {
      id: '123',
      email,
      role: email.includes('admin') ? 'admin' : 'user',
      name: 'Test User'
    };
    
    const token = jwt.sign(user, process.env.JWT_SECRET || 'test_secret', { expiresIn: '1h' });
    const refreshToken = jwt.sign({ userId: user.id }, process.env.JWT_SECRET || 'test_secret', { expiresIn: '7d' });
    
    sessions.set(user.id, { user, token, refreshToken });
    
    res.json({
      success: true,
      user,
      accessToken: token,
      refreshToken
    });
  } else {
    res.status(400).json({ error: 'Email and password required' });
  }
});

// Mock logout endpoint
app.post('/api/auth/logout', authenticateToken, (req, res) => {
  sessions.delete(req.user.id);
  res.json({ success: true, message: 'Logged out successfully' });
});

// Mock user profile endpoint
app.get('/api/user/profile', authenticateToken, (req, res) => {
  res.json({
    success: true,
    user: req.user
  });
});

// Mock password reset endpoint
app.post('/api/auth/reset-password', (req, res) => {
  const { email } = req.body;
  
  if (email) {
    res.json({
      success: true,
      message: 'Password reset email sent'
    });
  } else {
    res.status(400).json({ error: 'Email required' });
  }
});

// Mock registration endpoint
app.post('/api/auth/register', (req, res) => {
  const { email, password, firstName, lastName } = req.body;
  
  if (email && password && firstName && lastName) {
    const user = {
      id: Date.now().toString(),
      email,
      firstName,
      lastName,
      role: 'user'
    };
    
    res.json({
      success: true,
      message: 'Registration successful',
      user
    });
  } else {
    res.status(400).json({ error: 'All fields required' });
  }
});

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({
    status: 'UP',
    service: 'dsr-mock-frontend',
    timestamp: new Date().toISOString()
  });
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({ error: 'Something went wrong!' });
});

// 404 handler
app.use((req, res) => {
  res.status(404).json({ error: 'Not found' });
});

// Start server
app.listen(PORT, () => {
  console.log(`DSR Mock Frontend Server running on port ${PORT}`);
  console.log(`Environment: ${process.env.NODE_ENV || 'development'}`);
});
