import { createContext, useContext, useMemo, useState } from 'react';
import { api, setAuthToken } from '../api/client.js';

const AuthContext = createContext(null);
const AUTH_STORAGE_KEY = 'qr_attendance_auth';

function loadStoredAuth() {
  try {
    const stored = JSON.parse(localStorage.getItem(AUTH_STORAGE_KEY));
    if (stored?.token && stored?.user) {
      if (stored.expiresAt && Date.now() > stored.expiresAt) {
        localStorage.removeItem(AUTH_STORAGE_KEY);
        return { token: null, user: null };
      }
      setAuthToken(stored.token);
      return stored;
    }
  } catch {
    localStorage.removeItem(AUTH_STORAGE_KEY);
  }
  return { token: null, user: null };
}

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(loadStoredAuth);
  const token = auth.token;
  const user = auth.user;

  async function login(email, password) {
    const response = await api.post('/auth/login', { email, password });
    const nextAuth = {
      token: response.data.token,
      user: response.data.user,
      expiresAt: Date.now() + response.data.expiresInMinutes * 60 * 1000
    };
    setAuth(nextAuth);
    localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(nextAuth));
    setAuthToken(nextAuth.token);
    return response.data.user;
  }

  function logout() {
    setAuth({ token: null, user: null });
    localStorage.removeItem(AUTH_STORAGE_KEY);
    setAuthToken(null);
  }

  const value = useMemo(() => ({ token, user, login, logout, isAuthenticated: Boolean(token) }), [token, user]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
