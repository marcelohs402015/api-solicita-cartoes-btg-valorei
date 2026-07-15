import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import HistoricoPage from '../pages/HistoricoPage';
import * as api from '../services/api';

vi.mock('../services/api', () => ({
  fetchHistorico: vi.fn(),
}));

describe('HistoricoPage', () => {
  beforeEach(() => {
    vi.mocked(api.fetchHistorico).mockResolvedValue([
      {
        id: '1',
        propostaId: 'p1',
        evento: 'PROPOSTA_PERSISTIDA',
        status: 'APPROVED',
        payload: {},
        criadoEm: new Date().toISOString(),
      },
    ]);
  });

  it('should render historico items', async () => {
    render(<HistoricoPage />);
    await waitFor(() => {
      expect(screen.getByText('PROPOSTA_PERSISTIDA')).toBeInTheDocument();
    });
  });
});
