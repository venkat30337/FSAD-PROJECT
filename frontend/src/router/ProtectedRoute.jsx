import { Navigate, Outlet } from 'react-router-dom';
import useAuthStore from '../store/authStore';

const ProtectedRoute = ({ role }) => {
  const { isAuthenticated, user } = useAuthStore();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (role && user?.role !== role) {
    return <Navigate to={user?.role === 'ADMIN' ? '/admin' : '/dashboard'} replace />;
  }

  return <Outlet />;
};

export default ProtectedRoute;
