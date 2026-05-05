import { useAuth } from '../auth/AuthContext.jsx';

export default function Header() {
  const { user, logout } = useAuth();
  return (
    <header className="topbar">
      <div>
        <strong>QR Smart Attendance</strong>
        {user && <span className="muted"> {user.role} · {user.email}</span>}
      </div>
      {user && <button onClick={logout}>Logout</button>}
    </header>
  );
}
