import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { AuthProvider } from '../contexts/AuthContext';
import Layout from '../components/Layout';

describe('Layout', () => {
  it('should render navigation menu', () => {
    render(
      <MemoryRouter>
        <AuthProvider>
          <Routes>
            <Route path="/" element={<Layout />}>
              <Route index element={<div>content</div>} />
            </Route>
          </Routes>
        </AuthProvider>
      </MemoryRouter>,
    );

    expect(screen.getByText('Nova Proposta')).toBeInTheDocument();
    expect(screen.getByText('Historico')).toBeInTheDocument();
    expect(screen.getByText('E-mails')).toBeInTheDocument();
    expect(screen.getByText('Filas / Kafka')).toBeInTheDocument();
  });
});
