import { Outlet } from 'react-router-dom';
import Navbar from '../components/common/Navbar';

const StudentLayout = () => {
  return (
    <>
      <Navbar />
      <main className="page-shell">
        <Outlet />
      </main>
    </>
  );
};

export default StudentLayout;
