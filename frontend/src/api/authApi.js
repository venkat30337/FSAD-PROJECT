import axiosInstance from '../utils/axiosInstance';

export const registerApi = (payload) => axiosInstance.post('/auth/register', payload);
export const loginApi = (payload) => axiosInstance.post('/auth/login', payload);
export const logoutApi = () => axiosInstance.post('/auth/logout');
