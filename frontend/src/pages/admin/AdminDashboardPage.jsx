import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { adminDashboardApi } from '../../api/adminApi';
import Spinner from '../../components/common/Spinner';
import StatsCard from '../../components/admin/StatsCard';
import { formatDateTime } from '../../utils/timeUtils';

const AdminDashboardPage = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [data, setData] = useState(null);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const response = await adminDashboardApi();
        setData(response.data);
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load dashboard');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  if (loading) return <Spinner />;
  if (error) return <div className="toast toast-error">{error}</div>;

  return (
    <div className="grid" style={{ gap: 16 }}>
      <section className="home-intro home-intro-admin">
        <div className="home-intro-content">
          <div className="label home-intro-label">Project Title</div>
          <h1 className="home-intro-title">CourseFlow Admin Control Center</h1>
          <p className="home-intro-text">
            Oversee catalog quality, monitor enrollment velocity, and manage student outcomes through one operational dashboard.
          </p>
          <div className="home-intro-chips">
            <span className="home-intro-chip">Enrollment Analytics</span>
            <span className="home-intro-chip">User Management</span>
            <span className="home-intro-chip">Audit Tracking</span>
          </div>
        </div>

        <aside className="home-intro-side">
          <div className="home-intro-side-item">
            <span>Total Students</span>
            <strong>{data.totalStudents}</strong>
          </div>
          <div className="home-intro-side-item">
            <span>Published Courses</span>
            <strong>{data.publishedCourses}</strong>
          </div>
          <div className="home-intro-side-item">
            <span>Active Enrollments</span>
            <strong>{data.activeEnrollments}</strong>
          </div>
          <div className="home-intro-side-item">
            <span>Waitlisted</span>
            <strong>{data.waitlistedStudents}</strong>
          </div>
        </aside>
      </section>

      <section className="grid grid-3">
        <StatsCard label="Total Students" value={data.totalStudents} />
        <StatsCard label="Active Courses" value={data.publishedCourses} />
        <StatsCard label="Total Enrollments" value={data.activeEnrollments} />
        <StatsCard label="Waitlisted" value={data.waitlistedStudents} />
        <StatsCard label="Draft Courses" value={data.draftCourses} />
        <StatsCard label="Audit Events" value={data.recentActivity?.length || 0} />
      </section>

      <section className="card">
        <h2 className="section-title">Top 5 Courses by Enrollment</h2>
        <div style={{ width: '100%', height: 320 }}>
          <ResponsiveContainer>
            <BarChart data={data.topCoursesByEnrollment || []}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="courseCode" />
              <YAxis />
              <Tooltip />
              <Bar dataKey="enrollmentCount" fill="#1a56db" radius={[8, 8, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </section>

      <section className="card">
        <h2 className="section-title">Recent Activity</h2>
        <table className="table">
          <thead>
            <tr>
              <th>Timestamp</th>
              <th>Admin</th>
              <th>Action</th>
              <th>Entity</th>
              <th>Detail</th>
            </tr>
          </thead>
          <tbody>
            {(data.recentActivity || []).map((item) => (
              <tr key={item.id}>
                <td>{formatDateTime(item.createdAt)}</td>
                <td>{item.admin}</td>
                <td>{item.action}</td>
                <td>{item.entityType} {item.entityId || '-'}</td>
                <td>{item.detail}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section style={{ display: 'flex', gap: 10 }}>
        <Link to="/admin/courses" className="btn btn-primary">Manage Courses</Link>
        <Link to="/admin/users" className="btn btn-secondary">Manage Users</Link>
        <Link to="/admin/enrollments" className="btn btn-secondary">View Conflicts</Link>
      </section>
    </div>
  );
};

export default AdminDashboardPage;
