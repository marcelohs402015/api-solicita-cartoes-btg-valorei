import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import EmailsPage from '../pages/EmailsPage';
import * as api from '../services/api';

vi.mock('../services/api', () => ({
  fetchEmails: vi.fn(),
  fetchEmailById: vi.fn(),
}));

describe('EmailsPage', () => {
  beforeEach(() => {
    vi.mocked(api.fetchEmails).mockResolvedValue([
      {
        id: 'email-1',
        propostaId: 'p1',
        destinatario: 'cliente@teste.com',
        assunto: 'Proposta aprovada',
        templateJson: {
          titulo: 'Teste',
          tipoOferta: 'A',
          beneficios: ['CASHBACK'],
          mensagem: 'Sua proposta foi aprovada.',
        },
        status: 'DISPARADO',
        criadoEm: new Date().toISOString(),
      },
    ]);
  });

  it('should render email list', async () => {
    render(<EmailsPage />);
    await waitFor(() => {
      expect(screen.getByText('Proposta aprovada')).toBeInTheDocument();
      expect(screen.getByText('CASHBACK')).toBeInTheDocument();
    });
  });
});
