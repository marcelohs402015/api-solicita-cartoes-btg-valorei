import type { BenefitType } from '../types/proposal';

interface BenefitBadgesProps {
  benefits: BenefitType[];
  emptyLabel?: string;
}

export default function BenefitBadges({ benefits, emptyLabel = 'Nenhum' }: BenefitBadgesProps) {
  if (benefits.length === 0) {
    return <span className="text-sm text-slate-500">{emptyLabel}</span>;
  }

  return (
    <div className="flex flex-wrap gap-1.5">
      {benefits.map((benefit) => (
        <span
          key={benefit}
          className="rounded-full border border-emerald-800/60 bg-emerald-950/50 px-2 py-0.5 text-xs font-medium text-emerald-300"
        >
          {benefit}
        </span>
      ))}
    </div>
  );
}
