import React from 'react';

export default function InputRow({ 
  icon, 
  label, 
  type = 'text', 
  placeholder, 
  name, 
  value, 
  onChange 
}) {
  return (
    <div className="flex flex-col space-y-1.5 w-full">
      {/* 라벨: 가독성을 위해 약간 더 어두운 slate-600 사용 */}
      <label className="text-xs font-bold text-slate-600 ml-1">
        {label}
      </label>
      
      <div className="relative group">
        {/* 아이콘 영역 */}
        <div className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 group-focus-within:text-emerald-500 transition-colors">
          {icon}
        </div>
        
        {/* 입력창: text-slate-900을 추가하여 입력 내용이 검은색 계열로 보이게 함 */}
        <input
          name={name}
          type={type}
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          required
          className="w-full pl-11 pr-4 py-3.5 bg-white border border-slate-200 rounded-xl 
                     text-slate-900 placeholder:text-slate-400
                     focus:outline-none focus:ring-4 focus:ring-emerald-500/10 focus:border-emerald-500 
                     transition-all shadow-sm"
        />
      </div>
    </div>
  );
}