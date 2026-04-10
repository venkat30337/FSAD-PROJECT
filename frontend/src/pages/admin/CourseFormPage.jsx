import { useEffect, useMemo, useState } from 'react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { useNavigate, useParams } from 'react-router-dom';
import { archiveCourseApi, createCourseApi, getCourseByIdApi, getCoursesApi, publishCourseApi, updateCourseApi } from '../../api/coursesApi';
import { createSectionApi } from '../../api/sectionsApi';
import { courseSchema } from '../../utils/validators';

const CourseFormPage = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const editMode = Boolean(id);

  const [loading, setLoading] = useState(editMode);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [allCourses, setAllCourses] = useState([]);
  const [selectedPrerequisites, setSelectedPrerequisites] = useState([]);
  const [status, setStatus] = useState('DRAFT');
  const [newSections, setNewSections] = useState([]);
  const [existingSections, setExistingSections] = useState([]);
  const [sectionDraft, setSectionDraft] = useState({
    sectionCode: '',
    room: '',
    daysOfWeek: '',
    startTime: '09:00',
    endTime: '10:00',
    maxSeats: 60,
    academicYear: '2025-26',
    semesterTerm: 'ODD',
  });

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: yupResolver(courseSchema),
    defaultValues: {
      code: '',
      title: '',
      description: '',
      credits: 3,
      department: 'Computer Science',
      instructor: '',
    },
  });

  const availablePrerequisites = useMemo(() => {
    return allCourses.filter((course) => String(course.id) !== String(id));
  }, [allCourses, id]);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const [allCoursesRes, currentRes] = await Promise.all([
          getCoursesApi({ page: 0, size: 200 }),
          editMode ? getCourseByIdApi(id) : Promise.resolve({ data: null }),
        ]);

        setAllCourses(allCoursesRes.data.content || []);

        if (currentRes.data) {
          const c = currentRes.data;
          reset({
            code: c.code,
            title: c.title,
            description: c.description,
            credits: c.credits,
            department: c.department,
            instructor: c.instructor,
          });
          setStatus(c.status);
          setSelectedPrerequisites((c.prerequisites || []).map((p) => p.id));
          setExistingSections(c.sections || []);
        }
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load course form data.');
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [id]);

  const addSection = () => {
    if (!sectionDraft.sectionCode || !sectionDraft.daysOfWeek) {
      setError('Section code and days are required for section entries.');
      return;
    }
    setError('');
    setNewSections((prev) => [...prev, { ...sectionDraft }]);
    setSectionDraft((prev) => ({ ...prev, sectionCode: '', room: '', daysOfWeek: '' }));
  };

  const removeNewSection = (index) => {
    setNewSections((prev) => prev.filter((_, i) => i !== index));
  };

  const onSubmit = async (values) => {
    setError('');
    setSuccess('');

    try {
      const payload = {
        ...values,
        code: values.code.toUpperCase(),
        prerequisiteIds: selectedPrerequisites,
      };

      const response = editMode
        ? await updateCourseApi(id, payload)
        : await createCourseApi(payload);

      const courseId = response.data.id;

      if (status === 'PUBLISHED') {
        await publishCourseApi(courseId);
      } else if (status === 'ARCHIVED') {
        await archiveCourseApi(courseId);
      }

      for (const section of newSections) {
        await createSectionApi({
          ...section,
          courseId,
          maxSeats: Number(section.maxSeats),
        });
      }

      setSuccess(editMode ? 'Course updated successfully.' : 'Course created successfully.');
      setTimeout(() => navigate('/admin/courses'), 800);
    } catch (err) {
      const backendErrors = err.response?.data?.errors;
      if (backendErrors) {
        const first = Object.values(backendErrors)[0];
        setError(String(first));
      } else {
        setError(err.response?.data?.message || 'Failed to save course.');
      }
    }
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div className="grid" style={{ gap: 16 }}>
      <h1 className="page-title">{editMode ? 'Edit Course' : 'Create Course'}</h1>
      {error ? <div className="toast toast-error">{error}</div> : null}
      {success ? <div className="toast toast-success">{success}</div> : null}

      <form onSubmit={handleSubmit(onSubmit)} className="card grid" style={{ gap: 14 }}>
        <div className="grid grid-2">
          <div>
            <label className="label">Code</label>
            <input className="input" {...register('code')} />
            {errors.code ? <div className="error-text">{errors.code.message}</div> : null}
          </div>
          <div>
            <label className="label">Title</label>
            <input className="input" {...register('title')} />
            {errors.title ? <div className="error-text">{errors.title.message}</div> : null}
          </div>
          <div>
            <label className="label">Credits</label>
            <input type="number" className="input" {...register('credits')} />
            {errors.credits ? <div className="error-text">{errors.credits.message}</div> : null}
          </div>
          <div>
            <label className="label">Department</label>
            <input className="input" {...register('department')} />
            {errors.department ? <div className="error-text">{errors.department.message}</div> : null}
          </div>
          <div>
            <label className="label">Instructor</label>
            <input className="input" {...register('instructor')} />
            {errors.instructor ? <div className="error-text">{errors.instructor.message}</div> : null}
          </div>
          <div>
            <label className="label">Status</label>
            <select className="select" value={status} onChange={(e) => setStatus(e.target.value)}>
              <option value="DRAFT">DRAFT</option>
              <option value="PUBLISHED">PUBLISHED</option>
              <option value="ARCHIVED">ARCHIVED</option>
            </select>
          </div>
        </div>

        <div>
          <label className="label">Description</label>
          <textarea className="textarea" {...register('description')} />
          {errors.description ? <div className="error-text">{errors.description.message}</div> : null}
        </div>

        <div>
          <label className="label">Prerequisites</label>
          <select
            multiple
            className="select"
            style={{ minHeight: 120 }}
            value={selectedPrerequisites.map(String)}
            onChange={(event) => {
              const options = Array.from(event.target.selectedOptions).map((option) => Number(option.value));
              setSelectedPrerequisites(options);
            }}
          >
            {availablePrerequisites.map((course) => (
              <option key={course.id} value={course.id}>{course.code} - {course.title}</option>
            ))}
          </select>
        </div>

        <section className="card" style={{ borderStyle: 'dashed' }}>
          <h2 className="section-title">Section Management</h2>
          <div className="grid grid-3">
            <input className="input" placeholder="Section code" value={sectionDraft.sectionCode} onChange={(e) => setSectionDraft((prev) => ({ ...prev, sectionCode: e.target.value }))} />
            <input className="input" placeholder="Room" value={sectionDraft.room} onChange={(e) => setSectionDraft((prev) => ({ ...prev, room: e.target.value }))} />
            <input className="input" placeholder="Days (MON,WED)" value={sectionDraft.daysOfWeek} onChange={(e) => setSectionDraft((prev) => ({ ...prev, daysOfWeek: e.target.value }))} />
            <input className="input" type="time" value={sectionDraft.startTime} onChange={(e) => setSectionDraft((prev) => ({ ...prev, startTime: e.target.value }))} />
            <input className="input" type="time" value={sectionDraft.endTime} onChange={(e) => setSectionDraft((prev) => ({ ...prev, endTime: e.target.value }))} />
            <input className="input" type="number" min="1" value={sectionDraft.maxSeats} onChange={(e) => setSectionDraft((prev) => ({ ...prev, maxSeats: e.target.value }))} />
            <input className="input" value={sectionDraft.academicYear} onChange={(e) => setSectionDraft((prev) => ({ ...prev, academicYear: e.target.value }))} />
            <select className="select" value={sectionDraft.semesterTerm} onChange={(e) => setSectionDraft((prev) => ({ ...prev, semesterTerm: e.target.value }))}>
              <option value="ODD">ODD</option>
              <option value="EVEN">EVEN</option>
            </select>
            <button className="btn btn-secondary" type="button" onClick={addSection}>Add Section</button>
          </div>

          {existingSections.length > 0 ? (
            <div style={{ marginTop: 12 }}>
              <div className="label">Existing Sections</div>
              {existingSections.map((section) => (
                <div key={section.id}>{section.sectionCode} | {section.daysOfWeek} | {section.startTime}-{section.endTime}</div>
              ))}
            </div>
          ) : null}

          {newSections.length > 0 ? (
            <div style={{ marginTop: 12 }}>
              <div className="label">New Sections To Create</div>
              {newSections.map((section, index) => (
                <div key={`${section.sectionCode}-${index}`} style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6 }}>
                  <span>{section.sectionCode} | {section.daysOfWeek} | {section.startTime}-{section.endTime}</span>
                  <button className="btn btn-danger" type="button" onClick={() => removeNewSection(index)}>Remove</button>
                </div>
              ))}
            </div>
          ) : null}
        </section>

        <div style={{ display: 'flex', gap: 10 }}>
          <button className="btn btn-primary" type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Loading...' : editMode ? 'Update Course' : 'Create Course'}
          </button>
          <button className="btn btn-secondary" type="button" onClick={() => navigate('/admin/courses')}>Cancel</button>
        </div>
      </form>
    </div>
  );
};

export default CourseFormPage;
