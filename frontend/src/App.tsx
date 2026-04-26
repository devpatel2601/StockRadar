import { Routes, Route, Link, useNavigate } from 'react-router-dom'
import { signOut } from 'firebase/auth'
import { auth } from './firebase'
import { AuthProvider, useAuth } from './context/AuthContext'
import ProtectedRoute from './components/ProtectedRoute'
import Dashboard from './components/Dashboard'
import ReportViewer from './components/ReportViewer'
import LoginPage from './components/auth/LoginPage'
import SignupPage from './components/auth/SignupPage'
import PhoneVerifyPage from './components/auth/PhoneVerifyPage'
import EmailVerifyPage from './components/auth/EmailVerifyPage'
import ForgotPasswordPage from './components/auth/ForgotPasswordPage'
import CompleteProfilePage from './components/auth/CompleteProfilePage'
import './index.css'

export default function App() {
  return (
    <AuthProvider>
      <AppShell />
    </AuthProvider>
  )
}

function AppShell() {
  const { user } = useAuth()
  const navigate  = useNavigate()

  async function handleLogout() {
    await signOut(auth)
    navigate('/login')
  }

  return (
    <div className="app">
      <header className="app-header">
        <Link to="/" className="app-title">StockRadar</Link>
        <div className="app-header-right">
          {user ? (
            <>
              <span className="app-user-email">{user.displayName || user.email}</span>
              <button className="btn btn-secondary btn-sm" onClick={handleLogout}>
                Sign out
              </button>
            </>
          ) : (
            <>
              <Link to="/login"  className="btn btn-secondary btn-sm">Sign in</Link>
              <Link to="/signup" className="btn btn-primary  btn-sm">Sign up</Link>
            </>
          )}
        </div>
      </header>

      <main className="app-main">
        <Routes>
          {/* ── Public ─────────────────────────────────────────────────── */}
          <Route path="/login"           element={<LoginPage />} />
          <Route path="/signup"          element={<SignupPage />} />
          <Route path="/verify-phone"    element={<PhoneVerifyPage />} />
          <Route path="/verify-email"    element={<EmailVerifyPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/complete-profile" element={<CompleteProfilePage />} />

          {/* ── Protected ──────────────────────────────────────────────── */}
          <Route path="/" element={
            <ProtectedRoute><Dashboard /></ProtectedRoute>
          } />
          <Route path="/report/:id" element={
            <ProtectedRoute><ReportViewer /></ProtectedRoute>
          } />
        </Routes>
      </main>
    </div>
  )
}
