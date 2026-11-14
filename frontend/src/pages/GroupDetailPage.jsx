import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import api from "../api/client";
import Navbar from "../components/Navbar";
import Select from "../components/Select";
import Toast from "../components/Toast";

const SPLIT_LABELS = { EQUAL: "Equal", EXACT: "Exact amounts", PERCENTAGE: "Percentage" };
const money = (n) => Number(n).toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 });


export default function GroupDetailPage() {
  const { id } = useParams();
  const [group, setGroup] = useState(null);
  const [expenses, setExpenses] = useState([]);
  const [balances, setBalances] = useState([]);
  const [settlements, setSettlements] = useState([]);
  const [memberEmail, setMemberEmail] = useState("");
  const [error, setError] = useState("");
  const [toast, setToast] = useState(null);
  const [payments, setPayments] = useState([]);
  const [me, setMe] = useState(null);
  const [settlingKey, setSettlingKey] = useState(null);

  const [desc, setDesc] = useState("");
  const [amount, setAmount] = useState("");
  const [paidBy, setPaidBy] = useState("");
  const [splitType, setSplitType] = useState("EQUAL");
  const [splitValues, setSplitValues] = useState({});

  const loadAll = async () => {
    try {
        const [g, e, b, s, p, u] = await Promise.all([
        api.get(`/groups/${id}`),
        api.get(`/groups/${id}/expenses`),
        api.get(`/groups/${id}/balances`),
        api.get(`/groups/${id}/settlements`),
        api.get(`/groups/${id}/payments`),
        api.get(`/users/me`),
      ]);
      setGroup(g.data);
      setExpenses(e.data);
      setBalances(b.data);
      setSettlements(s.data);
      setPayments(p.data);
      setMe(u.data);
      
      if (!paidBy && g.data.members.length > 0) setPaidBy(String(g.data.members[0].id));
    } catch (err) {
      setError(err.response?.data?.error || "Unable to load group data.");
    }
  };

  useEffect(() => {
    loadAll();
  }, [id]);

    const addMember = async (e) => {
    e.preventDefault();
    setError("");
    try {
      await api.post(`/groups/${id}/members`, { email: memberEmail });
      setToast({ type: "success", title: "Member added", message: `${memberEmail} joined the group.` });
      setMemberEmail("");
      loadAll();
    } catch (err) {
      const msg = err.response?.data?.error || "Unable to add the member.";
      setToast({
        type: "error",
        title: msg.includes("No registered user") ? "User not found" : "Couldn't add member",
        message: msg,
      });
    }
  };


  const addExpense = async (e) => {
    e.preventDefault();
    setError("");
    try {
      const body = { description: desc, amount: Number(amount), paidBy: Number(paidBy), splitType };
      if (splitType !== "EQUAL") {
        body.splits = Object.entries(splitValues)
          .filter(([, v]) => v !== "" && Number(v) > 0)
          .map(([userId, v]) => ({ userId: Number(userId), value: Number(v) }));
      }
      await api.post(`/groups/${id}/expenses`, body);
      setDesc("");
      setAmount("");
      setSplitValues({});
      loadAll();
    } catch (err) {
      setError(err.response?.data?.error || "Unable to add the expense.");
    }
  };

  const markReceived = async (t) => {
  const key = `${t.from.id}-${t.to.id}`;
  setSettlingKey(key);
  try {
    await api.post(`/groups/${id}/payments`, { fromUserId: t.from.id, amount: t.amount });
    setToast({ type: "success", title: "Payment recorded", message: `${t.from.name} paid ${t.to.name} ₹${money(t.amount)}.` });
    loadAll();
  } catch (err) {
    setToast({ type: "error", title: "Couldn't record payment", message: err.response?.data?.error || "Something went wrong." });
  } finally {
    setSettlingKey(null);
  }
};

  if (!group) {
    return (
      <>
        <Navbar />
        
        <div className="container">{error ? <div className="alert">{error}</div> : <p className="empty">Loading...</p>}</div>
      </>
    );
  }

  return (
    <>
      <Navbar />
      <Toast toast={toast} onClose={() => setToast(null)} />
      <div className="container">
        <Link to="/groups" className="back-link">← Back to groups</Link>
        <div className="page-head">
          <h1>{group.name}</h1>
          {group.description && <p>{group.description}</p>}
        </div>

        {error && <div className="alert">{error}</div>}

        <div className="grid-2">
          {/* LEFT COLUMN */}
          <div className="stack">
            <div className="card">
              <h3>Add an expense</h3>
              <form className="form" onSubmit={addExpense}>
                <div className="row">
                  <input placeholder="What was it for?" value={desc} onChange={(e) => setDesc(e.target.value)} required />
                  <input type="number" step="0.01" min="0.01" placeholder="Amount (₹)" value={amount} onChange={(e) => setAmount(e.target.value)} required />
                </div>
                <div className="row">
                  
                  <div className="field">Paid by
                  <Select
                      value={paidBy}
                      onChange={setPaidBy}
                      options={group.members.map((m) => ({ value: String(m.id), label: m.name }))}
                    />
                  </div>
                  <div className="field">Split type
                    <Select
                      value={splitType}
                      onChange={setSplitType}
                      options={Object.entries(SPLIT_LABELS).map(([k, v]) => ({ value: k, label: v }))}
                    />
                  </div>
                </div>

                {splitType !== "EQUAL" && (
                  <div className="form" style={{ gap: 8 }}>
                    <span className="hint">
                      {splitType === "EXACT" ? "Enter each person's share. The total must equal the amount." : "Enter each person's percentage. The total must equal 100."}
                    </span>
                    {group.members.map((m) => (
                      <div key={m.id} className="split-row">
                        
                        <span>{m.name}</span>
                        <input type="number" step="0.01" placeholder={splitType === "EXACT" ? "₹ 0.00" : "0 %"}
                          value={splitValues[m.id] || ""}
                          onChange={(e) => setSplitValues({ ...splitValues, [m.id]: e.target.value })} />
                      </div>
                    ))}
                  </div>
                )}
                <button className="btn btn-primary" type="submit">Add expense</button>
              </form>
            </div>

            <div className="card">
              <div className="card-title-row">
                <h3>Expenses</h3>
                <span className="badge">{expenses.length}</span>
              </div>
              {expenses.length === 0 && payments.length === 0 ?  (
                <p className="empty">No expenses yet. Add your first one above.</p>
              ) : (
                <ul className="list">
                    {payments.map((p) => (
                    <li key={`pay-${p.id}`} className="list-item">
                      <div className="grow">
                        <div className="item-title">{p.from.name} paid {p.to.name}</div>
                        <div className="item-sub">Settlement</div>
                      </div>
                      <span className="badge badge-green">Settled ₹{money(p.amount)}</span>
                    </li>
                  ))}
                  {expenses.map((x) => (
                    <li key={x.id} className="list-item">
                      
                      <div className="grow">
                        <div className="item-title">{x.description}</div>
                        <div className="item-sub">Paid by {x.paidBy.name} · {SPLIT_LABELS[x.splitType]}</div>
                      </div>
                      <span className="amount">₹{money(x.amount)}</span>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>

          {/* RIGHT COLUMN */}
          <div className="stack">
            <div className="card">
              <h3>Balances</h3>
              <ul className="list">
                {balances.map((b) => {
                  const value = Number(b.balance);
                  return (
                    <li key={b.user.id} className="list-item">
                      
                      <div className="grow">
                        <div className="item-title">{b.user.name}</div>
                        <div className="item-sub">{value > 0 ? "gets back" : value < 0 ? "owes" : "settled up"}</div>
                      </div>
                      <span className={`amount ${value > 0 ? "pos" : value < 0 ? "neg" : ""}`}>₹{money(Math.abs(value))}</span>
                    </li>
                  );
                })}
              </ul>
            </div>

            <div className="card">
              <h3>Settle up</h3>
              {settlements.length === 0 ? (
                <p className="empty">🎉 Everyone is settled up.</p>
              ) : (
                <ul className="list">
                  {settlements.map((t) => {
                    const key = `${t.from.id}-${t.to.id}`;
                    const canMark = me && me.id === t.to.id;
                    return (
                      <li key={key} className="list-item settle-item">
                        <div className="grow">
                          <div className="transfer">
                            <strong>{t.from.name}</strong>
                            <span className="arrow">pays</span>
                            <strong>{t.to.name}</strong>
                          </div>
                          <span className="badge badge-green">₹{money(t.amount)}</span>
                        </div>
                        {canMark && (
                          <button className="btn btn-primary btn-sm" disabled={settlingKey === key} onClick={() => markReceived(t)}>
                            {settlingKey === key ? "Saving..." : "Mark as received"}
                          </button>
                        )}
                      </li>
                    );
                  })}
                </ul>
              )}
              <p className="hint" style={{ marginTop: 12 }}>Only the person receiving the money can mark a payment as received.</p>
            </div>

            <div className="card">
              <h3>Members</h3>
              <ul className="list" style={{ marginBottom: 16 }}>
                {group.members.map((m) => (
                  <li key={m.id} className="list-item">
                    
                    <div className="grow">
                      <div className="item-title">{m.name}</div>
                      <div className="item-sub">{m.email}</div>
                    </div>
                  </li>
                ))}
              </ul>
              <form className="form" onSubmit={addMember}>
                <input type="email" placeholder="Friend's email address" value={memberEmail} onChange={(e) => setMemberEmail(e.target.value)} required />
                <button className="btn btn-primary" type="submit">Add member</button>
              </form>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}

