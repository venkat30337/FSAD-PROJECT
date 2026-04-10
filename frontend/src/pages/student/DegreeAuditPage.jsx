import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { degreeAuditApi } from '../../api/degreeApi';
import Spinner from '../../components/common/Spinner';

const RequirementTable = ({ title, items }) => {
  return (
    <section className="card">
      <h2 className="section-title">{title}</h2>
      <table className="table">
        <thead>
          <tr>
            <th>Course</th>
            <th>Title</th>
            <th>Status</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {items.map((item) => (
            <tr key={item.requirementId}>
              <td>{item.courseCode}</td>
              <td>{item.courseTitle}</td>
              <td>{item.status}</td>
              <td>
                {item.status === 'NOT ENROLLED' ? (
                  <Link to={`/catalogue?course=${item.courseId}`} style={{ color: 'var(--color-primary-dark)', fontWeight: 600 }}>
                    Enroll now
                  </Link>
                ) : '-'}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
};

const DegreeAuditPage = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [audit, setAudit] = useState(null);

  const progress = useMemo(() => {
    if (!audit || audit.totalRequiredCredits === 0) return 0;
    return Math.min(100, Math.round((audit.completedOrEnrolledCredits / audit.totalRequiredCredits) * 100));
  }, [audit]);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const { data } = await degreeAuditApi();
        setAudit(data);
      } catch (err) {
        setError(err.response?.data?.message || 'Unable to load degree audit.');
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
      <h1 className="page-title">Degree Audit</h1>

      <section className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', gap: 12, alignItems: 'center' }}>
          <div>
            <div className="label">Program</div>
            <h2 style={{ margin: '2px 0 0' }}>{audit.program}</h2>
          </div>
          <div style={{ minWidth: 260 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6 }}>
              <span>{audit.completedOrEnrolledCredits}/{audit.totalRequiredCredits} Credits</span>
              <strong>{progress}%</strong>
            </div>
            <div style={{ height: 10, background: '#e5e7eb', borderRadius: 999 }}>
              <div style={{ width: `${progress}%`, height: '100%', borderRadius: 999, background: 'linear-gradient(90deg, #1a56db, #047857)' }} />
            </div>
          </div>
        </div>
      </section>

      <RequirementTable title="Core Requirements" items={audit.core || []} />
      <RequirementTable title="Electives" items={audit.elective || []} />
      <RequirementTable title="Lab" items={audit.lab || []} />
    </div>
  );
};

export default DegreeAuditPage;
