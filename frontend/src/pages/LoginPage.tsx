import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import AuthLoading from '../components/AuthLoading';
import { useAuth } from '../contexts/AuthContext';
import { validateLogin } from '../services/api';

export default function LoginPage() {
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('admin123');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const { authenticated, authReady, login } = useAuth();
  const navigate = useNavigate();

  if (!authReady) {
    return <AuthLoading />;
  }

  if (authenticated) {
    return <Navigate to="/" replace />;
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    const ok = await validateLogin(username, password);
    setLoading(false);
    if (ok) {
      login();
      navigate('/');
    } else {
      setError('Credenciais invalidas');
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-950 px-4">
      <form onSubmit={handleSubmit} className="w-full max-w-md rounded-2xl border border-slate-800 bg-slate-900 p-8 shadow-xl">
        <h1 className="text-2xl font-bold text-white">Proposals API</h1>
        <p className="mt-2 text-sm text-slate-400">Login seguro com HTTP Basic Auth</p>
        <div className="mt-6 space-y-4">
          <label className="block">
            <span className="text-sm text-slate-400">Usuario</span>
            <input
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-white"
              required
            />
          </label>
          <label className="block">
            <span className="text-sm text-slate-400">Senha</span>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-white"
              required
            />
          </label>
        </div>
        {error && <p className="mt-4 text-sm text-red-400">{error}</p>}
        <button
          type="submit"
          disabled={loading}
          className="mt-6 w-full rounded-lg bg-blue-600 py-3 font-medium text-white hover:bg-blue-500 disabled:opacity-60"
        >
          {loading ? 'Entrando...' : 'Entrar'}
        </button>
      </form>
    </div>
  );
}
