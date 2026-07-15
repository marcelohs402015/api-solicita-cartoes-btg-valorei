import { useEffect, useState } from 'react';
import { fetchEmails, fetchEmailById } from '../services/api';
import type { EmailDisparo } from '../types/proposal';

export default function EmailsPage() {
  const [emails, setEmails] = useState<EmailDisparo[]>([]);
  const [selected, setSelected] = useState<EmailDisparo | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchEmails(50).then(setEmails).catch((e) => setError(e.message));
    const interval = setInterval(() => fetchEmails(50).then(setEmails), 3000);
    return () => clearInterval(interval);
  }, []);

  async function openEmail(id: string) {
    const email = await fetchEmailById(id);
    setSelected(email);
  }

  return (
    <section className="rounded-2xl border border-slate-800 bg-slate-900 p-6">
      <h2 className="mb-4 text-lg font-semibold text-white">E-mails Gerados</h2>
      {error && <p className="text-red-400">{error}</p>}
      <div className="grid gap-6 lg:grid-cols-2">
        <div className="space-y-3">
          {emails.map((email) => (
            <article key={email.id} className="rounded-xl border border-slate-800 bg-slate-950 p-4">
              <p className="font-medium text-white">{email.assunto}</p>
              <p className="text-sm text-slate-400">Para: {email.destinatario}</p>
              <p className="text-xs text-slate-500">{new Date(email.criadoEm).toLocaleString('pt-BR')}</p>
              <button onClick={() => openEmail(email.id)} className="mt-2 text-sm text-blue-400 hover:underline">
                Ver template
              </button>
            </article>
          ))}
          {emails.length === 0 && <p className="text-slate-500">Nenhum email gerado ainda. Aprove uma proposta.</p>}
        </div>
        <div className="rounded-xl border border-slate-800 bg-slate-950 p-4">
          <h3 className="mb-2 font-medium text-white">Preview do e-mail</h3>
          {selected ? (
            <pre className="max-h-[500px] overflow-auto text-xs text-emerald-200">{JSON.stringify(selected.templateJson, null, 2)}</pre>
          ) : (
            <p className="text-sm text-slate-500">Selecione um e-mail para ver o template JSON.</p>
          )}
        </div>
      </div>
    </section>
  );
}
