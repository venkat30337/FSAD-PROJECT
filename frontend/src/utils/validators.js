import * as Yup from 'yup';

export const registerSchema = Yup.object({
  fullName: Yup.string().min(3).max(120).required('Full name is required'),
  email: Yup.string().email('Enter a valid email').required('Email is required'),
  studentId: Yup.string().matches(/^[A-Z0-9]+$/i, 'Alphanumeric only').required('Student ID is required'),
  department: Yup.string().required('Select a department'),
  semester: Yup.number().min(1).max(8).required('Select semester'),
  phone: Yup.string().matches(/^[6-9]\d{9}$/, 'Enter a valid 10-digit mobile number').required('Phone is required'),
  password: Yup.string()
    .min(8, 'Minimum 8 characters')
    .matches(/[A-Z]/, 'Must contain at least one uppercase letter')
    .matches(/\d/, 'Must contain at least one number')
    .matches(/[@$!%*?&#]/, 'Must contain at least one special character')
    .required('Password is required'),
  confirmPassword: Yup.string()
    .oneOf([Yup.ref('password')], 'Passwords do not match')
    .required('Please confirm your password'),
});

export const loginSchema = Yup.object({
  email: Yup.string().email('Enter a valid email').required('Email is required'),
  password: Yup.string().required('Password is required'),
});

export const courseSchema = Yup.object({
  code: Yup.string().matches(/^[A-Z]{2,4}\d{3}$/, 'Format: CS301').required('Code is required'),
  title: Yup.string().min(5).max(200).required('Title is required'),
  description: Yup.string().min(20).max(2000).required('Description is required'),
  credits: Yup.number().min(1).max(6).required('Credits are required'),
  department: Yup.string().required('Department is required'),
  instructor: Yup.string().min(3).required('Instructor is required'),
});
