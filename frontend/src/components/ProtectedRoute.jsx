import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * ProtectedRoute — guards routes that require authentication.
 *
 * If user is not logged in → redirects to /login.
 * If user is logged in → renders the child component.
 *
 * Usage in App.jsx:
 *   <Route path="/dashboard" element={
 *     <ProtectedRoute><DashboardPage /></ProtectedRoute>
 *   } />
 */
export default function ProtectedRoute({ children }) {
  const { user, loading } = useAuth();

  // While checking auth status, show a loading spinner
  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full animate-spin mx-auto mb-4" />
          <p className="text-gray-500">Loading...</p>
        </div>
      </div>
    );
  }

  // Not authenticated → redirect to login page
  if (!user) {
    return <Navigate to="/login" replace />;
  }

  // Authenticated → render the protected page
  return children;
}

