import { Navigate, Route, Routes } from 'react-router-dom';
import ProtectedRoute from './ProtectedRoute';
import LoginPage from '../pages/auth/LoginPage';
import RegisterPage from '../pages/auth/RegisterPage';
import StudentLayout from '../layouts/StudentLayout';
import AdminLayout from '../layouts/AdminLayout';
import DashboardPage from '../pages/student/DashboardPage';
import CataloguePage from '../pages/student/CataloguePage';
import SchedulePage from '../pages/student/SchedulePage';
import DegreeAuditPage from '../pages/student/DegreeAuditPage';
import AdminDashboardPage from '../pages/admin/AdminDashboardPage';
import UsersPage from '../pages/admin/UsersPage';
import UserDetailPage from '../pages/admin/UserDetailPage';
import AdminCoursesPage from '../pages/admin/AdminCoursesPage';
import CourseFormPage from '../pages/admin/CourseFormPage';
import AdminEnrollmentsPage from '../pages/admin/AdminEnrollmentsPage';
import AuditLogPage from '../pages/admin/AuditLogPage';
import useAuthStore from '../store/authStore';

const RootRedirect = () => {
  const { isAuthenticated, user } = useAuthStore();
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return <Navigate to={user?.role === 'ADMIN' ? '/admin' : '/dashboard'} replace />;
};

const AppRouter = () => {
  return (
    <Routes>
      <Route path="/" element={<RootRedirect />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route element={<ProtectedRoute role="STUDENT" />}>
        <Route element={<StudentLayout />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/catalogue" element={<CataloguePage />} />
          <Route path="/schedule" element={<SchedulePage />} />
          <Route path="/degree-audit" element={<DegreeAuditPage />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute role="ADMIN" />}>
        <Route element={<AdminLayout />}>
          <Route path="/admin" element={<AdminDashboardPage />} />
          <Route path="/admin/users" element={<UsersPage />} />
          <Route path="/admin/users/:id" element={<UserDetailPage />} />
          <Route path="/admin/courses" element={<AdminCoursesPage />} />
          <Route path="/admin/courses/new" element={<CourseFormPage />} />
          <Route path="/admin/courses/:id" element={<CourseFormPage />} />
          <Route path="/admin/enrollments" element={<AdminEnrollmentsPage />} />
          <Route path="/admin/audit" element={<AuditLogPage />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

export default AppRouter;
