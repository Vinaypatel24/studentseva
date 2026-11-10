import { createContext, useContext, useState, useEffect } from 'react';
import api from '../api/axios';

/**
 * AuthContext — manages the user's login state across the entire React app.
 *
 * How it works:
 *   1. On app load, calls GET /api/v1/auth/me to check if the browser
 *      already has a valid nearbuzz_token cookie.
 *   2. If yes → user is logged in → stores user data in state.
 *   3. If no (401 response) → user is not logged in → user = null.
 *   4. Provides login/logout functions to all child components.
 *
 * Any component in the app can use:
 *   const { user, login, logout, loading } = useAuth();
 */

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // On first app load, check if user is already logged in (cookie exists)
  useEffect(() => {
    checkAuth();
  }, []);

  async function checkAuth() {
    try {
      const response = await api.get('/auth/me');
      setUser(response.data);
    } catch (error) {
      // 401 = not logged in — that's fine, not an error
      setUser(null);
    } finally {
      setLoading(false);
    }
  }

  async function sendOtp(email) {
    const response = await api.post('/auth/send-otp', { email });
    return response.data;
  }

  async function verifyOtp(email, otp, name, role) {
    const response = await api.post('/auth/verify-otp', { email, otp, name, role });
    setUser(response.data);
    return response.data;
  }

  async function logout() {
    try {
      await api.post('/auth/logout');
    } catch (error) {
      // Even if API call fails, clear local state
    }
    setUser(null);
  }

  const value = {
    user,
    loading,
    sendOtp,
    verifyOtp,
    logout,
    checkAuth,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

/**
 * Custom hook to access auth state from any component.
 * Usage: const { user, login, logout } = useAuth();
 */
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

