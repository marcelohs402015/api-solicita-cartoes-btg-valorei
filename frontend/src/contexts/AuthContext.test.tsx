import { beforeEach, describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { AuthProvider, useAuth } from '../contexts/AuthContext';
import { clearAuth, setAuthCredentials } from '../services/api';

function AuthStatus() {
  const { authenticated } = useAuth();
  return <span data-testid="auth-status">{authenticated ? 'yes' : 'no'}</span>;
}

describe('AuthContext', () => {
  beforeEach(() => {
    clearAuth();
    sessionStorage.clear();
  });

  it('should restore session on mount', () => {
    setAuthCredentials('admin', 'admin123');

    render(
      <AuthProvider>
        <AuthStatus />
      </AuthProvider>,
    );

    expect(screen.getByTestId('auth-status')).toHaveTextContent('yes');
  });
});
