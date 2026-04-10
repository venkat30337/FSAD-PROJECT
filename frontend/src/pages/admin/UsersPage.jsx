import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { adminUsersApi, toggleUserActiveApi } from '../../api/adminApi';
import Spinner from '../../components/common/Spinner';

const UsersPage = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [users, setUsers] = useState([]);
  const [filters, setFilters] = useState({ role: '', department: '', active: '' });

  const loadUsers = async () => {
    setLoading(true);
    setError('');
    try {
      const params = {
        role: filters.role || undefined,
        department: filters.department || undefined,
        active: filters.active === '' ? undefined : filters.active,
        page: 0,
        size: 100,
      };
      const { data } = await adminUsersApi(params);
      setUsers(data.content || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load users.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUsers();
  }, [filters]);

  const onToggle = async (event, userId) => {
    event.stopPropagation();
    await toggleUserActiveApi(userId);
    await loadUsers();
  };

  if (loading) return <Spinner />;

  return (
    <div className="grid" style={{ gap: 14 }}>
      <h1 className="page-title">Users</h1>
      {error ? <div className="toast toast-error">{error}</div> : null}

      <div className="card" style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
        <select className="select" style={{ maxWidth: 180 }} value={filters.role} onChange={(e) => setFilters((prev) => ({ ...prev, role: e.target.value }))}>
          <option value="">All Roles</option>
          <option value="STUDENT">Student</option>
          <option value="ADMIN">Admin</option>
        </select>
        <input className="input" style={{ maxWidth: 240 }} placeholder="Department" value={filters.department} onChange={(e) => setFilters((prev) => ({ ...prev, department: e.target.value }))} />
        <select className="select" style={{ maxWidth: 200 }} value={filters.active} onChange={(e) => setFilters((prev) => ({ ...prev, active: e.target.value }))}>
          <option value="">All Statuses</option>
          <option value="true">Active</option>
          <option value="false">Inactive</option>
        </select>
      </div>

      <section className="card">
        <table className="table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Student ID</th>
              <th>Department</th>
              <th>Semester</th>
              <th>Credits Used</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id} onClick={() => navigate(`/admin/users/${user.id}`)} style={{ cursor: 'pointer' }}>
                <td>{user.fullName}</td>
                <td>{user.email}</td>
                <td>{user.studentId || '-'}</td>
                <td>{user.department || '-'}</td>
                <td>{user.semester || '-'}</td>
                <td>{user.usedCredits || 0}</td>
                <td>{user.isActive ? 'Active' : 'Inactive'}</td>
                <td>
                  <button className="btn btn-secondary" onClick={(event) => onToggle(event, user.id)}>
                    {user.isActive ? 'Deactivate' : 'Activate'}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  );
};

export default UsersPage;
