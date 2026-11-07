import { useEffect, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import api from "../api/client";
import Navbar from "../components/Navbar";

export default function GroupsPage() {
  const [groups, setGroups] = useState([]);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const loadGroups = async () => {
    try {
      const res = await api.get("/groups");
      setGroups(res.data);
    } catch (err) {
      if (err.response?.status === 401 || err.response?.status === 403) {
        localStorage.clear();
        navigate("/login");
      } else {
        setError("Unable to load your groups.");
      }
    }
  };

  useEffect(() => {
    loadGroups();
  }, []);

  const createGroup = async (e) => {
    e.preventDefault();
    setError("");
    try {
      await api.post("/groups", { name, description });
      setName("");
      setDescription("");
      loadGroups();
    } catch (err) {
      setError(err.response?.data?.error || "Unable to create the group.");
    }
  };

  return (
    <>
      <Navbar />
      <div className="container">
        <div className="page-head">
          <h1>Hello, {localStorage.getItem("name")} 👋</h1>
          <p>Manage your groups and shared expenses.</p>
        </div>

        {error && <div className="alert">{error}</div>}

        <div className="card" style={{ marginBottom: 32 }}>
          <h3>Create a new group</h3>
          <form className="form" onSubmit={createGroup}>
            <div className="row">
              <input placeholder="Group name (e.g. Goa Trip)" value={name} onChange={(e) => setName(e.target.value)} required />
              <input placeholder="Description (optional)" value={description} onChange={(e) => setDescription(e.target.value)} />
              <button className="btn btn-primary" type="submit" style={{ flex: "0 0 auto" }}>Create group</button>
            </div>
          </form>
        </div>

        <h3 style={{ marginBottom: 16 }}>Your groups</h3>
        {groups.length === 0 ? (
          <div className="card empty">You don't have any groups yet. Create your first one above.</div>
        ) : (
          <div className="groups-grid">
            {groups.map((g) => (
              <Link key={g.id} to={`/groups/${g.id}`} className="card group-card">
                <div className="group-icon">{g.name.charAt(0).toUpperCase()}</div>
                <h3>{g.name}</h3>
                <p>{g.description}</p>
                <div className="group-meta">👥 {g.members.length} {g.members.length === 1 ? "member" : "members"}</div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </>
  );
}
