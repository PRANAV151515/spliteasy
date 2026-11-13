import { useState, useRef, useEffect } from "react";

export default function Select({ value, onChange, options }) {
  const [open, setOpen] = useState(false);
  const ref = useRef(null);

  useEffect(() => {
    const close = (e) => {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false);
    };
    const esc = (e) => e.key === "Escape" && setOpen(false);
    document.addEventListener("mousedown", close);
    document.addEventListener("keydown", esc);
    return () => {
      document.removeEventListener("mousedown", close);
      document.removeEventListener("keydown", esc);
    };
  }, []);

  const selected = options.find((o) => String(o.value) === String(value));

  return (
    <div className="dd" ref={ref}>
      <button type="button" className="dd-btn" onClick={() => setOpen(!open)} aria-haspopup="listbox" aria-expanded={open}>
        <span>{selected ? selected.label : "Select"}</span>
        <svg className={`dd-arrow ${open ? "open" : ""}`} viewBox="0 0 20 20" width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M5 7.5l5 5 5-5" />
        </svg>
      </button>
      {open && (
        <ul className="dd-menu" role="listbox">
          {options.map((o) => (
            <li
              key={o.value}
              role="option"
              aria-selected={String(o.value) === String(value)}
              className={`dd-item ${String(o.value) === String(value) ? "active" : ""}`}
              onClick={() => { onChange(o.value); setOpen(false); }}
            >
              {o.label}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}