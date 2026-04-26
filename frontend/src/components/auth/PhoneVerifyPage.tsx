import { useEffect, useRef, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { PhoneAuthProvider, linkWithCredential, sendEmailVerification } from 'firebase/auth'
import { auth } from '../../firebase'
import { api } from '../../services/api'

const OTP_LENGTH = 6
const RESEND_COOLDOWN = 60

export default function PhoneVerifyPage() {
  const navigate  = useNavigate()
  const location  = useLocation()
  const { verificationId, phone, profileData } = (location.state || {}) as any

  const [otp, setOtp]         = useState<string[]>(Array(OTP_LENGTH).fill(''))
  const [error, setError]     = useState('')
  const [loading, setLoading] = useState(false)
  const [cooldown, setCooldown] = useState(RESEND_COOLDOWN)
  const inputs = useRef<(HTMLInputElement | null)[]>([])

  useEffect(() => {
    if (!verificationId) { navigate('/signup'); return }
    inputs.current[0]?.focus()
    const t = setInterval(() => setCooldown(c => (c > 0 ? c - 1 : 0)), 1000)
    return () => clearInterval(t)
  }, [verificationId, navigate])

  function handleOtpChange(index: number, value: string) {
    if (!/^\d*$/.test(value)) return
    const next = [...otp]
    next[index] = value.slice(-1)
    setOtp(next)
    if (value && index < OTP_LENGTH - 1) inputs.current[index + 1]?.focus()
  }

  function handleKeyDown(index: number, e: React.KeyboardEvent) {
    if (e.key === 'Backspace' && !otp[index] && index > 0) {
      inputs.current[index - 1]?.focus()
    }
  }

  async function handleVerify() {
    const code = otp.join('')
    if (code.length < OTP_LENGTH) { setError('Enter the full 6-digit code.'); return }
    setError('')
    setLoading(true)
    try {
      const credential = PhoneAuthProvider.credential(verificationId, code)
      await linkWithCredential(auth.currentUser!, credential)

      // Save profile to our backend
      await api.createProfile({ ...profileData, phone })

      // Send email verification
      await sendEmailVerification(auth.currentUser!)

      navigate('/verify-email')
    } catch (err: any) {
      if (err.code === 'auth/invalid-verification-code') setError('Incorrect code. Please try again.')
      else if (err.code === 'auth/code-expired')         setError('Code expired. Please request a new one.')
      else if (err.response?.status === 409)             setError('This phone number is already registered.')
      else setError('Something went wrong. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  function handlePaste(e: React.ClipboardEvent) {
    const pasted = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, OTP_LENGTH)
    if (pasted.length === OTP_LENGTH) {
      setOtp(pasted.split(''))
      inputs.current[OTP_LENGTH - 1]?.focus()
    }
  }

  const formattedPhone = phone
    ? `+1 (${phone.slice(0,3)}) ${phone.slice(3,6)}-${phone.slice(6)}`
    : ''

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-logo">📱</div>
        <h1 className="auth-title">Verify your phone</h1>
        <p className="auth-subtitle">
          Enter the 6-digit code sent to<br />
          <strong>{formattedPhone}</strong>
        </p>

        {error && <div className="error-banner">{error}</div>}

        <div className="otp-grid" onPaste={handlePaste}>
          {otp.map((digit, i) => (
            <input
              key={i}
              ref={el => { inputs.current[i] = el }}
              className="otp-box"
              type="text"
              inputMode="numeric"
              maxLength={1}
              value={digit}
              onChange={e => handleOtpChange(i, e.target.value)}
              onKeyDown={e => handleKeyDown(i, e)}
            />
          ))}
        </div>

        <button
          className="btn btn-primary btn-full"
          onClick={handleVerify}
          disabled={loading || otp.join('').length < OTP_LENGTH}
          style={{ marginTop: 24 }}
        >
          {loading ? <><div className="spinner" /> Verifying…</> : 'Verify phone'}
        </button>

        <p className="auth-footer" style={{ marginTop: 16 }}>
          {cooldown > 0
            ? `Resend code in ${cooldown}s`
            : <button className="auth-link" style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 0 }}
                onClick={() => { navigate(-1) }}>
                Resend code
              </button>
          }
        </p>
      </div>
    </div>
  )
}
