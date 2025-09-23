import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { CssBaseline, Container } from '@mui/material'
import { AuthProvider } from './modules/auth/AuthContext.jsx'
import LoginPage from './modules/auth/LoginPage.jsx'
import ProductsPage from './modules/products/ProductsPage.jsx'
import ProductDetailPage from './modules/products/ProductDetailPage.jsx'
import AdminProductsPage from './modules/admin/AdminProductsPage.jsx'
import AdminRoute from './modules/auth/AdminRoute.jsx'
import ProtectedRoute from './modules/auth/ProtectedRoute.jsx'

const qc = new QueryClient()

function App() {
  return (
    <QueryClientProvider client={qc}>
      <AuthProvider>
        <CssBaseline />
        <BrowserRouter>
          <Container maxWidth="lg" sx={{ py: 3 }}>
            <Routes>
              <Route path="/" element={<ProductsPage />} />
              <Route path="/products" element={<ProductsPage />} />
              <Route path="/products/:id" element={<ProductDetailPage />} />

              <Route path="/auth/login" element={<LoginPage />} />

              <Route path="/admin/products" element={
                <AdminRoute>
                  <AdminProductsPage />
                </AdminRoute>
              } />

              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </Container>
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  )
}

ReactDOM.createRoot(document.getElementById('root')).render(<App />)
