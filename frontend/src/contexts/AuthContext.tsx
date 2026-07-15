import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import { clearAuth, isAuthenticated, loadAuthFromSession } from '../services/api';

interface AuthContextValue {
  authenticated: boolean;
  login: () => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [authenticated, setAuthenticated] = useState(false);

  useEffect(() => {
    loadAuthFromSession();
    setAuthenticated(isAuthenticated());
  }, []);

  function login() {
    setAuthenticated(true);
  }

  function logout() {
    clearAuth();
    setAuthenticated(false);
  }

  return (
    <AuthContext.Provider value={{ authenticated, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return ctx;
}
