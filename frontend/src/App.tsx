import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import AuthLoading from './components/AuthLoading';
import Layout from './components/Layout';
import ProposalForm from './components/ProposalForm';
import LoginPage from './pages/LoginPage';
import HistoricoPage from './pages/HistoricoPage';
import EmailsPage from './pages/EmailsPage';
import FilasPage from './pages/FilasPage';
import PropostasPage from './pages/PropostasPage';

function PrivateRoute({ children }: { children: React.ReactNode }) {
  const { authenticated, authReady } = useAuth();

  if (!authReady) {
    return <AuthLoading />;
  }

  return authenticated ? <>{children}</> : <Navigate to="/login" replace />;
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/"
            element={
              <PrivateRoute>
                <Layout />
              </PrivateRoute>
            }
          >
            <Route index element={<ProposalForm />} />
            <Route path="propostas" element={<PropostasPage />} />
            <Route path="historico" element={<HistoricoPage />} />
            <Route path="emails" element={<EmailsPage />} />
            <Route path="filas" element={<FilasPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
