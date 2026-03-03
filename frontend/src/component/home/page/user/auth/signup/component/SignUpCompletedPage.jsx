import React from 'react';
import { Check } from 'lucide-react';

export default function SignUpCompletedPage({ userName }) {
  return (
    <div className="text-center py-8 animate-in zoom-in-95 duration-700">
      <div className="w-20 h-20 bg-emerald-100 text-emerald-600 rounded-full flex justify-center items-center mx-auto mb-6">
        <Check size={40} strokeWidth={3} />
      </div>
      <h2 className="text-3xl font-extrabold text-slate-800 mb-2">가입 완료!</h2>
      <p className="text-slate-600 mb-10">
        <span className="font-bold text-emerald-600">{userName || '회원'}</span>님, MateFarm의 회원이 되신 것을 환영합니다.<br />
        이제 다양한 영농 서비스를 이용해보세요.
      </p>
      <button
        className="w-full py-4 bg-slate-900 text-white rounded-xl font-bold shadow-lg hover:bg-slate-800 transition-all"
        onClick={() => (window.location.href = '/login')}
      >
        로그인하러 가기
      </button>
    </div>
  );
}