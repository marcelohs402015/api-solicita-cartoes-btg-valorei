import type {

  EmailDisparo,

  ExecutionFlow,

  HistoricoItem,

  ProposalRequest,

  ProposalResponse,

  PropostaSummary,

  QueueStatus,

} from '../types/proposal';



const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';



let authHeader: string | null = null;

let onUnauthorized: (() => void) | null = null;



export function setOnUnauthorized(handler: (() => void) | null) {

  onUnauthorized = handler;

}



export function setAuthCredentials(username: string, password: string) {

  authHeader = `Basic ${btoa(`${username}:${password}`)}`;

  sessionStorage.setItem('authHeader', authHeader);

}



export function loadAuthFromSession() {

  authHeader = sessionStorage.getItem('authHeader');

}



export function clearAuth() {

  authHeader = null;

  sessionStorage.removeItem('authHeader');

}



export function isAuthenticated() {

  return authHeader !== null;

}



function handleUnauthorized() {

  clearAuth();

  onUnauthorized?.();

}



async function apiFetch<T>(path: string, options: RequestInit = {}): Promise<T> {

  const headers: Record<string, string> = {

    ...(options.headers as Record<string, string>),

  };



  if (authHeader) {

    headers.Authorization = authHeader;

  }



  if (options.body) {

    headers['Content-Type'] = 'application/json';

  }



  const response = await fetch(`${API_URL}${path}`, { ...options, headers });



  if (response.status === 401) {

    handleUnauthorized();

    throw new Error('UNAUTHORIZED');

  }



  if (!response.ok) {

    const error = await response.json().catch(() => null);

    throw new Error(error?.message || `Erro ${response.status}`);

  }



  return response.json();

}



export async function submitProposal(request: ProposalRequest): Promise<ProposalResponse> {

  return apiFetch('/api/v1/proposals', {

    method: 'POST',

    body: JSON.stringify(request),

  });

}



export async function fetchHistorico(limit = 50): Promise<HistoricoItem[]> {

  return apiFetch(`/api/v1/historico?limit=${limit}`);

}



export async function fetchEmails(limit = 50): Promise<EmailDisparo[]> {

  return apiFetch(`/api/v1/emails?limit=${limit}`);

}



export async function fetchEmailById(id: string): Promise<EmailDisparo> {

  return apiFetch(`/api/v1/emails/${id}`);

}



export async function fetchPropostas(limit = 50): Promise<PropostaSummary[]> {

  return apiFetch(`/api/v1/proposals?limit=${limit}`);

}



export async function fetchExecution(proposalId: string): Promise<ExecutionFlow> {

  return apiFetch(`/api/v1/proposals/${proposalId}/execution`);

}



export async function fetchQueueStatus(): Promise<QueueStatus> {

  return apiFetch('/api/v1/queues/status');

}



export async function fetchEvents(limit = 30): Promise<HistoricoItem[]> {

  return apiFetch(`/api/v1/events?limit=${limit}`);

}



export async function validateSession(): Promise<boolean> {
  loadAuthFromSession();
  if (!isAuthenticated()) {
    return false;
  }

  try {
    await fetchQueueStatus();
    return true;
  } catch {
    clearAuth();
    return false;
  }
}

export async function validateLogin(username: string, password: string): Promise<boolean> {

  setAuthCredentials(username, password);

  try {

    await fetchQueueStatus();

    return true;

  } catch {

    clearAuth();

    return false;

  }

}


