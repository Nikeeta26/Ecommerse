import React, { useState } from 'react';
import { Box, Button, Paper, Stack, TextField, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import api from '../../api/axios';
import { useAuth } from './AuthContext';

export default function LoginPage() {
  const [identifier, setIdentifier] = useState('admin@shop.local');
  const [password, setPassword] = useState('admin123');
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const auth = useAuth();

  const onSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const res = await api.post('/auth/login', { identifier, password });
      const data = res.data;
      auth.login(data.accessToken, {
        userId: data.userId,
        fullName: data.fullName,
        email: data.email,
        role: data.role,
      });
      navigate(data.role === 'ROLE_ADMIN' ? '/admin/products' : '/');
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed');
    }
  };

  return (
    <Stack alignItems="center" justifyContent="center" minHeight="60vh">
      <Paper sx={{ p: 4, width: 400, maxWidth: '90%' }}>
        <Typography variant="h5" gutterBottom>Login</Typography>
        <Box component="form" onSubmit={onSubmit}>
          <Stack spacing={2}>
            <TextField label="Email or Phone" value={identifier} onChange={(e) => setIdentifier(e.target.value)} fullWidth />
            <TextField label="Password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} fullWidth />
            {error && <Typography color="error">{error}</Typography>}
            <Button type="submit" variant="contained">Login</Button>
          </Stack>
        </Box>
      </Paper>
    </Stack>
  );
}
