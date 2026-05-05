import { useEffect, useState } from 'react';
import { api, getErrorMessage } from '../../api/client.js';
import Message from '../../components/Message.jsx';

export default function StudentDashboard() {
  const [token, setToken] = useState('');
  const [deviceHash, setDeviceHash] = useState('');
  const [records, setRecords] = useState([]);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  async function load() {
    const response = await api.get('/student/attendance/my');
    setRecords(response.data);
  }

  useEffect(() => {
    load().catch((err) => setError(getErrorMessage(err)));
  }, []);

  async function submit(event) {
    event.preventDefault();
    setError('');
    setMessage('');
    try {
      await api.post('/student/attendance/scan', { token, deviceHash });
      setToken('');
      setMessage('Attendance marked');
      await load();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  return (
    <main className="shell">
      <h1>Student Dashboard</h1>
      <Message type="success">{message}</Message>
      <Message type="error">{error}</Message>
      <div className="grid">
        <section className="panel">
          <h2>Submit QR Token</h2>
          <form className="stack" onSubmit={submit}>
            <textarea placeholder="Paste scanned QR token" value={token} onChange={(e) => setToken(e.target.value)} required />
            <input placeholder="Optional device hash" value={deviceHash} onChange={(e) => setDeviceHash(e.target.value)} />
            <button>Mark Attendance</button>
          </form>
        </section>
        <section className="panel">
          <h2>My Attendance</h2>
          <table>
            <thead><tr><th>Course</th><th>Session</th><th>Scanned</th><th>Status</th></tr></thead>
            <tbody>
              {records.map((record) => (
                <tr key={record.id}>
                  <td>{record.courseName}</td>
                  <td>{record.sessionTitle}</td>
                  <td>{new Date(record.scannedAt).toLocaleString()}</td>
                  <td>{record.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      </div>
    </main>
  );
}
