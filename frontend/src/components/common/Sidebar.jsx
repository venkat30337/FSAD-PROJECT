import { NavLink } from 'react-router-dom';

const Sidebar = () => {
  return (
    <aside className="sidebar">
      <NavLink to="/admin" end className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
        Dashboard
      </NavLink>
      <NavLink to="/admin/users" className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
        Users
      </NavLink>
      <NavLink to="/admin/courses" className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
        Courses
      </NavLink>
      <NavLink to="/admin/enrollments" className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
        Conflicts
      </NavLink>
      <NavLink to="/admin/audit" className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
        Audit Logs
      </NavLink>
    </aside>
  );
};

export default Sidebar;
