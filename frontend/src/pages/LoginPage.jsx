import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * LoginPage — two-step login form.
 *
 * Step 1: Enter email → click "Send OTP"
 * Step 2: Enter 6-digit OTP → click "Verify" → logged in → redirect to dashboard
 */
export default function LoginPage() {
  const { sendOtp, verifyOtp, user } = useAuth();
  const navigate = useNavigate();

  // If already logged in, redirect to dashboard
  if (user) {
    navigate('/dashboard', { replace: true });
  }

  const [step, setStep] = useState(1); // 1 = email, 2 = otp
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [name, setName] = useState('');
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSendOtp(e) {
    e.preventDefault();
    setError('');
    setMessage('');
    setLoading(true);

    try {
      const data = await sendOtp(email);
      setMessage(data.message);
      setStep(2);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to send OTP. Please try again.');
    } finally {
      setLoading(false);
    }
  }

  async function handleVerifyOtp(e) {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await verifyOtp(email, otp, name || undefined, 'STUDENT');
      navigate('/dashboard', { replace: true });
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid OTP. Please try again.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 px-4">
      <div className="w-full max-w-md">

        {/* Logo & Title */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-indigo-700 mb-2">StudentSeva</h1>
          <p className="text-gray-500">Hyperlocal services for students</p>
        </div>

        {/* Card */}
        <div className="bg-white rounded-2xl shadow-lg p-8">

          {step === 1 ? (
            /* ── Step 1: Email Input ──────────────────────────── */
            <form onSubmit={handleSendOtp}>
              <h2 className="text-xl font-semibold text-gray-800 mb-1">Welcome</h2>
              <p className="text-sm text-gray-500 mb-6">
                Enter your email to receive a verification code
              </p>

              <label className="block text-sm font-medium text-gray-700 mb-1">
                Email Address
              </label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@college.edu"
                required
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition mb-4"
              />

              <button
                type="submit"
                disabled={loading || !email}
                className="w-full bg-indigo-600 text-white py-3 rounded-lg font-medium hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
              >
                {loading ? 'Sending...' : 'Send Verification Code'}
              </button>
            </form>
          ) : (
            /* ── Step 2: OTP Verification ──────────────────────── */
            <form onSubmit={handleVerifyOtp}>
              <h2 className="text-xl font-semibold text-gray-800 mb-1">Verify Code</h2>
              <p className="text-sm text-gray-500 mb-6">
                Enter the 6-digit code sent to <span className="font-medium text-gray-700">{email}</span>
              </p>

              <label className="block text-sm font-medium text-gray-700 mb-1">
                Your Name (optional)
              </label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Vinay Patel"
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition mb-4"
              />

              <label className="block text-sm font-medium text-gray-700 mb-1">
                Verification Code
              </label>
              <input
                type="text"
                value={otp}
                onChange={(e) => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
                placeholder="123456"
                maxLength={6}
                required
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition text-center text-2xl tracking-widest font-mono mb-4"
              />

              <button
                type="submit"
                disabled={loading || otp.length !== 6}
                className="w-full bg-indigo-600 text-white py-3 rounded-lg font-medium hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
              >
                {loading ? 'Verifying...' : 'Verify & Sign In'}
              </button>

              <button
                type="button"
                onClick={() => { setStep(1); setOtp(''); setError(''); }}
                className="w-full mt-3 text-sm text-indigo-600 hover:text-indigo-800 transition"
              >
                ← Use a different email
              </button>
            </form>
          )}

          {/* Error Message */}
          {error && (
            <div className="mt-4 p-3 bg-red-50 border border-red-200 rounded-lg">
              <p className="text-sm text-red-600">{error}</p>
            </div>
          )}

          {/* Success Message */}
          {message && step === 2 && (
            <div className="mt-4 p-3 bg-green-50 border border-green-200 rounded-lg">
              <p className="text-sm text-green-600">{message}</p>
            </div>
          )}
        </div>

        <p className="text-center text-xs text-gray-400 mt-6">
          No password needed — we'll email you a secure code
        </p>
      </div>
    </div>
  );
}

