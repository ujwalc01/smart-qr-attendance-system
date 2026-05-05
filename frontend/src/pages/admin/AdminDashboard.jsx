import { useEffect, useState } from 'react';
import { api, getErrorMessage } from '../../api/client.js';
import Message from '../../components/Message.jsx';

const emptyUser = { name: '', email: '', password: '', rollNumber: '', employeeCode: '' };

export default function AdminDashboard() {
  const [students, setStudents] = useState([]);
  const [faculty, setFaculty] = useState([]);
  const [courses, setCourses] = useState([]);
  const [studentForm, setStudentForm] = useState(emptyUser);
  const [facultyForm, setFacultyForm] = useState(emptyUser);
  const [courseForm, setCourseForm] = useState({ name: '', code: '' });
  const [assignment, setAssignment] = useState({ courseId: '', facultyId: '' });
  const [enrollment, setEnrollment] = useState({ studentId: '', courseId: '' });
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  async function load() {
    const [studentsRes, facultyRes, coursesRes] = await Promise.all([
      api.get('/admin/students'),
      api.get('/admin/faculty'),
      api.get('/admin/courses')
    ]);
    setStudents(studentsRes.data);
    setFaculty(facultyRes.data);
    setCourses(coursesRes.data);
  }

  useEffect(() => {
    load().catch((err) => setError(getErrorMessage(err)));
  }, []);

  async function action(fn, success) {
    setError('');
    setMessage('');
    try {
      await fn();
      setMessage(success);
      await load();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  return (
    <main className="shell">
      <h1>Admin Dashboard</h1>
      <Message type="success">{message}</Message>
      <Message type="error">{error}</Message>
      <div className="grid">
        <section className="panel">
          <h2>Create Student</h2>
          <UserForm value={studentForm} onChange={setStudentForm} showRoll onSubmit={() => action(async () => {
            await api.post('/admin/students', studentForm);
            setStudentForm(emptyUser);
          }, 'Student created')} />
        </section>
        <section className="panel">
          <h2>Create Faculty</h2>
          <UserForm value={facultyForm} onChange={setFacultyForm} showEmployee onSubmit={() => action(async () => {
            await api.post('/admin/faculty', facultyForm);
            setFacultyForm(emptyUser);
          }, 'Faculty created')} />
        </section>
        <section className="panel">
          <h2>Create Course</h2>
          <form className="stack" onSubmit={(e) => {
            e.preventDefault();
            action(async () => {
              await api.post('/admin/courses', courseForm);
              setCourseForm({ name: '', code: '' });
            }, 'Course created');
          }}>
            <input placeholder="Course name" value={courseForm.name} onChange={(e) => setCourseForm({ ...courseForm, name: e.target.value })} required />
            <input placeholder="Course code" value={courseForm.code} onChange={(e) => setCourseForm({ ...courseForm, code: e.target.value })} required />
            <button>Create</button>
          </form>
        </section>
        <section className="panel">
          <h2>Assign Faculty</h2>
          <form className="stack" onSubmit={(e) => {
            e.preventDefault();
            action(() => api.post(`/admin/courses/${assignment.courseId}/faculty/${assignment.facultyId}`), 'Faculty assigned');
          }}>
            <Select value={assignment.courseId} onChange={(courseId) => setAssignment({ ...assignment, courseId })} items={courses} label="Course" />
            <Select value={assignment.facultyId} onChange={(facultyId) => setAssignment({ ...assignment, facultyId })} items={faculty} label="Faculty" />
            <button>Assign</button>
          </form>
        </section>
        <section className="panel">
          <h2>Enroll Student</h2>
          <form className="stack" onSubmit={(e) => {
            e.preventDefault();
            action(() => api.post('/admin/enrollments', enrollment), 'Student enrolled');
          }}>
            <Select value={enrollment.studentId} onChange={(studentId) => setEnrollment({ ...enrollment, studentId })} items={students} label="Student" />
            <Select value={enrollment.courseId} onChange={(courseId) => setEnrollment({ ...enrollment, courseId })} items={courses} label="Course" />
            <button>Enroll</button>
          </form>
        </section>
      </div>
      <Lists students={students} faculty={faculty} courses={courses} />
    </main>
  );
}

function UserForm({ value, onChange, onSubmit, showRoll, showEmployee }) {
  return (
    <form className="stack" onSubmit={(e) => { e.preventDefault(); onSubmit(); }}>
      <input placeholder="Name" value={value.name} onChange={(e) => onChange({ ...value, name: e.target.value })} required />
      <input placeholder="Email" type="email" value={value.email} onChange={(e) => onChange({ ...value, email: e.target.value })} required />
      <input placeholder="Password" type="password" value={value.password} onChange={(e) => onChange({ ...value, password: e.target.value })} required minLength="8" />
      {showRoll && <input placeholder="Roll number" value={value.rollNumber} onChange={(e) => onChange({ ...value, rollNumber: e.target.value })} />}
      {showEmployee && <input placeholder="Employee code" value={value.employeeCode} onChange={(e) => onChange({ ...value, employeeCode: e.target.value })} />}
      <button>Create</button>
    </form>
  );
}

function Select({ value, onChange, items, label }) {
  return (
    <select value={value} onChange={(e) => onChange(e.target.value)} required>
      <option value="">{label}</option>
      {items.map((item) => <option key={item.id} value={item.id}>{item.name} {item.code ? `(${item.code})` : ''}</option>)}
    </select>
  );
}

function Lists({ students, faculty, courses }) {
  return (
    <div className="grid three">
      <List title="Students" items={students} />
      <List title="Faculty" items={faculty} />
      <List title="Courses" items={courses} />
    </div>
  );
}

function List({ title, items }) {
  return (
    <section className="panel">
      <h2>{title}</h2>
      <ul className="plain-list">
        {items.map((item) => <li key={item.id}>{item.name} <span className="muted">{item.email || item.code}</span></li>)}
      </ul>
    </section>
  );
}
