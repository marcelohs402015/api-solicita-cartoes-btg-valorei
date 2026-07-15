export type OfferType = 'A' | 'B' | 'C';

export type BenefitType = 'CASHBACK' | 'SEGURO_VIAGEM' | 'SALA_VIP' | 'PONTOS';

export type ProposalStatus = 'APPROVED' | 'REJECTED';

export type EmailStatus = 'GERADO' | 'DISPARADO';

export interface ProposalRequest {
  renda: number;
  investimentos: number;
  tempoContaAnos: number;
  tipoOferta: OfferType;
  beneficios: BenefitType[];
}

export interface CardAccount {
  created: boolean;
  accountId: string | null;
}

export interface ProposalResponse {
  proposalId: string;
  status: ProposalStatus;
  motivosRejeicao: string[];
  cardAccount: CardAccount;
  activatedBenefits: BenefitType[];
}

export interface PropostaSummary {
  id: string;
  renda: number;
  investimentos: number;
  tempoContaAnos: number;
  tipoOferta: OfferType;
  beneficios: BenefitType[];
  status: ProposalStatus;
  motivosRejeicao: string[];
  accountId: string | null;
  criadoEm: string;
}

export interface HistoricoItem {
  id: string;
  propostaId: string;
  evento: string;
  status: ProposalStatus;
  payload: Record<string, unknown>;
  criadoEm: string;
}

export interface EmailDisparo {
  id: string;
  propostaId: string;
  destinatario: string;
  assunto: string;
  templateJson: Record<string, unknown>;
  status: EmailStatus;
  criadoEm: string;
}

export interface QueueStatus {
  topic: string;
  consumers: { name: string; groupId: string; status: string }[];
  recentMessagesCount: number;
}

export interface ExecutionStep {
  step: string;
  status: string;
  detail: string;
}

export interface ExecutionFlow {
  proposalId: string;
  steps: ExecutionStep[];
}
