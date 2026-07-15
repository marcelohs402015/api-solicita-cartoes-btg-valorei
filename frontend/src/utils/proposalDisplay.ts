import type { BenefitType } from '../types/proposal';

export function formatCurrency(value: number): string {
  return value.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}

export function formatBenefits(benefits: BenefitType[] | undefined | null): string {
  if (!benefits || benefits.length === 0) {
    return 'Nenhum';
  }
  return benefits.join(', ');
}

export function extractBenefitsFromPayload(payload: Record<string, unknown>): BenefitType[] {
  const raw = payload.beneficios;
  if (!Array.isArray(raw)) {
    return [];
  }
  return raw.filter((item): item is BenefitType => typeof item === 'string');
}

export function extractOfferFromPayload(payload: Record<string, unknown>): string | null {
  const raw = payload.tipoOferta;
  return typeof raw === 'string' ? raw : null;
}
