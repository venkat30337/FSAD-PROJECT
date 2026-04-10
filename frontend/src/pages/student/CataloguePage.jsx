import { useEffect, useMemo, useState } from 'react';
import { getCoursesApi } from '../../api/coursesApi';
import { checkConflictApi, enrollApi, myEnrollmentsApi } from '../../api/enrollmentApi';
import CourseCard from '../../components/courses/CourseCard';
import ConflictCheckModal from '../../components/enrollment/ConflictCheckModal';
import Spinner from '../../components/common/Spinner';

const departments = ['Computer Science', 'Electronics', 'Mechanical', 'Civil', 'Information Technology', 'Mathematics'];

const CataloguePage = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [courses, setCourses] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [filters, setFilters] = useState({ department: '', search: '', credits: '' });
  const [enrollments, setEnrollments] = useState([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [modalChecking, setModalChecking] = useState(false);
  const [modalError, setModalError] = useState('');
  const [modalResult, setModalResult] = useState(null);
  const [selectedSectionId, setSelectedSectionId] = useState(null);
  const [toast, setToast] = useState('');

  const enrollmentByCourse = useMemo(() => {
    const map = new Map();
    enrollments.forEach((enrollment) => {
      map.set(enrollment.section.courseId, enrollment.status);
    });
    return map;
  }, [enrollments]);

  const loadCourses = async () => {
    setLoading(true);
    setError('');
    try {
      const params = {
        page,
        size: 9,
        status: 'PUBLISHED',
        search: filters.search || undefined,
        department: filters.department || undefined,
        credits: filters.credits || undefined,
      };
      const [{ data }, { data: myData }] = await Promise.all([getCoursesApi(params), myEnrollmentsApi()]);
      setCourses(data.content || []);
      setTotalPages(Math.max(1, data.totalPages || 1));
      setEnrollments(myData || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load catalogue.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCourses();
  }, [page, filters]);

  const startEnroll = async (course) => {
    const section = course.sections?.[0];
    if (!section) {
      setToast('No sections available for this course.');
      return;
    }

    setModalOpen(true);
    setModalChecking(true);
    setModalError('');
    setModalResult(null);
    setSelectedSectionId(section.id);

    try {
      const { data } = await checkConflictApi(section.id);
      setModalResult(data);
    } catch (err) {
      setModalError(err.response?.data?.message || 'Unable to check conflict.');
    } finally {
      setModalChecking(false);
    }
  };

  const confirmEnroll = async () => {
    if (!selectedSectionId) return;
    try {
      await enrollApi(selectedSectionId);
      setToast('Enrollment successful.');
      setModalOpen(false);
      setSelectedSectionId(null);
      await loadCourses();
    } catch (err) {
      setModalError(err.response?.data?.message || 'Enrollment failed.');
    }
  };

  if (loading) return <Spinner />;

  return (
    <div className="grid" style={{ gap: 18 }}>
      <h1 className="page-title">Course Catalogue</h1>

      {toast ? <div className="toast toast-success">{toast}</div> : null}
      {error ? <div className="toast toast-error">{error}</div> : null}

      <div className="grid" style={{ gridTemplateColumns: '260px 1fr', gap: 16 }}>
        <aside className="card" style={{ alignSelf: 'start' }}>
          <h3 className="section-title">Filters</h3>
          <div style={{ marginBottom: 10 }}>
            <label className="label">Department</label>
            <select className="select" value={filters.department} onChange={(e) => setPage(0) || setFilters((prev) => ({ ...prev, department: e.target.value }))}>
              <option value="">All</option>
              {departments.map((dept) => (
                <option key={dept} value={dept}>{dept}</option>
              ))}
            </select>
          </div>
          <div style={{ marginBottom: 10 }}>
            <label className="label">Credits</label>
            <select className="select" value={filters.credits} onChange={(e) => setPage(0) || setFilters((prev) => ({ ...prev, credits: e.target.value }))}>
              <option value="">All</option>
              {[1, 2, 3, 4, 5, 6].map((credit) => (
                <option key={credit} value={credit}>{credit}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="label">Search</label>
            <input className="input" value={filters.search} onChange={(e) => setPage(0) || setFilters((prev) => ({ ...prev, search: e.target.value }))} placeholder="Code or title" />
          </div>
        </aside>

        <section>
          <div className="grid grid-3">
            {courses.map((course) => (
              <CourseCard
                key={course.id}
                course={course}
                onEnroll={() => startEnroll(course)}
                enrollmentState={enrollmentByCourse.get(course.id)}
              />
            ))}
          </div>

          <div style={{ display: 'flex', gap: 8, justifyContent: 'center', marginTop: 14 }}>
            <button className="btn btn-secondary" disabled={page === 0} onClick={() => setPage((prev) => Math.max(0, prev - 1))}>Prev</button>
            <span style={{ alignSelf: 'center' }}>Page {page + 1} / {totalPages}</span>
            <button className="btn btn-secondary" disabled={page + 1 >= totalPages} onClick={() => setPage((prev) => prev + 1)}>Next</button>
          </div>
        </section>
      </div>

      <ConflictCheckModal
        open={modalOpen}
        checking={modalChecking}
        result={modalResult}
        error={modalError}
        onConfirm={confirmEnroll}
        onClose={() => setModalOpen(false)}
      />
    </div>
  );
};

export default CataloguePage;
