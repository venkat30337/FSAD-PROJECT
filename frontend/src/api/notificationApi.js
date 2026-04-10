import axiosInstance from '../utils/axiosInstance';

export const notificationsApi = (params) => axiosInstance.get('/notifications', { params });
export const latestNotificationsApi = () => axiosInstance.get('/notifications/latest');
export const markNotificationReadApi = (id) => axiosInstance.patch(`/notifications/${id}/read`);
export const markAllReadApi = () => axiosInstance.patch('/notifications/read-all');
export const unreadCountApi = () => axiosInstance.get('/notifications/unread-count');
