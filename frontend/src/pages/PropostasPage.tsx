import { useEffect, useState } from 'react';
import { fetchPropostas } from '../services/api';
import type { PropostaSummary } from '../types/proposal';

export default function PropostasPage() {
  const [propostas, setPropostas] = useState<PropostaSummary[]>([]);

  useEffect(() => {
    fetchPropostas(50).then(setPropostas);
    const interval = setInterval(() => fetchPropostas(50).then(setPropostas), 3000);
    return () => clearInterval(interval);
  }, []);

  return (
    <section className="rounded-2xl border border-slate-800 bg-slate-900 p-6">
      <h2 className="mb-4 text-lg font-semibold text-white">Propostas Persistidas</h2>
      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm">
          <thead className="text-slate-400">
            <tr>
              <th className="pb-2">ID</th>
              <th className="pb-2">Oferta</th>
              <th className="pb-2">Status</th>
              <th className="pb-2">Conta</th>
              <th className="pb-2">Criado em</th>
            </tr>
          </thead>
          <tbody>
            {propostas.map((p) => (
              <tr key={p.id} className="border-t border-slate-800 text-slate-300">
                <td className="py-2 font-mono text-xs">{p.id.substring(0, 8)}...</td>
                <td className="py-2">{p.tipoOferta}</td>
                <td className={`py-2 ${p.status === 'APPROVED' ? 'text-emerald-400' : 'text-red-400'}`}>{p.status}</td>
                <td className="py-2">{p.accountId || '-'}</td>
                <td className="py-2">{new Date(p.criadoEm).toLocaleString('pt-BR')}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
