import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import PropostasPage from './PropostasPage';
import * as api from '../services/api';

vi.mock('../services/api', () => ({
  fetchPropostas: vi.fn(),
}));

describe('PropostasPage', () => {
  beforeEach(() => {
    vi.mocked(api.fetchPropostas).mockResolvedValue([
      {
        id: '12345678-abcd-efgh',
        renda: 5000,
        investimentos: 1000,
        tempoContaAnos: 2,
        tipoOferta: 'A',
        beneficios: ['CASHBACK'],
        status: 'APPROVED',
        motivosRejeicao: [],
        accountId: 'CARD-1',
        criadoEm: '2026-07-14T12:00:00Z',
      },
      {
        id: '87654321-wxyz-abcd',
        renda: 1000,
        investimentos: 0,
        tempoContaAnos: 0,
        tipoOferta: 'B',
        beneficios: [],
        status: 'REJECTED',
        motivosRejeicao: ['Renda insuficiente'],
        accountId: null,
        criadoEm: '2026-07-14T13:00:00Z',
      },
    ]);
  });

  it('should render propostas table', async () => {
    render(<PropostasPage />);

    await waitFor(() => {
      expect(screen.getByText('Propostas Persistidas')).toBeInTheDocument();
      expect(screen.getByText('APPROVED')).toBeInTheDocument();
      expect(screen.getByText('REJECTED')).toBeInTheDocument();
      expect(screen.getByText('CARD-1')).toBeInTheDocument();
    });
  });
});
