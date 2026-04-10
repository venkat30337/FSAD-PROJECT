import { useEffect, useState } from 'react';
import { adminAuditLogsApi } from '../../api/adminApi';
import Spinner from '../../components/common/Spinner';
import { formatDateTime } from '../../utils/timeUtils';

const AuditLogPage = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filters, setFilters] = useState({ action: '', startDate: '', endDate: '' });
  const [logs, setLogs] = useState([]);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const params = {
        action: filters.action || undefined,
        startDate: filters.startDate || undefined,
        endDate: filters.endDate || undefined,
        page: 0,
        size: 100,
      };
      const { data } = await adminAuditLogsApi(params);
      setLogs(data.content || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load audit logs.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, [filters]);

  if (loading) return <Spinner />;

  return (
    <div className="grid" style={{ gap: 14 }}>
      <h1 className="page-title">Audit Logs</h1>
      {error ? <div className="toast toast-error">{error}</div> : null}

      <div className="card" style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
        <input className="input" style={{ maxWidth: 220 }} placeholder="Action contains" value={filters.action} onChange={(e) => setFilters((prev) => ({ ...prev, action: e.target.value }))} />
        <input className="input" style={{ maxWidth: 200 }} type="date" value={filters.startDate} onChange={(e) => setFilters((prev) => ({ ...prev, startDate: e.target.value }))} />
        <input className="input" style={{ maxWidth: 200 }} type="date" value={filters.endDate} onChange={(e) => setFilters((prev) => ({ ...prev, endDate: e.target.value }))} />
      </div>

      <section className="card">
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
            {logs.map((log) => (
              <tr key={log.id}>
                <td>{formatDateTime(log.createdAt)}</td>
                <td>{log.adminName}</td>
                <td>{log.action}</td>
                <td>{log.entityType} {log.entityId || '-'}</td>
                <td>{log.detail}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  );
};

export default AuditLogPage;
