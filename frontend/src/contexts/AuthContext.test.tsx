import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
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
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => ({ topic: 'proposals.events', consumers: [], recentMessagesCount: 0 }),
      }),
    );
  });

  it('should restore session on mount after API validation', async () => {
    setAuthCredentials('admin', 'admin123');

    render(
      <AuthProvider>
        <AuthStatus />
      </AuthProvider>,
    );

    await waitFor(() => {
      expect(screen.getByTestId('auth-status')).toHaveTextContent('yes');
    });
  });
});
