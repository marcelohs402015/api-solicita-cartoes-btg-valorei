import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import FilasPage from '../pages/FilasPage';
import * as api from '../services/api';

vi.mock('../services/api', () => ({
  fetchQueueStatus: vi.fn(),
  fetchEvents: vi.fn(),
}));

describe('FilasPage', () => {
  beforeEach(() => {
    vi.mocked(api.fetchQueueStatus).mockResolvedValue({
      topic: 'proposals.events',
      consumers: [{ name: 'Worker Historico', groupId: 'historico-worker-group', status: 'ACTIVE' }],
      recentMessagesCount: 2,
    });
    vi.mocked(api.fetchEvents).mockResolvedValue([]);
  });

  it('should render queue status', async () => {
    render(<FilasPage />);
    await waitFor(() => {
      expect(screen.getByText('proposals.events')).toBeInTheDocument();
      expect(screen.getByText('Worker Historico')).toBeInTheDocument();
    });
  });
});
