import axiosInstance from '../utils/axiosInstance';

export const adminDashboardApi = () => axiosInstance.get('/admin/dashboard');
export const adminUsersApi = (params) => axiosInstance.get('/admin/users', { params });
export const adminUserDetailApi = (id) => axiosInstance.get(`/admin/users/${id}`);
export const toggleUserActiveApi = (id) => axiosInstance.patch(`/admin/users/${id}/toggle-active`);
export const adminConflictsApi = (params) => axiosInstance.get('/admin/conflicts', { params });
export const adminManualEnrollApi = (payload) => axiosInstance.post('/admin/enroll', payload);
export const adminForceDropApi = (id) => axiosInstance.delete(`/admin/enrollments/${id}`);
export const adminAuditLogsApi = (params) => axiosInstance.get('/admin/audit-logs', { params });
