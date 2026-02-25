import React from 'react';

export default function StepIndicator({ currentStep, totalSteps }) {
  return (
    <div className="flex justify-center gap-2 mb-10">
      {Array.from({ length: totalSteps }, (_, i) => i + 1).map((i) => (
        <div
          key={i}
          className={`h-1.5 rounded-full transition-all duration-500 ${
            currentStep >= i ? 'w-8 bg-emerald-500' : 'w-2 bg-slate-200'
          }`}
        />
      ))}
    </div>
  );
}