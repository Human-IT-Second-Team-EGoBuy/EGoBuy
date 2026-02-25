import { CheckCircle, AlertCircle } from 'lucide-react';

function StatusBadge({ isValid, onCheck, loading }) {
  if (isValid === true) {
    return (
      <span className="flex items-center gap-1 text-emerald-500 text-xs font-bold p-3 bg-emerald-50 rounded-xl border border-emerald-100 whitespace-nowrap">
        <CheckCircle size={14} /> 사용가능
      </span>
    );
  }
  if (isValid === false) {
    return (
      <button
        type="button"
        onClick={onCheck}
        disabled={loading}
        className="flex items-center gap-1 text-red-500 text-xs font-bold bg-red-50 px-3 py-3 rounded-xl border border-red-100 hover:bg-red-100 transition-colors whitespace-nowrap disabled:opacity-50"
      >
        <AlertCircle size={14} /> 중복
      </button>
    );
  }
  return (
    <button
      type="button"
      onClick={onCheck}
      disabled={loading}
      className="text-xs font-bold text-slate-600 bg-slate-100 px-4 py-3 rounded-xl hover:bg-slate-200 border border-slate-200 transition-colors whitespace-nowrap disabled:opacity-50"
    >
      {loading ? '확인 중...' : '중복확인'}
    </button>
  );
}

export default StatusBadge;