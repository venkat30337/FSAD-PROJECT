import { NavLink } from 'react-router-dom';
import NotificationBell from '../notifications/NotificationBell';
import useAuthStore from '../../store/authStore';

const Navbar = ({ admin = false }) => {
  const { user, logout } = useAuthStore();

  return (
    <header className="navbar">
      <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
        <NavLink to={admin ? '/admin' : '/dashboard'} className="brand">
          CourseFlow
        </NavLink>
        {!admin ? (
          <>
            <NavLink to="/catalogue" style={{ fontWeight: 600 }}>Catalogue</NavLink>
            <NavLink to="/schedule" style={{ fontWeight: 600 }}>Schedule</NavLink>
            <NavLink to="/degree-audit" style={{ fontWeight: 600 }}>Degree Audit</NavLink>
          </>
        ) : null}
      </div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
        <NotificationBell />
        <div style={{ fontWeight: 600 }}>{user?.fullName}</div>
        <button className="btn btn-secondary" onClick={logout}>Logout</button>
      </div>
    </header>
  );
};

export default Navbar;
