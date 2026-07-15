import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import App from './App';
import { clearAuth } from './services/api';

describe('App', () => {
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

  it('should show login when unauthenticated', async () => {
    render(<App />);

    await waitFor(() => {
      expect(screen.getByText('Proposals API')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Entrar' })).toBeInTheDocument();
    });
  });

  it('should show dashboard after login', async () => {
    const user = userEvent.setup();
    render(<App />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Entrar' })).toBeInTheDocument();
    });

    await user.click(screen.getByRole('button', { name: 'Entrar' }));

    await waitFor(() => {
      expect(screen.getByText('Proposals API — Dashboard')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Sair' })).toBeInTheDocument();
    });
  });

  it('should restore dashboard on refresh when session exists', async () => {
    const { setAuthCredentials } = await import('./services/api');
    setAuthCredentials('admin', 'admin123');

    render(<App />);

    await waitFor(() => {
      expect(screen.getByText('Proposals API — Dashboard')).toBeInTheDocument();
      expect(screen.queryByRole('button', { name: 'Entrar' })).not.toBeInTheDocument();
    });
  });

  it('should stay on login when session exists but API is unavailable', async () => {
    const { setAuthCredentials } = await import('./services/api');
    setAuthCredentials('admin', 'admin123');
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('Failed to fetch')));

    render(<App />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Entrar' })).toBeInTheDocument();
      expect(screen.queryByText('Proposals API — Dashboard')).not.toBeInTheDocument();
    });
  });
});
