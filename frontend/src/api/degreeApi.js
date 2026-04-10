import axiosInstance from '../utils/axiosInstance';

export const degreeAuditApi = () => axiosInstance.get('/degree/audit');
export const degreeRequirementsApi = () => axiosInstance.get('/degree/requirements');
