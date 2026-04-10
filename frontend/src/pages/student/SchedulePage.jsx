import { useEffect, useState } from 'react';
import { dropEnrollmentApi, myEnrollmentsApi, myScheduleApi } from '../../api/enrollmentApi';
import WeeklyTimetable from '../../components/schedule/WeeklyTimetable';
import Spinner from '../../components/common/Spinner';
import StatusBadge from '../../components/common/StatusBadge';
import { formatTime } from '../../utils/timeUtils';

const SchedulePage = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [toast, setToast] = useState('');
  const [schedule, setSchedule] = useState([]);
  const [enrollments, setEnrollments] = useState([]);

  const loadData = async () => {
    setLoading(true);
    setError('');
    try {
      const [{ data: scheduleData }, { data: enrollmentData }] = await Promise.all([
        myScheduleApi(),
        myEnrollmentsApi(),
      ]);
      setSchedule(scheduleData || []);
      setEnrollments(enrollmentData || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load schedule.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const onDrop = async (enrollment) => {
    const confirmed = window.confirm(`Are you sure you want to drop ${enrollment.section.courseTitle}?`);
    if (!confirmed) return;

    try {
      await dropEnrollmentApi(enrollment.id);
      setToast('Enrollment dropped successfully.');
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || 'Unable to drop enrollment.');
    }
  };

  if (loading) return <Spinner />;

  return (
    <div className="grid" style={{ gap: 18 }}>
      <h1 className="page-title">Weekly Schedule</h1>
      {toast ? <div className="toast toast-success">{toast}</div> : null}
      {error ? <div className="toast toast-error">{error}</div> : null}

      <WeeklyTimetable sections={schedule} />

      <section className="card">
        <h2 className="section-title">My Enrollments</h2>
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
            {enrollments.map((enrollment) => (
              <tr key={enrollment.id}>
                <td>{enrollment.section.courseCode} - {enrollment.section.courseTitle}</td>
                <td>{enrollment.section.sectionCode}</td>
                <td>{enrollment.section.daysOfWeek}</td>
                <td>{formatTime(enrollment.section.startTime)} - {formatTime(enrollment.section.endTime)}</td>
                <td><StatusBadge status={enrollment.status} /></td>
                <td>
                  {enrollment.status !== 'DROPPED' ? (
                    <button className="btn btn-danger" onClick={() => onDrop(enrollment)}>Drop</button>
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

export default SchedulePage;
