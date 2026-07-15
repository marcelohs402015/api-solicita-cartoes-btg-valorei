import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import { clearAuth, setOnUnauthorized, validateSession } from '../services/api';

interface AuthContextValue {
  authenticated: boolean;
  authReady: boolean;
  login: () => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [authenticated, setAuthenticated] = useState(false);
  const [authReady, setAuthReady] = useState(false);

  useEffect(() => {
    let cancelled = false;

    setOnUnauthorized(() => {
      setAuthenticated(false);
    });

    async function bootstrap() {
      const valid = await validateSession();
      if (!cancelled) {
        setAuthenticated(valid);
        setAuthReady(true);
      }
    }

    bootstrap();

    return () => {
      cancelled = true;
      setOnUnauthorized(null);
    };
  }, []);

  function login() {
    setAuthenticated(true);
  }

  function logout() {
    clearAuth();
    setAuthenticated(false);
  }

  return (
    <AuthContext.Provider value={{ authenticated, authReady, login, logout }}>
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
