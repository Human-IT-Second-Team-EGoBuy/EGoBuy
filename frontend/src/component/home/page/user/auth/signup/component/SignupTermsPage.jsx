import React, { useState } from 'react';
import { Check } from 'lucide-react';
import TermsRow from '@/component/home/page/user/auth/signup/common/TermsRow';

export default function SignupTermsPage({ onNext, onCancel }) {
  const [agreements, setAgreements] = useState({
    all: false,
    age: false,
    terms: false,
    privacy: false,
    marketing: false,
  });

  const handleAllAgreement = (checked) => {
    setAgreements({
      all: checked,
      age: checked,
      terms: checked,
      privacy: checked,
      marketing: checked,
    });
  };

  const handleIndividualAgreement = (key, checked) => {
    setAgreements((prev) => {
      const newState = { ...prev, [key]: checked };
      newState.all =
        newState.age &&
        newState.terms &&
        newState.privacy &&
        newState.marketing;
      return newState;
    });
  };

  const isMandatoryAgreed =
    agreements.age && agreements.terms && agreements.privacy;

  return (
    <div className="animate-in fade-in slide-in-from-bottom-4 duration-500">
      <h2 className="text-2xl font-extrabold text-slate-800 mb-2 text-center">
        약관 동의
      </h2>
      <p className="text-slate-500 text-center mb-8 text-sm">
        MateFarm 서비스 이용을 위해 약관에 동의해주세요.
      </p>

      {/* 전체 동의 */}
      <div
        className={`p-5 rounded-xl mb-6 border-2 transition-all cursor-pointer ${
          agreements.all ? 'bg-green-50 border-green-500' : 'bg-slate-100 border-transparent'
        }`}
        onClick={() => handleAllAgreement(!agreements.all)}
      >
        <div className="flex items-center gap-3">
          <div
            className={`w-6 h-6 rounded-md flex justify-center items-center text-white ${
              agreements.all ? 'bg-green-500' : 'bg-slate-300'
            }`}
          >
            {agreements.all && <Check size={16} strokeWidth={3} />}
          </div>
          <span className="font-bold text-lg text-slate-900">전체 동의하기</span>
        </div>
      </div>

      {/* 개별 약관 */}
      <div className="flex flex-col gap-4 mb-8">
        <TermsRow
          label="[필수] 만 14세 이상입니다"
          checked={agreements.age}
          onChange={(c) => handleIndividualAgreement('age', c)}
        />
        <TermsRow
          label="[필수] 서비스 이용약관 동의"
          checked={agreements.terms}
          onChange={(c) => handleIndividualAgreement('terms', c)}
          isDetail
        />
        <TermsRow
          label="[필수] 개인정보 수집 및 이용 동의"
          checked={agreements.privacy}
          onChange={(c) => handleIndividualAgreement('privacy', c)}
          isDetail
        />
        <TermsRow
          label="[선택] 마케팅 목적 이메일 수신 동의"
          checked={agreements.marketing}
          onChange={(c) => handleIndividualAgreement('marketing', c)}
        />
      </div>

      {/* 버튼 */}
      <div className="grid grid-cols-[1fr_2fr] gap-3">
        <button
          className="py-4 rounded-xl border border-slate-200 bg-white text-slate-500 font-semibold hover:bg-slate-50 transition-colors"
          onClick={onCancel}
        >
          취소
        </button>
        <button
          className={`py-4 rounded-xl font-bold transition-all ${
            isMandatoryAgreed
              ? 'bg-emerald-500 text-white shadow-md hover:bg-emerald-600'
              : 'bg-slate-200 text-slate-400 cursor-not-allowed'
          }`}
          disabled={!isMandatoryAgreed}
          onClick={() =>
            onNext({ marketingAgreementYn: agreements.marketing ? 'Y' : 'N' })
          }
        >
          다음 단계
        </button>
      </div>
    </div>
  );
}