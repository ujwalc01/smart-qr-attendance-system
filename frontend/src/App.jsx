import { useAuth } from "./auth/AuthContext.jsx";
import Header from "./components/Header.jsx";
import LoginPage from "./pages/login/LoginPage.jsx";
import AdminDashboard from "./pages/admin/AdminDashboard.jsx";
import FacultyDashboard from "./pages/faculty/FacultyDashboard.jsx";
import StudentDashboard from "./pages/student/StudentDashboard.jsx";

export default function App() {
  const { user } = useAuth();

  let content = <LoginPage />;
  if (user?.role === "ADMIN") content = <AdminDashboard />;
  if (user?.role === "FACULTY") content = <FacultyDashboard />;
  if (user?.role === "STUDENT") content = <StudentDashboard />;

  return (
    <>
      <Header />
      {content}
    </>
  );
}
