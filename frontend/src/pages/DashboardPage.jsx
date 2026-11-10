import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

/**
 * DashboardPage — the main protected page after login.
 *
 * Shows the user's profile information fetched from the JWT session.
 * Will be expanded in later days to show nearby services, listings, etc.
 */
export default function DashboardPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  async function handleLogout() {
    await logout();
    navigate('/login', { replace: true });
  }

  if (!user) return null;

  // Role badge color
  const roleBadge = {
    STUDENT: 'bg-blue-100 text-blue-700',
    VENDOR: 'bg-green-100 text-green-700',
    ADMIN: 'bg-red-100 text-red-700',
  };

  return (
    <div className="min-h-screen bg-gray-50">

      {/* ── Top Navigation Bar ────────────────────────────────────── */}
      <nav className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-4xl mx-auto px-4 py-3 flex items-center justify-between">
          <h1 className="text-xl font-bold text-indigo-700">StudentSeva</h1>
          <button
            onClick={handleLogout}
            className="text-sm text-gray-500 hover:text-red-600 transition font-medium"
          >
            Sign Out
          </button>
        </div>
      </nav>

      {/* ── Main Content ──────────────────────────────────────────── */}
      <main className="max-w-4xl mx-auto px-4 py-8">

        {/* Welcome Card */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-8 mb-6">
          <div className="flex items-center gap-4 mb-6">
            {/* Avatar circle with first letter */}
            <div className="w-16 h-16 rounded-full bg-indigo-100 flex items-center justify-center">
              <span className="text-2xl font-bold text-indigo-600">
                {user.name ? user.name.charAt(0).toUpperCase() : '?'}
              </span>
            </div>
            <div>
              <h2 className="text-2xl font-bold text-gray-800">
                Welcome, {user.name || 'User'}! 👋
              </h2>
              <p className="text-gray-500">{user.email}</p>
            </div>
          </div>

          {/* Profile Details */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div className="bg-gray-50 rounded-lg p-4">
              <p className="text-xs text-gray-400 uppercase tracking-wide mb-1">Role</p>
              <span className={`inline-block px-3 py-1 rounded-full text-sm font-medium ${roleBadge[user.role] || 'bg-gray-100 text-gray-700'}`}>
                {user.role}
              </span>
            </div>
            <div className="bg-gray-50 rounded-lg p-4">
              <p className="text-xs text-gray-400 uppercase tracking-wide mb-1">Status</p>
              <span className="inline-block px-3 py-1 rounded-full text-sm font-medium bg-emerald-100 text-emerald-700">
                {user.status}
              </span>
            </div>
            <div className="bg-gray-50 rounded-lg p-4">
              <p className="text-xs text-gray-400 uppercase tracking-wide mb-1">User ID</p>
              <p className="text-sm text-gray-600 font-mono truncate">{user.id}</p>
            </div>
          </div>
        </div>

        {/* Placeholder for future features */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-8 text-center">
          <p className="text-gray-400 text-lg mb-2">🚧 Coming Soon</p>
          <p className="text-gray-500 text-sm">
            Nearby services, business listings, and more will appear here in upcoming days.
          </p>
        </div>
      </main>
    </div>
  );
}

