import StatusBadge from '../common/StatusBadge';

const CourseCard = ({ course, onEnroll, enrollmentState }) => {
  const totalSeats = course.sections?.reduce((sum, section) => sum + section.maxSeats, 0) || 0;
  const filledSeats = course.sections?.reduce((sum, section) => sum + section.enrolledCount, 0) || 0;
  const percent = totalSeats ? Math.min(100, Math.round((filledSeats / totalSeats) * 100)) : 0;

  return (
    <article className="card" style={{ display: 'grid', gap: 10 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', gap: 12 }}>
        <div>
          <div style={{ fontSize: 12, color: 'var(--color-text-muted)', fontWeight: 700 }}>{course.code}</div>
          <h3 style={{ margin: '4px 0 0', fontSize: 18 }}>{course.title}</h3>
        </div>
        <StatusBadge status={course.status} />
      </div>

      <div style={{ color: 'var(--color-text-muted)', minHeight: 44 }}>{course.description}</div>

      <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
        <span className="badge badge-muted">{course.department}</span>
        <span className="badge badge-info">{course.credits} Credits</span>
        <span className="badge badge-muted">{course.instructor}</span>
      </div>

      <div>
        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 12, marginBottom: 4 }}>
          <span>Seat occupancy</span>
          <span>{filledSeats}/{totalSeats || 0}</span>
        </div>
        <div style={{ height: 8, background: '#e5e7eb', borderRadius: 999 }}>
          <div style={{ height: '100%', width: `${percent}%`, borderRadius: 999, background: percent > 90 ? '#b45309' : '#1a56db' }} />
        </div>
      </div>

      <button
        className={`btn ${enrollmentState === 'ENROLLED' ? 'btn-secondary' : enrollmentState === 'WAITLISTED' ? 'btn-secondary' : 'btn-primary'}`}
        disabled={enrollmentState === 'ENROLLED'}
        onClick={onEnroll}
      >
        {enrollmentState === 'ENROLLED' ? 'Enrolled' : enrollmentState === 'WAITLISTED' ? 'Waitlisted' : 'Enroll'}
      </button>
    </article>
  );
};

export default CourseCard;
