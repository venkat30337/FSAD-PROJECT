const Spinner = () => {
  return (
    <div style={{ display: 'inline-block', width: 22, height: 22, border: '3px solid #dbeafe', borderTopColor: '#1a56db', borderRadius: '50%', animation: 'spin 0.8s linear infinite' }}>
      <style>{'@keyframes spin { to { transform: rotate(360deg); } }'}</style>
    </div>
  );
};

export default Spinner;
