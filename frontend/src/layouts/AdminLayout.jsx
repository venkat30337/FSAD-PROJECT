import { Outlet } from 'react-router-dom';
import Navbar from '../components/common/Navbar';
import Sidebar from '../components/common/Sidebar';

const AdminLayout = () => {
  return (
    <>
      <Navbar admin />
      <div className="layout-admin">
        <Sidebar />
        <main className="main-area page-shell">
          <Outlet />
        </main>
      </div>
    </>
  );
};

export default AdminLayout;
