import axiosInstance from '../utils/axiosInstance';

export const enrollApi = (sectionId) => axiosInstance.post('/enrollments', { sectionId });
export const dropEnrollmentApi = (id) => axiosInstance.delete(`/enrollments/${id}`);
export const myEnrollmentsApi = () => axiosInstance.get('/enrollments/my');
export const myScheduleApi = () => axiosInstance.get('/enrollments/my/schedule');
export const sectionEnrollmentsApi = (sectionId, params) => axiosInstance.get(`/enrollments/section/${sectionId}`, { params });
export const conflictsApi = (params) => axiosInstance.get('/enrollments/conflicts', { params });
export const checkConflictApi = (sectionId) => axiosInstance.post('/enrollments/check-conflict', { sectionId });
