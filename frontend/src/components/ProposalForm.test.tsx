import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ProposalForm from './ProposalForm';
import * as api from '../services/api';

vi.mock('../services/api', () => ({
  submitProposal: vi.fn(),
  fetchExecution: vi.fn(),
}));

describe('ProposalForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should submit proposal and show execution flow', async () => {
    const user = userEvent.setup();

    vi.mocked(api.submitProposal).mockResolvedValue({
      proposalId: 'abc-123',
      status: 'APPROVED',
      motivosRejeicao: [],
      cardAccount: { created: true, accountId: 'CARD-1' },
      activatedBenefits: ['CASHBACK'],
    });
    vi.mocked(api.fetchExecution).mockResolvedValue({
      proposalId: 'abc-123',
      steps: [{ step: 'KAFKA', status: 'DONE', detail: '' }],
    });

    render(<ProposalForm />);
    await user.click(screen.getByRole('button', { name: 'Submeter Proposta' }));

    await waitFor(() => {
      expect(screen.getByText(/Status: APPROVED/)).toBeInTheDocument();
    });

    await waitFor(
      () => {
        expect(screen.getByText('Execucao do fluxo')).toBeInTheDocument();
        expect(screen.getByText('KAFKA')).toBeInTheDocument();
      },
      { timeout: 3000 },
    );
  });

  it('should toggle benefits', async () => {
    const user = userEvent.setup();
    render(<ProposalForm />);

    const checkbox = screen.getByRole('checkbox', { name: 'CASHBACK' });
    await user.click(checkbox);
    expect(checkbox).toBeChecked();

    await user.click(checkbox);
    expect(checkbox).not.toBeChecked();
  });

  it('should show error on failure', async () => {
    const user = userEvent.setup();
    vi.mocked(api.submitProposal).mockRejectedValue(new Error('Falha na API'));

    render(<ProposalForm />);
    await user.click(screen.getByRole('button', { name: 'Submeter Proposta' }));

    await waitFor(() => {
      expect(screen.getByText('Falha na API')).toBeInTheDocument();
    });
  });
});
