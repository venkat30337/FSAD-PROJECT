import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { registerSchema } from '../../utils/validators';
import { registerApi } from '../../api/authApi';

const departments = ['Computer Science', 'Electronics', 'Mechanical', 'Civil', 'Information Technology', 'Mathematics'];

const RegisterPage = () => {
  const navigate = useNavigate();
  const [serverError, setServerError] = useState('');
  const [success, setSuccess] = useState('');

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: yupResolver(registerSchema),
    defaultValues: {
      fullName: '',
      email: '',
      studentId: '',
      department: '',
      semester: '',
      phone: '',
      password: '',
      confirmPassword: '',
    },
  });

  const onSubmit = async (values) => {
    setServerError('');
    setSuccess('');
    try {
      await registerApi(values);
      setSuccess('Account created. Please log in.');
      setTimeout(() => navigate('/login'), 900);
    } catch (error) {
      setServerError(error.response?.data?.message || 'Unable to register at this time.');
    }
  };

  return (
    <div className="auth-wrap">
      <div className="auth-card">
        <div className="auth-art">
          <h1 style={{ fontSize: 34, margin: 0 }}>CourseFlow</h1>
          <p style={{ marginTop: 10, lineHeight: 1.6 }}>
            Create your student account to browse courses, enroll sections, and build a conflict-free weekly schedule.
          </p>
        </div>
        <div className="auth-body">
          <h2 className="page-title" style={{ marginTop: 0 }}>Student Registration</h2>
          {serverError ? <div className="toast toast-error">{serverError}</div> : null}
          {success ? <div className="toast toast-success">{success}</div> : null}

          <form onSubmit={handleSubmit(onSubmit)} className="grid grid-2">
            <div>
              <label className="label">Full Name</label>
              <input className="input" {...register('fullName')} />
              {errors.fullName ? <div className="error-text">{errors.fullName.message}</div> : null}
            </div>

            <div>
              <label className="label">Email</label>
              <input type="email" className="input" {...register('email')} />
              {errors.email ? <div className="error-text">{errors.email.message}</div> : null}
            </div>

            <div>
              <label className="label">Student ID</label>
              <input className="input" {...register('studentId')} />
              {errors.studentId ? <div className="error-text">{errors.studentId.message}</div> : null}
            </div>

            <div>
              <label className="label">Department</label>
              <select className="select" {...register('department')}>
                <option value="">Select department</option>
                {departments.map((dept) => (
                  <option value={dept} key={dept}>{dept}</option>
                ))}
              </select>
              {errors.department ? <div className="error-text">{errors.department.message}</div> : null}
            </div>

            <div>
              <label className="label">Semester</label>
              <select className="select" {...register('semester')}>
                <option value="">Select semester</option>
                {Array.from({ length: 8 }).map((_, index) => (
                  <option key={index + 1} value={index + 1}>{index + 1}</option>
                ))}
              </select>
              {errors.semester ? <div className="error-text">{errors.semester.message}</div> : null}
            </div>

            <div>
              <label className="label">Phone</label>
              <input className="input" {...register('phone')} />
              {errors.phone ? <div className="error-text">{errors.phone.message}</div> : null}
            </div>

            <div>
              <label className="label">Password</label>
              <input type="password" className="input" {...register('password')} />
              {errors.password ? <div className="error-text">{errors.password.message}</div> : null}
            </div>

            <div>
              <label className="label">Confirm Password</label>
              <input type="password" className="input" {...register('confirmPassword')} />
              {errors.confirmPassword ? <div className="error-text">{errors.confirmPassword.message}</div> : null}
            </div>

            <div style={{ gridColumn: '1 / -1', display: 'flex', gap: 10, alignItems: 'center' }}>
              <button className="btn btn-primary" disabled={isSubmitting} type="submit">
                {isSubmitting ? 'Loading...' : 'Create Account'}
              </button>
              <Link to="/login" style={{ color: 'var(--color-primary-dark)', fontWeight: 600 }}>
                Already have an account? Login
              </Link>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;
