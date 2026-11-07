import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/client";

export default function AuthPage() {
  const [isLogin, setIsLogin] = useState(true);
  const [form, setForm] = useState({ name: "", email: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const url = isLogin ? "/auth/login" : "/auth/register";
      const body = isLogin ? { email: form.email, password: form.password } : form;
      const res = await api.post(url, body);
      localStorage.setItem("token", res.data.token);
      localStorage.setItem("name", res.data.name);
      navigate("/groups");
    } catch (err) {
      setError(err.response?.data?.error || "Something went wrong. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-wrap">
      <div className="auth-hero">
        <div className="brand"><span className="brand-logo">₹</span>SplitEasy</div>
        <h1>Split expenses.<br />Not friendships.</h1>
        <p>Track shared costs with friends, roommates and travel groups, and settle up with the fewest possible payments.</p>
        <ul className="features">
          <li>Equal, exact and percentage splits</li>
          <li>Smart debt simplification</li>
          <li>Secure JWT authentication</li>
        </ul>
      </div>

      <div className="auth-panel">
        <div className="auth-form">
          <h2>{isLogin ? "Welcome back" : "Create your account"}</h2>
          <p className="sub">{isLogin ? "Sign in to continue to SplitEasy." : "It only takes a few seconds."}</p>

          {error && <div className="alert">{error}</div>}

          <form className="form" onSubmit={handleSubmit}>
            {!isLogin && (
              <label className="field">Full name
                <input name="name" placeholder="John Doe" value={form.name} onChange={handleChange} required />
              </label>
            )}
            <label className="field">Email address
              <input name="email" type="email" placeholder="you@example.com" value={form.email} onChange={handleChange} required />
            </label>
            <label className="field">Password
              <input name="password" type="password" placeholder="At least 6 characters" value={form.password} onChange={handleChange} required />
            </label>
            <button className="btn btn-primary btn-block" type="submit" disabled={loading}>
              {loading ? "Please wait..." : isLogin ? "Sign in" : "Create account"}
            </button>
          </form>

          <div className="switch">
            {isLogin ? "Don't have an account? " : "Already have an account? "}
            <button onClick={() => { setIsLogin(!isLogin); setError(""); }}>
              {isLogin ? "Sign up" : "Sign in"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

