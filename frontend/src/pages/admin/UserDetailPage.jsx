import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { adminForceDropApi, adminUserDetailApi } from '../../api/adminApi';
import Spinner from '../../components/common/Spinner';
import StatusBadge from '../../components/common/StatusBadge';
import { formatTime } from '../../utils/timeUtils';

const UserDetailPage = () => {
  const { id } = useParams();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [data, setData] = useState(null);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await adminUserDetailApi(id);
      setData(response.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Unable to load user detail.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, [id]);

  const onForceDrop = async (enrollmentId) => {
    await adminForceDropApi(enrollmentId);
    await load();
  };

  if (loading) return <Spinner />;
  if (error) return <div className="toast toast-error">{error}</div>;

  const user = data.user;

  return (
    <div className="grid" style={{ gap: 16 }}>
      <h1 className="page-title">User Detail</h1>
      <section className="card grid grid-3">
        <div><span className="label">Name</span>{user.fullName}</div>
        <div><span className="label">Email</span>{user.email}</div>
        <div><span className="label">Role</span>{user.role}</div>
        <div><span className="label">Student ID</span>{user.studentId || '-'}</div>
        <div><span className="label">Department</span>{user.department || '-'}</div>
        <div><span className="label">Semester</span>{user.semester || '-'}</div>
      </section>

      <section className="card">
        <h2 className="section-title">Enrollments</h2>
        <table className="table">
          <thead>
            <tr>
              <th>Course</th>
              <th>Section</th>
              <th>Days</th>
              <th>Time</th>
              <th>Status</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {(data.enrollments || []).map((enrollment) => (
              <tr key={enrollment.id}>
                <td>{enrollment.section.courseCode}</td>
                <td>{enrollment.section.sectionCode}</td>
                <td>{enrollment.section.daysOfWeek}</td>
                <td>{formatTime(enrollment.section.startTime)} - {formatTime(enrollment.section.endTime)}</td>
                <td><StatusBadge status={enrollment.status} /></td>
                <td>
                  {enrollment.status !== 'DROPPED' ? (
                    <button className="btn btn-danger" onClick={() => onForceDrop(enrollment.id)}>Force Drop</button>
                  ) : '-'}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  );
};

export default UserDetailPage;
