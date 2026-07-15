import { useEffect, useState } from 'react';
import { fetchHistorico } from '../services/api';
import type { HistoricoItem } from '../types/proposal';

export default function HistoricoPage() {
  const [items, setItems] = useState<HistoricoItem[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchHistorico(50).then(setItems).catch((e) => setError(e.message));
    const interval = setInterval(() => fetchHistorico(50).then(setItems), 3000);
    return () => clearInterval(interval);
  }, []);

  return (
    <section className="rounded-2xl border border-slate-800 bg-slate-900 p-6">
      <h2 className="mb-4 text-lg font-semibold text-white">Historico de Auditoria</h2>
      {error && <p className="text-red-400">{error}</p>}
      <div className="max-h-[700px] space-y-3 overflow-y-auto">
        {items.map((item) => (
          <article key={item.id} className="rounded-xl border border-slate-800 bg-slate-950 p-4 text-sm">
            <div className="flex justify-between">
              <span className="font-medium text-blue-300">{item.evento}</span>
              <time className="text-slate-500">{new Date(item.criadoEm).toLocaleString('pt-BR')}</time>
            </div>
            <p className="mt-1 text-slate-400">Proposta: {item.propostaId}</p>
            <p className="text-slate-500">Status: {item.status}</p>
          </article>
        ))}
      </div>
    </section>
  );
}
