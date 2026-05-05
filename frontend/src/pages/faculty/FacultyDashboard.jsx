import { useEffect, useState } from 'react';
import QRCode from 'qrcode';
import { api, getErrorMessage } from '../../api/client.js';
import Message from '../../components/Message.jsx';
import { toIsoFromLocal, toLocalInputValue } from '../../utils/date.js';

export default function FacultyDashboard() {
  const [courses, setCourses] = useState([]);
  const [sessions, setSessions] = useState([]);
  const [records, setRecords] = useState([]);
  const [selectedSession, setSelectedSession] = useState('');
  const [selectedCourseId, setSelectedCourseId] = useState('');
  const [summaryMonth, setSummaryMonth] = useState(new Date().toISOString().slice(0, 7));
  const [summary, setSummary] = useState(null);
  const [qr, setQr] = useState(null);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [form, setForm] = useState({
    courseId: '',
    title: '',
    startsAt: toLocalInputValue(new Date()),
    endsAt: toLocalInputValue(new Date(Date.now() + 60 * 60 * 1000))
  });

  async function load() {
    const [coursesRes, sessionsRes] = await Promise.all([
      api.get('/faculty/courses'),
      api.get('/faculty/attendance-sessions')
    ]);
    setCourses(coursesRes.data);
    setSessions(sessionsRes.data);
    if (!selectedCourseId && coursesRes.data.length > 0) {
      setSelectedCourseId(String(coursesRes.data[0].id));
    }
  }

  async function loadSummary(courseId = selectedCourseId, month = summaryMonth) {
    if (!courseId) return;
    setError('');
    try {
      const response = await api.get(`/faculty/attendance-sessions/courses/${courseId}/summary`, {
        params: { month }
      });
      setSummary(response.data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  useEffect(() => {
    load().catch((err) => setError(getErrorMessage(err)));
  }, []);

  useEffect(() => {
    if (selectedCourseId) {
      loadSummary(selectedCourseId, summaryMonth);
    }
  }, [selectedCourseId, summaryMonth]);

  async function createSession(event) {
    event.preventDefault();
    setError('');
    setMessage('');
    try {
      await api.post('/faculty/attendance-sessions', {
        courseId: Number(form.courseId),
        title: form.title,
        startsAt: toIsoFromLocal(form.startsAt),
        endsAt: toIsoFromLocal(form.endsAt)
      });
      setMessage('Attendance session created');
      await load();
      await loadSummary(form.courseId, summaryMonth);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  async function generateQr(sessionId) {
    setError('');
    setQr(null);
    try {
      const response = await api.post(`/faculty/attendance-sessions/${sessionId}/qr`);
      setQr(response.data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  async function loadRecords(sessionId) {
    setSelectedSession(sessionId);
    setError('');
    try {
      const response = await api.get(`/faculty/attendance-sessions/${sessionId}/records`);
      setRecords(response.data);
      await loadSummary();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  return (
    <main className="shell">
      <h1>Faculty Dashboard</h1>
      <Message type="success">{message}</Message>
      <Message type="error">{error}</Message>
      <div className="grid">
        <section className="panel">
          <h2>Create Attendance Session</h2>
          <form className="stack" onSubmit={createSession}>
            <select value={form.courseId} onChange={(e) => setForm({ ...form, courseId: e.target.value })} required>
              <option value="">Course</option>
              {courses.map((course) => <option key={course.id} value={course.id}>{course.name} ({course.code})</option>)}
            </select>
            <input placeholder="Title" value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} required />
            <label>Starts at<input type="datetime-local" value={form.startsAt} onChange={(e) => setForm({ ...form, startsAt: e.target.value })} required /></label>
            <label>Ends at<input type="datetime-local" value={form.endsAt} onChange={(e) => setForm({ ...form, endsAt: e.target.value })} required /></label>
            <button>Create Session</button>
          </form>
        </section>
        <section className="panel">
          <h2>QR Token</h2>
          {qr ? <QrTokenDisplay qr={qr} /> : <p className="muted">Generate a token from a session below.</p>}
        </section>
      </div>
      <section className="panel">
        <h2>Sessions</h2>
        <table>
          <thead><tr><th>Title</th><th>Course</th><th>Window</th><th>Actions</th></tr></thead>
          <tbody>
            {sessions.map((session) => (
              <tr key={session.id}>
                <td>{session.title}</td>
                <td>{session.courseName}</td>
                <td>{new Date(session.startsAt).toLocaleString()} - {new Date(session.endsAt).toLocaleString()}</td>
                <td>
                  <button onClick={() => generateQr(session.id)}>Generate QR Token</button>
                  <button className="secondary" onClick={() => loadRecords(session.id)}>Records</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
      <section className="panel">
        <h2>Course Attendance Register</h2>
        <div className="toolbar">
          <select value={selectedCourseId} onChange={(e) => setSelectedCourseId(e.target.value)}>
            <option value="">Course</option>
            {courses.map((course) => <option key={course.id} value={course.id}>{course.name} ({course.code})</option>)}
          </select>
          <input type="month" value={summaryMonth} onChange={(e) => setSummaryMonth(e.target.value)} />
          <button type="button" onClick={() => loadSummary()}>Refresh</button>
        </div>
        <AttendanceRegister summary={summary} />
      </section>
      <section className="panel">
        <h2>Present Records {selectedSession && <span className="muted">for session #{selectedSession}</span>}</h2>
        <table>
          <thead><tr><th>Student</th><th>Email</th><th>Scanned</th><th>IP</th><th>Device</th></tr></thead>
          <tbody>
            {records.map((record) => (
              <tr key={record.id}>
                <td>{record.studentName}</td>
                <td>{record.studentEmail}</td>
                <td>{new Date(record.scannedAt).toLocaleString()}</td>
                <td>{record.ipAddress}</td>
                <td>{record.deviceHash}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </main>
  );
}

function QrTokenDisplay({ qr }) {
  const [qrImage, setQrImage] = useState('');

  useEffect(() => {
    QRCode.toDataURL(qr.token, { width: 220, margin: 2 })
      .then(setQrImage)
      .catch(() => setQrImage(''));
  }, [qr.token]);

  return (
    <div className="token-box">
      {qrImage && <img className="qr-image" src={qrImage} alt="QR code for attendance token" />}
      <label>
        Raw token code
        <div className="token">{qr.token}</div>
      </label>
      <p className="muted">Expires: {new Date(qr.expiresAt).toLocaleString()}</p>
    </div>
  );
}

function AttendanceRegister({ summary }) {
  if (!summary) {
    return <p className="muted">Select a course and month to view attendance.</p>;
  }

  return (
    <div className="table-scroll">
      <table>
        <thead>
          <tr>
            <th>Roll</th>
            <th>Student</th>
            <th>Email</th>
            {summary.sessions.map((session) => (
              <th key={session.id}>
                {new Date(session.startsAt).toLocaleDateString()}
                <span className="muted block">{session.title}</span>
              </th>
            ))}
            <th>Present</th>
            <th>Monthly %</th>
          </tr>
        </thead>
        <tbody>
          {summary.students.map((student) => (
            <tr key={student.studentId}>
              <td>{student.rollNumber}</td>
              <td>{student.studentName}</td>
              <td>{student.studentEmail}</td>
              {student.attendance.map((cell) => (
                <td key={cell.sessionId} className={cell.present ? 'present' : 'absent'}>
                  {cell.present ? 'P' : 'A'}
                  {cell.scannedAt && <span className="muted block">{new Date(cell.scannedAt).toLocaleTimeString()}</span>}
                </td>
              ))}
              <td>{student.presentCount}/{student.totalSessions}</td>
              <td><strong>{student.attendancePercentage}%</strong></td>
            </tr>
          ))}
        </tbody>
      </table>
      {summary.sessions.length === 0 && <p className="muted">No sessions created for this course in {summary.month}. Enrolled students are still listed.</p>}
    </div>
  );
}
