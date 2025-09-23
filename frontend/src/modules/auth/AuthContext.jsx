import React, { createContext, useContext, useMemo, useState } from 'react';

const AuthContext = createContext(null);
export const useAuth = () => useContext(AuthContext);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('accessToken'));
  const [user, setUser] = useState(() => {
    const v = localStorage.getItem('userInfo');
    return v ? JSON.parse(v) : null;
    });

  const login = (t, u) => {
    setToken(t); setUser(u);
    localStorage.setItem('accessToken', t);
    localStorage.setItem('userInfo', JSON.stringify(u));
  };
  const logout = () => {
    setToken(null); setUser(null);
    localStorage.removeItem('accessToken');
    localStorage.removeItem('userInfo');
  };

  const value = useMemo(() => ({ token, user, login, logout }), [token, user]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
