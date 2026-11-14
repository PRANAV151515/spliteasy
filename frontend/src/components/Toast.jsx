import { useEffect } from "react";

export default function Toast({ toast, onClose }) {
  useEffect(() => {
    if (!toast) return;
    const t = setTimeout(onClose, 4500);
    return () => clearTimeout(t);
  }, [toast, onClose]);

  if (!toast) return null;

  const isError = toast.type === "error";

  return (
    <div className="toast-wrap">
      <div className={`toast ${isError ? "toast-error" : "toast-success"}`} role="alert">
        <div className="toast-icon">{isError ? "!" : "✓"}</div>
        <div className="toast-body">
          <div className="toast-title">{toast.title}</div>
          {toast.message && <div className="toast-msg">{toast.message}</div>}
        </div>
        <button type="button" className="toast-close" onClick={onClose} aria-label="Close">×</button>
      </div>
    </div>
  );
}

