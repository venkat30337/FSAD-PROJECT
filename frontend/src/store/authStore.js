import { create } from 'zustand';

const normalizeRole = (role) => String(role || '').replace('ROLE_', '').toUpperCase();

const storedToken = localStorage.getItem('token');
const storedUser = localStorage.getItem('authUser');

const initialUser = storedUser ? JSON.parse(storedUser) : null;

const useAuthStore = create((set) => ({
  token: storedToken || null,
  user: initialUser,
  isAuthenticated: Boolean(storedToken),
  login: (payload) => {
    const user = {
      userId: payload.userId,
      email: payload.email,
      fullName: payload.fullName,
      role: normalizeRole(payload.role),
      department: payload.department,
      semester: payload.semester,
      maxCredits: payload.maxCredits,
    };
    localStorage.setItem('token', payload.token);
    localStorage.setItem('authUser', JSON.stringify(user));
    set({ token: payload.token, user, isAuthenticated: true });
  },
  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('authUser');
    set({ token: null, user: null, isAuthenticated: false });
  },
  hydrate: () => {
    const token = localStorage.getItem('token');
    const userRaw = localStorage.getItem('authUser');
    set({
      token,
      user: userRaw ? JSON.parse(userRaw) : null,
      isAuthenticated: Boolean(token),
    });
  },
}));

export default useAuthStore;
