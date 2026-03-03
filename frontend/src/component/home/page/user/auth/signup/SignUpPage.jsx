import React, { useState } from 'react';
import SignupTerms from '@/component/home/page/user/auth/signup/component/SignupTermsPage';
import SignupInfo from '@/component/home/page/user/auth/signup/component/SignUpInfoPage';
import SignupComplete from '@/component/home/page/user/auth/signup/component/SignUpCompletedPage';
import StepIndicator from '@/component/home/page/user/auth/signup/common/StepIndicator';

export default function SignupPage() {
  const [step, setStep] = useState(1);
  const [totalData, setTotalData] = useState({});

  const nextStep = (data) => {
    setTotalData((prev) => ({ ...prev, ...data }));
    setStep((prev) => prev + 1);
  };

  const prevStep = () => {
    setStep((prev) => prev - 1);
  };

  return (
    <div className="flex justify-center items-center min-h-screen bg-slate-50 p-5 font-sans">
      <div className="w-full max-w-lg bg-white p-10 rounded-3xl shadow-xl border border-slate-100">
        
        <StepIndicator currentStep={step} totalSteps={3} />

        {step === 1 && (
          <SignupTerms 
            onNext={nextStep} 
            onCancel={() => window.history.back()} 
          />
        )}

        {step === 2 && (
          <SignupInfo 
            onNext={nextStep} 
            onBack={prevStep} 
          />
        )}

        {step === 3 && (
          <SignupComplete 
            userName={totalData.userName} 
          />
        )}
      </div>
    </div>
  );
}