import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { archiveCourseApi, deleteCourseApi, getCoursesApi, publishCourseApi } from '../../api/coursesApi';
import StatusBadge from '../../components/common/StatusBadge';
import Spinner from '../../components/common/Spinner';

const AdminCoursesPage = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [courses, setCourses] = useState([]);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const { data } = await getCoursesApi({ page: 0, size: 200 });
      setCourses(data.content || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load courses.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const publishOrArchive = async (course) => {
    if (course.status === 'PUBLISHED') {
      await archiveCourseApi(course.id);
    } else {
      await publishCourseApi(course.id);
    }
    await load();
  };

  const remove = async (id) => {
    await deleteCourseApi(id);
    await load();
  };

  if (loading) return <Spinner />;

  return (
    <div className="grid" style={{ gap: 14 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h1 className="page-title" style={{ margin: 0 }}>Courses</h1>
        <Link to="/admin/courses/new" className="btn btn-primary">Add Course</Link>
      </div>

      {error ? <div className="toast toast-error">{error}</div> : null}

      <section className="card">
        <table className="table">
          <thead>
            <tr>
              <th>Code</th>
              <th>Title</th>
              <th>Dept</th>
              <th>Credits</th>
              <th>Instructor</th>
              <th>Status</th>
              <th>Sections</th>
              <th>Enrolled</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {courses.map((course) => (
              <tr key={course.id}>
                <td>{course.code}</td>
                <td>{course.title}</td>
                <td>{course.department}</td>
                <td>{course.credits}</td>
                <td>{course.instructor}</td>
                <td><StatusBadge status={course.status} /></td>
                <td>{course.sections?.length || 0}</td>
                <td>{(course.sections || []).reduce((sum, section) => sum + section.enrolledCount, 0)}</td>
                <td style={{ display: 'flex', gap: 6 }}>
                  <Link className="btn btn-secondary" to={`/admin/courses/${course.id}`}>Edit</Link>
                  <button className="btn btn-secondary" onClick={() => publishOrArchive(course)}>
                    {course.status === 'PUBLISHED' ? 'Archive' : 'Publish'}
                  </button>
                  <button className="btn btn-danger" onClick={() => remove(course.id)}>Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  );
};

export default AdminCoursesPage;
