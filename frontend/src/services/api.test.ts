import { beforeEach, describe, expect, it, vi } from 'vitest';
import {
  clearAuth,
  fetchEmailById,
  fetchEmails,
  fetchEvents,
  fetchExecution,
  fetchHistorico,
  fetchPropostas,
  fetchQueueStatus,
  isAuthenticated,
  loadAuthFromSession,
  setAuthCredentials,
  submitProposal,
  validateLogin,
} from '../services/api';

describe('api service', () => {
  beforeEach(() => {
    clearAuth();
    sessionStorage.clear();
    vi.restoreAllMocks();
  });

  it('should set and load auth credentials', () => {
    setAuthCredentials('admin', 'admin123');
    expect(isAuthenticated()).toBe(true);

    clearAuth();
    expect(isAuthenticated()).toBe(false);

    setAuthCredentials('admin', 'admin123');
    loadAuthFromSession();
    expect(isAuthenticated()).toBe(true);
  });

  it('should submit proposal with authorization header', async () => {
    setAuthCredentials('admin', 'admin123');
    const mockResponse = {
      proposalId: '123',
      status: 'APPROVED',
      motivosRejeicao: [],
      cardAccount: { created: true, accountId: 'CARD-1' },
      activatedBenefits: ['CASHBACK'],
    };

    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => mockResponse,
    }));

    const result = await submitProposal({
      renda: 5000,
      investimentos: 0,
      tempoContaAnos: 1,
      tipoOferta: 'A',
      beneficios: ['CASHBACK'],
    });

    expect(result.status).toBe('APPROVED');
    expect(fetch).toHaveBeenCalledWith(
      expect.stringContaining('/api/v1/proposals'),
      expect.objectContaining({
        method: 'POST',
        headers: expect.objectContaining({
          Authorization: expect.stringContaining('Basic'),
        }),
      }),
    );
  });

  it('should clear auth on unauthorized response', async () => {
    setAuthCredentials('admin', 'wrong');

    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: false,
      status: 401,
      json: async () => ({}),
    }));

    await expect(validateLogin('admin', 'wrong')).resolves.toBe(false);
    expect(isAuthenticated()).toBe(false);
  });

  it('should validate login successfully', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({ topic: 'proposals.events', consumers: [], recentMessagesCount: 0 }),
    }));

    const valid = await validateLogin('admin', 'admin123');
    expect(valid).toBe(true);
    expect(isAuthenticated()).toBe(true);
  });

  it('should fetch historico, emails, propostas and events', async () => {
    setAuthCredentials('admin', 'admin123');
    const historico = [{ id: '1', propostaId: 'p1', evento: 'EVT', status: 'APPROVED', payload: {}, criadoEm: '' }];
    const emails = [{ id: 'e1', propostaId: 'p1', destinatario: 'a@b.com', assunto: 'S', templateJson: {}, status: 'GERADO', criadoEm: '' }];
    const propostas = [{ id: 'p1', renda: 1, investimentos: 0, tempoContaAnos: 1, tipoOferta: 'A', beneficios: [], status: 'APPROVED', motivosRejeicao: [], accountId: null, criadoEm: '' }];

    vi.stubGlobal('fetch', vi.fn()
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => historico })
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => emails })
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => emails[0] })
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => propostas })
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => historico }));

    await expect(fetchHistorico(10)).resolves.toEqual(historico);
    await expect(fetchEmails(10)).resolves.toEqual(emails);
    await expect(fetchEmailById('e1')).resolves.toEqual(emails[0]);
    await expect(fetchPropostas(10)).resolves.toEqual(propostas);
    await expect(fetchEvents(10)).resolves.toEqual(historico);
  });

  it('should fetch execution and queue status', async () => {
    setAuthCredentials('admin', 'admin123');
    const execution = { proposalId: 'p1', steps: [{ step: 'KAFKA', status: 'DONE', detail: '' }] };
    const queue = { topic: 'proposals.events', consumers: [], recentMessagesCount: 1 };

    vi.stubGlobal('fetch', vi.fn()
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => execution })
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => queue }));

    await expect(fetchExecution('p1')).resolves.toEqual(execution);
    await expect(fetchQueueStatus()).resolves.toEqual(queue);
  });

  it('should throw api error message', async () => {
    setAuthCredentials('admin', 'admin123');

    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: false,
      status: 500,
      json: async () => ({ message: 'Erro interno' }),
    }));

    await expect(fetchQueueStatus()).rejects.toThrow('Erro interno');
  });
});
