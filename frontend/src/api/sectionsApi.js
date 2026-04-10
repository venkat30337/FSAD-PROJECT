import axiosInstance from '../utils/axiosInstance';

export const getSectionsByCourseApi = (courseId) => axiosInstance.get(`/sections/course/${courseId}`);
export const createSectionApi = (payload) => axiosInstance.post('/sections', payload);
export const updateSectionApi = (id, payload) => axiosInstance.put(`/sections/${id}`, payload);
export const deleteSectionApi = (id) => axiosInstance.delete(`/sections/${id}`);
export const getSectionSeatsApi = (id) => axiosInstance.get(`/sections/${id}/seats`);
