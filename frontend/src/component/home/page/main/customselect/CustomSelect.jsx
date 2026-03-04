import { Listbox, ListboxButton, ListboxOption, ListboxOptions } from '@headlessui/react';
import { ChevronDownIcon, CheckIcon } from '@heroicons/react/20/solid';

export default function CustomSelect({ label, value, option, onChange, disabled }) {

    const arrOption = Array.isArray(option) ? option : [];

  return (
    <div className="select-container">
      <Listbox value={value} onChange={onChange} disabled={disabled}>
        {({ open }) => (
          <div className="relative">
            {/* 버튼 부분: 상태에 따라 클래스 동적 결합 */}
            <ListboxButton className={`
              select-btn 
              ${open ? 'select-btn-active' : ''} 
              ${disabled ? 'select-btn-disabled' : ''}
            `}>
              <span className="block truncate text-slate-700">{value || label}</span>
              <span className="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2">
                <ChevronDownIcon className="h-5 w-5 text-slate-400" aria-hidden="true" />
              </span>
            </ListboxButton>

            {/* 드롭다운 메뉴 부분 */}
            <ListboxOptions className="select-options-panel">
              {arrOption.map((name, idx) => (
                <ListboxOption
                  key={idx}
                  value={name}
                  className={({ active, selected }) => `
                    select-option-item
                    ${active ? 'select-option-active' : ''}
                    ${selected ? 'select-option-selected' : ''}
                  `}
                >
                  {({ selected }) => (
                    <>
                      <span className="block truncate">{name}</span>
                      {selected && (
                        <span className="select-check-icon">
                          <CheckIcon className="h-4 w-4" aria-hidden="true" />
                        </span>
                      )}
                    </>
                  )}
                </ListboxOption>
              ))}
            </ListboxOptions>
          </div>
        )}
      </Listbox>
    </div>
  );
}