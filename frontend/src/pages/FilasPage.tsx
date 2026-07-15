import { useEffect, useState } from 'react';
import { fetchQueueStatus, fetchEvents } from '../services/api';
import type { HistoricoItem, QueueStatus } from '../types/proposal';

export default function FilasPage() {
  const [status, setStatus] = useState<QueueStatus | null>(null);
  const [events, setEvents] = useState<HistoricoItem[]>([]);

  useEffect(() => {
    async function load() {
      setStatus(await fetchQueueStatus());
      setEvents(await fetchEvents(20));
    }
    load();
    const interval = setInterval(load, 3000);
    return () => clearInterval(interval);
  }, []);

  return (
    <section className="space-y-6">
      <div className="rounded-2xl border border-slate-800 bg-slate-900 p-6">
        <h2 className="mb-4 text-lg font-semibold text-white">Filas Kafka</h2>
        {status && (
          <div className="space-y-3 text-sm">
            <p className="text-slate-300">Topico: <span className="font-mono text-blue-300">{status.topic}</span></p>
            <p className="text-slate-400">Mensagens processadas: {status.recentMessagesCount}</p>
            {status.consumers.map((c) => (
              <div key={c.groupId} className="rounded-lg border border-slate-800 bg-slate-950 p-3">
                <p className="text-white">{c.name}</p>
                <p className="text-slate-500">Group: {c.groupId}</p>
                <p className="text-emerald-400">{c.status}</p>
              </div>
            ))}
            <a href="http://localhost:8090" target="_blank" rel="noreferrer" className="inline-block text-blue-400 hover:underline">
              Abrir Kafka UI
            </a>
          </div>
        )}
      </div>
      <div className="rounded-2xl border border-slate-800 bg-slate-900 p-6">
        <h3 className="mb-3 font-medium text-white">Ultimos eventos do topico</h3>
        <div className="space-y-2">
          {events.map((e) => (
            <div key={e.id} className="rounded-lg border border-slate-800 bg-slate-950 p-3 text-sm">
              <p className="text-slate-300">{e.evento} — {e.status}</p>
              <p className="text-slate-500">{e.propostaId}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
