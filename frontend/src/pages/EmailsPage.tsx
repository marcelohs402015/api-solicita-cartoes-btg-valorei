import { useEffect, useState } from 'react';
import { fetchEmails, fetchEmailById } from '../services/api';
import BenefitBadges from '../components/BenefitBadges';
import { extractBenefitsFromPayload, extractOfferFromPayload } from '../utils/proposalDisplay';
import type { BenefitType, EmailDisparo } from '../types/proposal';

function benefitsFromTemplate(template: Record<string, unknown>): BenefitType[] {
  return extractBenefitsFromPayload(template);
}

function offerFromTemplate(template: Record<string, unknown>): string | null {
  return extractOfferFromPayload(template);
}

export default function EmailsPage() {
  const [emails, setEmails] = useState<EmailDisparo[]>([]);
  const [selected, setSelected] = useState<EmailDisparo | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function load() {
      try {
        setEmails(await fetchEmails(50));
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

  async function openEmail(id: string) {
    try {
      const email = await fetchEmailById(id);
      setSelected(email);
      setError(null);
    } catch (err) {
      if (err instanceof Error && err.message !== 'UNAUTHORIZED') {
        setError(err.message);
      }
    }
  }

  return (
    <section className="rounded-2xl border border-slate-800 bg-slate-900 p-6">
      <h2 className="mb-4 text-lg font-semibold text-white">E-mails Gerados</h2>
      {error && <p className="text-red-400">{error}</p>}
      <div className="grid gap-6 lg:grid-cols-2">
        <div className="space-y-3">
          {emails.map((email) => {
            const benefits = benefitsFromTemplate(email.templateJson);
            const offer = offerFromTemplate(email.templateJson);

            return (
              <article key={email.id} className="rounded-xl border border-slate-800 bg-slate-950 p-4">
                <p className="font-medium text-white">{email.assunto}</p>
                <p className="text-sm text-slate-400">Para: {email.destinatario}</p>
                <p className="text-xs text-slate-500">Proposta: {email.propostaId}</p>
                {offer && <p className="mt-1 text-sm text-slate-400">Oferta {offer}</p>}
                {benefits.length > 0 && (
                  <div className="mt-2">
                    <p className="mb-1 text-xs text-slate-500">Beneficios aprovados:</p>
                    <BenefitBadges benefits={benefits} />
                  </div>
                )}
                <p className="mt-2 text-xs text-slate-500">{new Date(email.criadoEm).toLocaleString('pt-BR')}</p>
                <button onClick={() => openEmail(email.id)} className="mt-2 text-sm text-blue-400 hover:underline">
                  Ver detalhes
                </button>
              </article>
            );
          })}
          {emails.length === 0 && <p className="text-slate-500">Nenhum email gerado ainda. Aprove uma proposta.</p>}
        </div>
        <div className="rounded-xl border border-slate-800 bg-slate-950 p-4">
          <h3 className="mb-2 font-medium text-white">Preview do e-mail</h3>
          {selected ? (
            <div className="space-y-3 text-sm">
              <p className="text-slate-300">{String(selected.templateJson.mensagem ?? '')}</p>
              {offerFromTemplate(selected.templateJson) && (
                <p className="text-slate-400">
                  Oferta aprovada: <span className="text-white">Oferta {offerFromTemplate(selected.templateJson)}</span>
                </p>
              )}
              <div>
                <p className="mb-1 text-slate-400">Beneficios ativados no cartao:</p>
                <BenefitBadges benefits={benefitsFromTemplate(selected.templateJson)} emptyLabel="Nenhum" />
              </div>
              <details>
                <summary className="cursor-pointer text-blue-400">Ver JSON completo</summary>
                <pre className="mt-2 max-h-[300px] overflow-auto text-xs text-emerald-200">{JSON.stringify(selected.templateJson, null, 2)}</pre>
              </details>
            </div>
          ) : (
            <p className="text-sm text-slate-500">Selecione um e-mail para ver o template.</p>
          )}
        </div>
      </div>
    </section>
  );
}
