import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { sendEmailVerification } from 'firebase/auth'
import { auth } from '../../firebase'

export default function EmailVerifyPage() {
  const navigate   = useNavigate()
  const [resent, setResent]   = useState(false)
  const [cooldown, setCooldown] = useState(0)
  const user = auth.currentUser

  // Poll Firebase every 3s — redirect when email is verified
  useEffect(() => {
    const t = setInterval(async () => {
      await user?.reload()
      if (auth.currentUser?.emailVerified) {
        clearInterval(t)
        navigate('/')
      }
    }, 3000)
    return () => clearInterval(t)
  }, [navigate, user])

  // Cooldown counter
  useEffect(() => {
    if (cooldown <= 0) return
    const t = setInterval(() => setCooldown(c => c - 1), 1000)
    return () => clearInterval(t)
  }, [cooldown])

  async function handleResend() {
    if (!user) return
    await sendEmailVerification(user)
    setResent(true)
    setCooldown(60)
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-logo">✉️</div>
        <h1 className="auth-title">Check your inbox</h1>
        <p className="auth-subtitle">
          We sent a verification link to<br />
          <strong>{user?.email}</strong>
        </p>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', marginTop: 12, textAlign: 'center' }}>
          Click the link in the email to verify your address.<br />
          This page will redirect automatically.
        </p>

        {resent && <div className="error-banner" style={{ background: 'var(--success-bg)', color: 'var(--success)' }}>Verification email resent!</div>}

        <button
          className="btn btn-secondary btn-full"
          onClick={handleResend}
          disabled={cooldown > 0}
          style={{ marginTop: 24 }}
        >
          {cooldown > 0 ? `Resend in ${cooldown}s` : 'Resend verification email'}
        </button>

        <p className="auth-footer" style={{ marginTop: 16 }}>
          Wrong email?{' '}
          <button
            className="auth-link"
            style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 0 }}
            onClick={() => auth.signOut().then(() => navigate('/signup'))}
          >
            Start over
          </button>
        </p>
      </div>
    </div>
  )
}
