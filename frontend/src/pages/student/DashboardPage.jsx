import { useEffect, useMemo, useState } from 'react';
import { getPublicCoursesApi } from '../../api/coursesApi';
import { myEnrollmentsApi, myScheduleApi } from '../../api/enrollmentApi';
import useAuthStore from '../../store/authStore';
import useNotificationStore from '../../store/notificationStore';
import Spinner from '../../components/common/Spinner';

const DashboardPage = () => {
  const { user } = useAuthStore();
  const { unreadCount } = useNotificationStore();

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [enrollments, setEnrollments] = useState([]);
  const [schedule, setSchedule] = useState([]);
  const [latestCourses, setLatestCourses] = useState([]);

  const stats = useMemo(() => {
    const enrolled = enrollments.filter((item) => item.status === 'ENROLLED');
    const waitlisted = enrollments.filter((item) => item.status === 'WAITLISTED');
    const creditsUsed = enrolled.reduce((sum, item) => sum + (item.section?.courseCredits || 0), 0);
    return {
      enrolled: enrolled.length,
      waitlisted: waitlisted.length,
      creditsUsed,
      maxCredits: user?.maxCredits || 24,
    };
  }, [enrollments, user?.maxCredits]);

  const loadDashboard = async () => {
    setLoading(true);
    setError('');
    try {
      const [{ data: enrollmentData }, { data: scheduleData }, { data: coursesData }] = await Promise.all([
        myEnrollmentsApi(),
        myScheduleApi(),
        getPublicCoursesApi({ page: 0, size: 4, sort: 'id,desc' }),
      ]);

      setEnrollments(enrollmentData || []);
      setSchedule(scheduleData || []);
      setLatestCourses(coursesData.content || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load dashboard data.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboard();
  }, []);

  if (loading) {
    return <Spinner />;
  }

  if (error) {
    return <div className="toast toast-error">{error}</div>;
  }

  return (
    <div className="grid" style={{ gap: 20 }}>
      <section className="home-intro">
        <div className="home-intro-content">
          <div className="label home-intro-label">Project Title</div>
          <h1 className="home-intro-title">CourseFlow Student Workspace</h1>
          <p className="home-intro-text">
            Manage your semester with live course visibility, conflict-safe enrollment checks, and a degree progress tracker designed for fast planning.
          </p>
          <div className="home-intro-chips">
            <span className="home-intro-chip">Conflict Detection</span>
            <span className="home-intro-chip">Smart Waitlist</span>
            <span className="home-intro-chip">Degree Audit</span>
          </div>
        </div>

        <aside className="home-intro-side">
          <div className="home-intro-side-item">
            <span>Student</span>
            <strong>{user?.fullName}</strong>
          </div>
          <div className="home-intro-side-item">
            <span>Department</span>
            <strong>{user?.department || '-'}</strong>
          </div>
          <div className="home-intro-side-item">
            <span>Semester</span>
            <strong>{user?.semester || '-'}</strong>
          </div>
          <div className="home-intro-side-item">
            <span>Credits Used</span>
            <strong>{stats.creditsUsed}/{stats.maxCredits}</strong>
          </div>
        </aside>
      </section>

      <section className="grid grid-4">
        <div className="card"><div className="label">Enrolled Courses</div><div style={{ fontSize: 30, fontWeight: 800 }}>{stats.enrolled}</div></div>
        <div className="card"><div className="label">Waitlisted</div><div style={{ fontSize: 30, fontWeight: 800 }}>{stats.waitlisted}</div></div>
        <div className="card"><div className="label">Credits Used</div><div style={{ fontSize: 30, fontWeight: 800 }}>{stats.creditsUsed}</div></div>
        <div className="card"><div className="label">Unread Notifications</div><div style={{ fontSize: 30, fontWeight: 800 }}>{unreadCount}</div></div>
      </section>

      <section className="card">
        <h2 className="section-title">My This Week</h2>
        {schedule.length === 0 ? (
          <p style={{ color: 'var(--color-text-muted)' }}>No active enrolled sections.</p>
        ) : (
          <div className="grid grid-3">
            {schedule.slice(0, 3).map((item) => (
              <div key={item.id} className="card" style={{ borderStyle: 'dashed' }}>
                <strong>{item.courseCode}</strong>
                <div>{item.daysOfWeek}</div>
                <div>{item.startTime} - {item.endTime}</div>
                <div>{item.room || 'Room TBD'}</div>
              </div>
            ))}
          </div>
        )}
      </section>

      <section className="card">
        <h2 className="section-title">Recently Added Courses</h2>
        <div className="grid grid-2">
          {latestCourses.map((course) => (
            <div key={course.id} className="card" style={{ borderStyle: 'dashed' }}>
              <strong>{course.code}</strong>
              <div>{course.title}</div>
              <small style={{ color: 'var(--color-text-muted)' }}>{course.department}</small>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
};

export default DashboardPage;
