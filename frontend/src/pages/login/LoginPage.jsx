import { useState } from 'react';
import { useAuth } from '../../auth/AuthContext.jsx';
import { getErrorMessage } from '../../api/client.js';
import Message from '../../components/Message.jsx';

export default function LoginPage() {
  const { login } = useAuth();
  const [email, setEmail] = useState('admin@example.com');
  const [password, setPassword] = useState('Admin@12345');
  const [error, setError] = useState('');

  async function submit(event) {
    event.preventDefault();
    setError('');
    try {
      await login(email, password);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  return (
    <main className="shell narrow">
      <section className="panel">
        <h1>Login</h1>
        <Message type="error">{error}</Message>
        <form onSubmit={submit} className="stack">
          <label>
            Email
            <input value={email} onChange={(e) => setEmail(e.target.value)} type="email" required />
          </label>
          <label>
            Password
            <input value={password} onChange={(e) => setPassword(e.target.value)} type="password" required />
          </label>
          <button type="submit">Login</button>
        </form>
        <p className="muted small">Demo: admin@example.com, faculty@example.com, student@example.com</p>
      </section>
    </main>
  );
}
