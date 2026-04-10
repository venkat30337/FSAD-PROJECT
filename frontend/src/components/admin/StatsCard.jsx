const StatsCard = ({ label, value }) => {
  return (
    <div className="card" style={{ padding: 16 }}>
      <div className="label" style={{ marginBottom: 4 }}>{label}</div>
      <div style={{ fontSize: 28, fontWeight: 800 }}>{value}</div>
    </div>
  );
};

export default StatsCard;
