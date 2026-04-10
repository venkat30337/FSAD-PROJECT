import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { loginSchema } from '../../utils/validators';
import { loginApi } from '../../api/authApi';
import useAuthStore from '../../store/authStore';

const LoginPage = () => {
  const navigate = useNavigate();
  const { login } = useAuthStore();
  const [showPassword, setShowPassword] = useState(false);
  const [serverError, setServerError] = useState('');

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: yupResolver(loginSchema),
    defaultValues: { email: '', password: '' },
  });

  const onSubmit = async (values) => {
    setServerError('');
    try {
      const { data } = await loginApi(values);
      const normalizedRole = String(data.role || '').replace('ROLE_', '').toUpperCase();
      login({ ...data, role: normalizedRole });
      navigate(normalizedRole === 'ADMIN' ? '/admin' : '/dashboard');
    } catch (error) {
      if (!error.response) {
        setServerError('Cannot connect to backend. Start Spring Boot server on port 8080 and try again.');
        return;
      }
      setServerError(error.response?.data?.message || 'Invalid email or password.');
    }
  };

  return (
    <div className="auth-wrap login-theme-wrap">
      <div className="auth-card auth-card-login" style={{ maxWidth: 940 }}>
        <div className="auth-art login-art">
          <div className="login-brand-kicker">Academic Suite</div>
          <h1 style={{ fontSize: 36, margin: '0 0 10px' }}>CourseFlow</h1>
          <h2 style={{ margin: 0, fontSize: 20, fontWeight: 700 }}>Enrollment and Scheduling Portal</h2>
          <p style={{ marginTop: 12, lineHeight: 1.7, maxWidth: 460 }}>
            Plan your semester with confidence. Track available seats, avoid timetable conflicts, and monitor your degree progress in one place.
          </p>

          <div className="login-feature-list">
            <div className="login-feature-item">Real-time course and section visibility</div>
            <div className="login-feature-item">Conflict and prerequisite validation</div>
            <div className="login-feature-item">Smart waitlist and notifications</div>
          </div>
        </div>
        <div className="auth-body" style={{ display: 'grid', alignContent: 'center' }}>
          <h2 className="page-title" style={{ marginTop: 0, marginBottom: 8 }}>Sign In</h2>
          <p className="login-panel-subtitle">
            Enter your account details to continue to your personalized dashboard.
          </p>
          {serverError ? <div className="toast toast-error">{serverError}</div> : null}

          <form onSubmit={handleSubmit(onSubmit)}>
            <div style={{ marginBottom: 14 }}>
              <label className="label">Email</label>
              <input type="email" className="input" {...register('email')} />
              {errors.email ? <div className="error-text">{errors.email.message}</div> : null}
            </div>

            <div style={{ marginBottom: 14 }}>
              <label className="label">Password</label>
              <div style={{ display: 'flex', gap: 8 }}>
                <input type={showPassword ? 'text' : 'password'} className="input" {...register('password')} />
                <button className="btn btn-secondary" type="button" onClick={() => setShowPassword((prev) => !prev)}>
                  {showPassword ? 'Hide' : 'Show'}
                </button>
              </div>
              {errors.password ? <div className="error-text">{errors.password.message}</div> : null}
            </div>

            <button className="btn btn-primary" type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Loading...' : 'Login'}
            </button>
          </form>

          <div className="login-detail-card">
            <div className="login-detail-title">Portal Highlights</div>
            <div className="login-detail-item">
              <span>Student Tools</span>
              <strong>Catalog, schedule, degree audit</strong>
            </div>
            <div className="login-detail-item">
              <span>Admin Tools</span>
              <strong>Users, courses, conflicts, audit logs</strong>
            </div>
          </div>

          <div style={{ marginTop: 14 }}>
            New student? <Link to="/register" style={{ color: 'var(--color-primary-dark)', fontWeight: 600 }}>Create account</Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
