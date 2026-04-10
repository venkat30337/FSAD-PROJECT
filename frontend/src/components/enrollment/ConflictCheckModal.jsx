import { formatTime } from '../../utils/timeUtils';

const overlayStyle = {
  position: 'fixed',
  inset: 0,
  background: 'rgba(17,24,39,0.35)',
  display: 'grid',
  placeItems: 'center',
  zIndex: 100,
};

const ConflictCheckModal = ({ open, checking, result, error, onConfirm, onClose }) => {
  if (!open) return null;

  return (
    <div style={overlayStyle}>
      <div className="card" style={{ width: 'min(520px, 92%)' }}>
        <h3 className="section-title">Enrollment Check</h3>

        {checking ? <p>Checking for scheduling conflicts...</p> : null}

        {!checking && error ? <div className="toast toast-error">{error}</div> : null}

        {!checking && !error && result?.hasConflict === false ? (
          <div className="toast toast-success">No conflict found. You can proceed with enrollment.</div>
        ) : null}

        {!checking && result?.hasConflict && result.conflictingSection ? (
          <div className="toast toast-error">
            Time conflict with {result.conflictingSection.courseCode} - {result.conflictingSection.daysOfWeek} {formatTime(result.conflictingSection.startTime)} to {formatTime(result.conflictingSection.endTime)}
            {result.conflictingSection.room ? `, Room ${result.conflictingSection.room}` : ''}
          </div>
        ) : null}

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10, marginTop: 12 }}>
          <button className="btn btn-secondary" onClick={onClose}>Close</button>
          <button className="btn btn-primary" disabled={checking || error || result?.hasConflict} onClick={onConfirm}>
            Confirm Enroll
          </button>
        </div>
      </div>
    </div>
  );
};

export default ConflictCheckModal;
