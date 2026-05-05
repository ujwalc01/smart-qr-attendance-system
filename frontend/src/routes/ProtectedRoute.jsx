import { useAuth } from '../auth/AuthContext.jsx';
import LoginPage from '../pages/login/LoginPage.jsx';

export default function ProtectedRoute({ role, children }) {
  const { user } = useAuth();
  if (!user) {
    return <LoginPage />;
  }
  if (role && user.role !== role) {
    return <main className="shell"><div className="notice error">You are not allowed to view this page.</div></main>;
  }
  return children;
}
