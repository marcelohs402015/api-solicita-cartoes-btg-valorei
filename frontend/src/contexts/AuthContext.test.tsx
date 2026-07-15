import { describe, expect, it } from 'vitest';
import { act, renderHook } from '@testing-library/react';
import { AuthProvider, useAuth } from '../contexts/AuthContext';

describe('AuthContext', () => {
  it('should toggle authentication state', () => {
    const { result } = renderHook(() => useAuth(), {
      wrapper: AuthProvider,
    });

    expect(result.current.authenticated).toBe(false);

    act(() => result.current.login());
    expect(result.current.authenticated).toBe(true);

    act(() => result.current.logout());
    expect(result.current.authenticated).toBe(false);
  });
});
