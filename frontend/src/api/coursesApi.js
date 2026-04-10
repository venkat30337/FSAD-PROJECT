import axiosInstance from '../utils/axiosInstance';

export const getPublicCoursesApi = (params) => axiosInstance.get('/courses/public', { params });
export const getCoursesApi = (params) => axiosInstance.get('/courses', { params });
export const getCourseByIdApi = (id) => axiosInstance.get(`/courses/${id}`);
export const createCourseApi = (payload) => axiosInstance.post('/courses', payload);
export const updateCourseApi = (id, payload) => axiosInstance.put(`/courses/${id}`, payload);
export const publishCourseApi = (id) => axiosInstance.patch(`/courses/${id}/publish`);
export const archiveCourseApi = (id) => axiosInstance.patch(`/courses/${id}/archive`);
export const deleteCourseApi = (id) => axiosInstance.delete(`/courses/${id}`);
