export const formatDateTime = (value) => {
  if (!value) return '-';
  return new Intl.DateTimeFormat('en-IN', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value));
};

export const formatTime = (value) => {
  if (!value) return '-';
  const [hours, minutes] = value.split(':');
  const date = new Date();
  date.setHours(Number(hours), Number(minutes), 0, 0);
  return new Intl.DateTimeFormat('en-IN', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: true,
  }).format(date);
};

export const timeToMinutes = (value) => {
  if (!value) return 0;
  const [hours, minutes] = value.split(':').map(Number);
  return hours * 60 + minutes;
};
