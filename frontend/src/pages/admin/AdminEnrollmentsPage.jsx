import { useEffect, useState } from 'react';
import { adminConflictsApi, adminForceDropApi } from '../../api/adminApi';
import { formatTime } from '../../utils/timeUtils';
import Spinner from '../../components/common/Spinner';

const AdminEnrollmentsPage = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [items, setItems] = useState([]);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const { data } = await adminConflictsApi({ page: 0, size: 100 });
      setItems(data.content || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load conflicts.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const resolve = async (enrollmentId) => {
    await adminForceDropApi(enrollmentId);
    await load();
  };

  if (loading) return <Spinner />;

  return (
    <div className="grid" style={{ gap: 14 }}>
      <h1 className="page-title">Conflicts Dashboard</h1>
      {error ? <div className="toast toast-error">{error}</div> : null}

      <section className="card">
        <table className="table">
          <thead>
            <tr>
              <th>Student</th>
              <th>Course</th>
              <th>Section</th>
              <th>Overlap Time</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.id}>
                <td>{item.studentName}</td>
                <td>{item.section.courseCode}</td>
                <td>{item.section.sectionCode}</td>
                <td>{item.section.daysOfWeek} | {formatTime(item.section.startTime)} - {formatTime(item.section.endTime)}</td>
                <td>
                  <button className="btn btn-danger" onClick={() => resolve(item.id)}>Resolve (Drop)</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  );
};

export default AdminEnrollmentsPage;
