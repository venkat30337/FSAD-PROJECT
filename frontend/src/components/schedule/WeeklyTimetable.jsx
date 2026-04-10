import { useMemo } from 'react';
import { timeToMinutes } from '../../utils/timeUtils';

const palette = ['#dbeafe', '#dcfce7', '#fee2e2', '#fef3c7', '#e0e7ff', '#fae8ff', '#d1fae5', '#fce7f3'];
const days = ['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT'];
const startHour = 7;
const endHour = 21;
const pxPerHour = 56;

const WeeklyTimetable = ({ sections = [] }) => {
  const blocks = useMemo(() => {
    const result = [];
    sections.forEach((section) => {
      const start = timeToMinutes(section.startTime);
      const end = timeToMinutes(section.endTime);
      const top = ((start - startHour * 60) / 60) * pxPerHour;
      const height = ((end - start) / 60) * pxPerHour;
      const color = palette[(section.courseId || 0) % palette.length];

      section.daysOfWeek.split(',').map((d) => d.trim()).forEach((day) => {
        const dayIndex = days.indexOf(day);
        if (dayIndex >= 0) {
          result.push({
            id: `${section.id}-${day}`,
            dayIndex,
            top,
            height,
            color,
            courseCode: section.courseCode,
            room: section.room,
          });
        }
      });
    });
    return result;
  }, [sections]);

  return (
    <div className="card" style={{ overflowX: 'auto' }}>
      <div style={{ minWidth: 940 }}>
        <div style={{ display: 'grid', gridTemplateColumns: '80px repeat(6, 1fr)', borderBottom: '1px solid var(--color-border)' }}>
          <div style={{ padding: 8 }} />
          {days.map((day) => (
            <div key={day} style={{ padding: 8, textAlign: 'center', fontWeight: 700 }}>{day}</div>
          ))}
        </div>

        <div style={{ position: 'relative', height: (endHour - startHour) * pxPerHour }}>
          <div style={{ display: 'grid', gridTemplateColumns: '80px repeat(6, 1fr)', height: '100%' }}>
            <div>
              {Array.from({ length: endHour - startHour + 1 }).map((_, idx) => (
                <div key={idx} style={{ height: pxPerHour, borderBottom: '1px solid #eef2f7', fontSize: 12, color: 'var(--color-text-muted)' }}>
                  {String(startHour + idx).padStart(2, '0')}:00
                </div>
              ))}
            </div>
            {days.map((day) => (
              <div key={day} style={{ borderLeft: '1px solid #eef2f7', borderRight: '1px solid #eef2f7' }}>
                {Array.from({ length: endHour - startHour + 1 }).map((_, idx) => (
                  <div key={idx} style={{ height: pxPerHour, borderBottom: '1px solid #eef2f7' }} />
                ))}
              </div>
            ))}
          </div>

          {blocks.map((block) => (
            <div
              key={block.id}
              style={{
                position: 'absolute',
                left: `calc(80px + ${block.dayIndex} * ((100% - 80px) / 6) + 6px)`,
                width: 'calc((100% - 80px) / 6 - 12px)',
                top: block.top,
                height: block.height,
                background: block.color,
                border: '1px solid rgba(17,24,39,0.08)',
                borderRadius: 8,
                padding: 6,
                overflow: 'hidden',
                fontSize: 12,
                fontWeight: 700,
              }}
            >
              <div>{block.courseCode}</div>
              <div style={{ fontWeight: 500 }}>{block.room || 'Room TBD'}</div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default WeeklyTimetable;
