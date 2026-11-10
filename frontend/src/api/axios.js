import axios from 'axios';

/**
 * Axios instance configured for our Spring Boot backend.
 *
 * withCredentials: true  →  tells the browser to send the httpOnly cookie
 *                           (nearbuzz_token) with every request automatically.
 *                           Without this, the cookie stays in the browser
 *                           but never gets sent to the API.
 *
 * baseURL: '/api/v1'     →  in development, Vite's proxy forwards this to
 *                           http://localhost:8080/api/v1 (configured in vite.config.js).
 *                           In production, this will point to the Render backend URL.
 */
const api = axios.create({
  baseURL: '/api/v1',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

export default api;

