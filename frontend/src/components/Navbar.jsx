import { Link, useNavigate } from "react-router-dom";

export default function Navbar() {
  const navigate = useNavigate();
  const name = localStorage.getItem("name");

  const logout = () => {
    localStorage.clear();
    navigate("/login");
  };

  return (
    <header className="navbar">
      <div className="navbar-inner">
        <Link to="/groups" className="brand">
          <span className="brand-logo">₹</span>SplitEasy
        </Link>
        <div className="nav-right">
          {name && (
            <>
              
              <span className="nav-name">{name}</span>
            </>
          )}
          <button className="btn btn-ghost" onClick={logout}>Log out</button>
        </div>
      </div>
    </header>
  );
}
