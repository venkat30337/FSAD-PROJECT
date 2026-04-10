import { useEffect, useState } from 'react';
import { latestNotificationsApi, markAllReadApi, unreadCountApi } from '../../api/notificationApi';
import useNotificationStore from '../../store/notificationStore';
import { formatDateTime } from '../../utils/timeUtils';

const NotificationBell = () => {
  const [open, setOpen] = useState(false);
  const [items, setItems] = useState([]);
  const { unreadCount, setUnreadCount } = useNotificationStore();

  const loadUnread = async () => {
    const { data } = await unreadCountApi();
    setUnreadCount(data.count || 0);
  };

  const loadLatest = async () => {
    const { data } = await latestNotificationsApi();
    setItems(data || []);
  };

  const markAllRead = async () => {
    await markAllReadApi();
    await Promise.all([loadUnread(), loadLatest()]);
  };

  useEffect(() => {
    loadUnread();
    loadLatest();
    const timer = setInterval(loadUnread, 30000);
    return () => clearInterval(timer);
  }, []);

  return (
    <div style={{ position: 'relative' }}>
      <button className="btn btn-secondary" onClick={() => setOpen((prev) => !prev)} style={{ position: 'relative', width: 40, padding: 0 }}>
        N
        {unreadCount > 0 && (
          <span style={{ position: 'absolute', top: -8, right: -8, background: '#b91c1c', color: '#fff', borderRadius: 999, minWidth: 18, textAlign: 'center', fontSize: 11, padding: '2px 5px' }}>
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        )}
      </button>
      {open && (
        <div className="card" style={{ position: 'absolute', right: 0, top: 44, width: 360, zIndex: 50 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
            <strong>Notifications</strong>
            <button className="btn btn-secondary" onClick={markAllRead}>Mark all read</button>
          </div>
          <div style={{ maxHeight: 300, overflowY: 'auto' }}>
            {items.length === 0 ? <p style={{ margin: 0, color: 'var(--color-text-muted)' }}>No notifications</p> : null}
            {items.map((item) => (
              <div key={item.id} style={{ padding: '8px 0', borderBottom: '1px solid var(--color-border)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', gap: 8 }}>
                  <strong style={{ fontSize: 13 }}>{item.title}</strong>
                  {!item.isRead ? <span className="badge badge-warning">New</span> : null}
                </div>
                <div style={{ color: 'var(--color-text-muted)', marginTop: 4 }}>{item.message}</div>
                <small style={{ color: 'var(--color-text-muted)' }}>{formatDateTime(item.createdAt)}</small>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default NotificationBell;
