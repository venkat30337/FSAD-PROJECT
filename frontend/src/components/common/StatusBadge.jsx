const StatusBadge = ({ status }) => {
  const value = String(status || '').toUpperCase();
  const className =
    value === 'PUBLISHED' || value === 'ENROLLED' || value === 'COMPLETED'
      ? 'badge badge-success'
      : value === 'DRAFT' || value === 'WAITLISTED'
        ? 'badge badge-warning'
        : value === 'DROPPED'
          ? 'badge badge-danger'
          : 'badge badge-muted';

  return <span className={className}>{value || 'N/A'}</span>;
};

export default StatusBadge;
