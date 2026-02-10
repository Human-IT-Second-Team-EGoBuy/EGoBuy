export default function Dropdown({ items = [], selectTeb }) {
  return (
    <div className="absolute top-full left-0 pt-2 w-64 z-50"> 
      <ul className="bg-white border border-slate-200 rounded-xl shadow-lg p-2">
        {items.map((item) => (
          <li key={item.path}>
            <button
              type="button"
              className="w-full text-left px-3 py-2 rounded-lg hover:bg-slate-100 text-slate-700"
              onClick={() => selectTeb(item.path)}
            >
              {item.name}
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
}