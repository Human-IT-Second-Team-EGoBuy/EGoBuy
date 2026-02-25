import React from 'react';
import { Check, ChevronRight } from 'lucide-react';

export default function TermsRow({ label, checked, onChange, isDetail }) {
  return (
    <div className="flex justify-between items-center px-2">
      <div
        className="flex items-center gap-3 cursor-pointer group"
        onClick={() => onChange(!checked)}
      >
        <div
          className={`w-5 h-5 rounded-md flex justify-center items-center text-white transition-colors ${
            checked ? 'bg-emerald-500' : 'bg-slate-200 group-hover:bg-slate-300'
          }`}
        >
          {checked && <Check size={12} strokeWidth={4} />}
        </div>
        <span className="text-[15px] text-slate-700">{label}</span>
      </div>
      {isDetail && <ChevronRight size={18} className="text-slate-400" />}
    </div>
  );
}