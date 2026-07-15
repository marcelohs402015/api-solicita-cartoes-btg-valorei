import { useEffect, useState } from 'react';
import { fetchHistorico } from '../services/api';
import BenefitBadges from '../components/BenefitBadges';
import { extractBenefitsFromPayload, extractOfferFromPayload } from '../utils/proposalDisplay';
import type { HistoricoItem } from '../types/proposal';

export default function HistoricoPage() {
  const [items, setItems] = useState<HistoricoItem[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function load() {
      try {
        setItems(await fetchHistorico(50));
        setError(null);
      } catch (err) {
        if (err instanceof Error && err.message !== 'UNAUTHORIZED') {
          setError(err.message);
        }
      }
    }

    load();
    const interval = setInterval(load, 3000);
    return () => clearInterval(interval);
  }, []);

  return (
    <section className="rounded-2xl border border-slate-800 bg-slate-900 p-6">
      <h2 className="mb-4 text-lg font-semibold text-white">Historico de Auditoria</h2>
      {error && <p className="text-red-400">{error}</p>}
      <div className="max-h-[700px] space-y-3 overflow-y-auto">
        {items.map((item) => {
          const offer = extractOfferFromPayload(item.payload);
          const benefits = extractBenefitsFromPayload(item.payload);

          return (
            <article key={item.id} className="rounded-xl border border-slate-800 bg-slate-950 p-4 text-sm">
              <div className="flex justify-between">
                <span className="font-medium text-blue-300">{item.evento}</span>
                <time className="text-slate-500">{new Date(item.criadoEm).toLocaleString('pt-BR')}</time>
              </div>
              <p className="mt-1 text-slate-400">Proposta: {item.propostaId}</p>
              <p className="text-slate-500">Status: {item.status}</p>
              {offer && <p className="mt-2 text-slate-400">Oferta: <span className="text-white">Oferta {offer}</span></p>}
              {benefits.length > 0 && (
                <div className="mt-2">
                  <p className="mb-1 text-slate-400">Beneficios no evento:</p>
                  <BenefitBadges benefits={benefits} />
                </div>
              )}
            </article>
          );
        })}
      </div>
    </section>
  );
}
