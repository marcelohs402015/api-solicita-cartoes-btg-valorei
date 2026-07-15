import { useState } from 'react';
import { submitProposal, fetchExecution } from '../services/api';
import type { BenefitType, ExecutionFlow, OfferType, ProposalResponse } from '../types/proposal';

const BENEFITS: BenefitType[] = ['CASHBACK', 'SEGURO_VIAGEM', 'SALA_VIP', 'PONTOS'];
const OFFERS: OfferType[] = ['A', 'B', 'C'];

export default function ProposalForm() {
  const [renda, setRenda] = useState('5000');
  const [investimentos, setInvestimentos] = useState('1000');
  const [tempoContaAnos, setTempoContaAnos] = useState('1');
  const [tipoOferta, setTipoOferta] = useState<OfferType>('A');
  const [beneficios, setBeneficios] = useState<BenefitType[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<ProposalResponse | null>(null);
  const [execution, setExecution] = useState<ExecutionFlow | null>(null);

  function toggleBenefit(benefit: BenefitType) {
    setBeneficios((current) =>
      current.includes(benefit) ? current.filter((b) => b !== benefit) : [...current, benefit],
    );
  }

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    setLoading(true);
    setError(null);
    setResult(null);
    setExecution(null);

    try {
      const response = await submitProposal({
        renda: Number(renda),
        investimentos: Number(investimentos),
        tempoContaAnos: Number(tempoContaAnos),
        tipoOferta,
        beneficios,
      });
      setResult(response);
      setTimeout(async () => {
        const flow = await fetchExecution(response.proposalId);
        setExecution(flow);
      }, 1500);
    } catch (err) {
      if (err instanceof Error && err.message === 'UNAUTHORIZED') {
        window.location.href = '/login';
        return;
      }
      setError(err instanceof Error ? err.message : 'Erro ao enviar proposta');
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="rounded-2xl border border-slate-800 bg-slate-900 p-6 shadow-xl">
      <h2 className="mb-6 text-lg font-semibold text-white">Nova Proposta</h2>
      <form onSubmit={handleSubmit} className="space-y-5">
        <div className="grid gap-4 sm:grid-cols-2">
          <label className="block">
            <span className="mb-1 block text-sm text-slate-400">Renda (R$)</span>
            <input type="number" step="0.01" min="0" value={renda} onChange={(e) => setRenda(e.target.value)} className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-white" required />
          </label>
          <label className="block">
            <span className="mb-1 block text-sm text-slate-400">Investimentos (R$)</span>
            <input type="number" step="0.01" min="0" value={investimentos} onChange={(e) => setInvestimentos(e.target.value)} className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-white" required />
          </label>
        </div>
        <div className="grid gap-4 sm:grid-cols-2">
          <label className="block">
            <span className="mb-1 block text-sm text-slate-400">Tempo de Conta (anos)</span>
            <input type="number" min="0" value={tempoContaAnos} onChange={(e) => setTempoContaAnos(e.target.value)} className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-white" required />
          </label>
          <label className="block">
            <span className="mb-1 block text-sm text-slate-400">Tipo de Oferta</span>
            <select value={tipoOferta} onChange={(e) => setTipoOferta(e.target.value as OfferType)} className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-white">
              {OFFERS.map((o) => <option key={o} value={o}>Oferta {o}</option>)}
            </select>
          </label>
        </div>
        <fieldset>
          <legend className="mb-2 text-sm text-slate-400">Beneficios</legend>
          <div className="grid grid-cols-2 gap-2">
            {BENEFITS.map((b) => (
              <label key={b} className="flex cursor-pointer items-center gap-2 rounded-lg border border-slate-800 px-3 py-2">
                <input type="checkbox" checked={beneficios.includes(b)} onChange={() => toggleBenefit(b)} className="accent-blue-500" />
                <span className="text-sm">{b}</span>
              </label>
            ))}
          </div>
        </fieldset>
        <button type="submit" disabled={loading} className="w-full rounded-lg bg-blue-600 py-3 font-medium text-white hover:bg-blue-500 disabled:opacity-60">
          {loading ? 'Processando...' : 'Submeter Proposta'}
        </button>
      </form>
      {error && <p className="mt-4 text-sm text-red-400">{error}</p>}
      {result && (
        <div className={`mt-4 rounded-lg border p-4 ${result.status === 'APPROVED' ? 'border-emerald-800 bg-emerald-950/40' : 'border-red-800 bg-red-950/40'}`}>
          <p className="font-semibold">Status: {result.status}</p>
          <p className="text-sm text-slate-300">ID: {result.proposalId}</p>
          {result.motivosRejeicao.map((m) => <p key={m} className="text-sm text-red-300">{m}</p>)}
        </div>
      )}
      {execution && (
        <div className="mt-4 rounded-lg border border-slate-800 bg-slate-950 p-4">
          <h3 className="mb-3 font-medium text-white">Execucao do fluxo</h3>
          <div className="space-y-2">
            {execution.steps.map((step) => (
              <div key={step.step} className="flex items-center justify-between text-sm">
                <span className="text-slate-300">{step.step}</span>
                <span className={step.status === 'DONE' ? 'text-emerald-400' : 'text-amber-400'}>{step.status}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </section>
  );
}
