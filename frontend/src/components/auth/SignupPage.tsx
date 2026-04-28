import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import {
  createUserWithEmailAndPassword,
  signInWithPopup,
  GoogleAuthProvider,
  PhoneAuthProvider,
  RecaptchaVerifier,
} from 'firebase/auth'
import { auth } from '../../firebase'

const googleProvider = new GoogleAuthProvider()

const COMMON_PASSWORDS = [
  'password','password1','12345678','123456789','qwerty123','iloveyou',
  'admin123','letmein1','welcome1','monkey123','dragon123','master123',
]

const PROVINCES = [
  'Alberta','British Columbia','Manitoba','New Brunswick',
  'Newfoundland and Labrador','Nova Scotia','Ontario','Prince Edward Island',
  'Quebec','Saskatchewan','Northwest Territories','Nunavut','Yukon',
]
const AGE_RANGES   = ['18–25','26–35','36–45','46–55','56–65','65+']
const PROFESSIONS  = [
  'Technology','Finance / Banking','Healthcare','Education',
  'Real Estate','Retail / Sales','Self-employed','Student','Retired','Other',
]

interface Form {
  email: string; password: string; firstName: string; lastName: string; phone: string
  country: string; province: string; ageRange: string; profession: string
}

export default function SignupPage() {
  const navigate = useNavigate()
  const recaptchaRef = useRef<RecaptchaVerifier | null>(null)

  const [form, setForm] = useState<Form>({
    email: '', password: '', firstName: '', lastName: '', phone: '',
    country: 'Canada', province: '', ageRange: '', profession: '',
  })
  const [error, setError]     = useState('')
  const [loading, setLoading] = useState(false)
  const [showOptional, setShowOptional] = useState(false)

  useEffect(() => {
    recaptchaRef.current = new RecaptchaVerifier(auth, 'recaptcha-container', { size: 'invisible' })
    return () => { recaptchaRef.current?.clear() }
  }, [])

  const set = (k: keyof Form) => (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) =>
    setForm(f => ({ ...f, [k]: e.target.value }))

  function pwStrength(pw: string): 'weak' | 'medium' | 'strong' {
    if (pw.length < 8) return 'weak'
    const checks = [/[A-Z]/, /[a-z]/, /[0-9]/, /[^A-Za-z0-9]/].filter(r => r.test(pw)).length
    return checks >= 4 ? 'strong' : checks >= 2 ? 'medium' : 'weak'
  }

  function validatePassword(pw: string): string | null {
    if (pw.length < 8)             return 'Minimum 8 characters.'
    if (!/[A-Z]/.test(pw))         return 'Add at least one uppercase letter.'
    if (!/[a-z]/.test(pw))         return 'Add at least one lowercase letter.'
    if (!/[0-9]/.test(pw))         return 'Add at least one number.'
    if (!/[^A-Za-z0-9]/.test(pw))  return 'Add at least one symbol (!@#$…).'
    if (COMMON_PASSWORDS.includes(pw.toLowerCase())) return 'Password is too common. Choose a stronger one.'
    return null
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError('')

    const pwErr = validatePassword(form.password)
    if (pwErr) { setError(pwErr); return }

    const rawPhone = form.phone.replace(/\D/g, '')
    if (rawPhone.length !== 10) { setError('Enter a valid 10-digit Canadian phone number.'); return }

    setLoading(true)
    try {
      // 1. Create Firebase email/password account
      await createUserWithEmailAndPassword(auth, form.email, form.password)

      // 2. Send phone OTP via Firebase
      const provider = new PhoneAuthProvider(auth)
      const verificationId = await provider.verifyPhoneNumber(
        `+1${rawPhone}`, recaptchaRef.current!
      )

      // 3. Navigate to OTP screen with all data
      navigate('/verify-phone', {
        state: { verificationId, phone: rawPhone, profileData: { ...form, phone: rawPhone } }
      })
    } catch (err: any) {
      setError(friendlyError(err.code))
    } finally {
      setLoading(false)
    }
  }

  async function handleGoogle() {
    setError('')
    setLoading(true)
    try {
      const result = await signInWithPopup(auth, googleProvider)
      const { displayName, email } = result.user
      const [firstName = '', ...rest] = (displayName || '').split(' ')
      navigate('/complete-profile', { state: { firstName, lastName: rest.join(' '), email } })
    } catch (err: any) {
      setError(friendlyError(err.code))
    } finally {
      setLoading(false)
    }
  }

  const strength = pwStrength(form.password)

  return (
    <div className="auth-page">
      <div className="auth-card auth-card--wide">
        <div className="auth-logo">📈</div>
        <h1 className="auth-title">Create your account</h1>
        <p className="auth-subtitle">Free. Powered by AI. Built for Canadian investors.</p>

        {error && <div className="error-banner">{error}</div>}

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="auth-form-grid">
            <div className="form-group">
              <label>First Name</label>
              <input value={form.firstName} onChange={set('firstName')} placeholder="Dev" required />
            </div>
            <div className="form-group">
              <label>Last Name</label>
              <input value={form.lastName} onChange={set('lastName')} placeholder="Patel" required />
            </div>
            <div className="form-group auth-col-full">
              <label>Email</label>
              <input type="email" value={form.email} onChange={set('email')} placeholder="you@example.com" required />
            </div>
            <div className="form-group auth-col-full">
              <label>Password</label>
              <input type="password" value={form.password} onChange={set('password')} placeholder="Min 8 chars, upper, lower, number, symbol" required />
              {form.password && (
                <div className="pw-strength">
                  <div className={`pw-bar pw-bar--${strength}`} />
                  <span className={`pw-label pw-label--${strength}`}>
                    {strength.charAt(0).toUpperCase() + strength.slice(1)}
                  </span>
                </div>
              )}
            </div>
            <div className="form-group auth-col-full">
              <label>Phone Number (Canada)</label>
              <div className="phone-input">
                <span className="phone-prefix">+1</span>
                <input
                  value={form.phone}
                  onChange={set('phone')}
                  placeholder="(416) 555-0100"
                  required
                  maxLength={14}
                />
              </div>
            </div>
          </div>

          {/* Optional section */}
          <button
            type="button"
            className="auth-optional-toggle"
            onClick={() => setShowOptional(s => !s)}
          >
            {showOptional ? '▲' : '▼'} Optional info (help us personalise your experience)
          </button>

          {showOptional && (
            <div className="auth-form-grid" style={{ marginTop: 12 }}>
              <div className="form-group">
                <label>Province</label>
                <select value={form.province} onChange={set('province')}>
                  <option value="">Select…</option>
                  {PROVINCES.map(p => <option key={p}>{p}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label>Age Range</label>
                <select value={form.ageRange} onChange={set('ageRange')}>
                  <option value="">Select…</option>
                  {AGE_RANGES.map(a => <option key={a}>{a}</option>)}
                </select>
              </div>
              <div className="form-group auth-col-full">
                <label>Profession</label>
                <select value={form.profession} onChange={set('profession')}>
                  <option value="">Select…</option>
                  {PROFESSIONS.map(p => <option key={p}>{p}</option>)}
                </select>
              </div>
            </div>
          )}

          <button type="submit" className="btn btn-primary btn-full" disabled={loading} style={{ marginTop: 20 }}>
            {loading ? <><div className="spinner" /> Creating account…</> : 'Create account'}
          </button>
        </form>

        <div className="auth-divider"><span>or</span></div>

        <button className="btn btn-google btn-full" onClick={handleGoogle} disabled={loading}>
          <GoogleIcon /> Continue with Google
        </button>

        <p className="auth-footer">
          Already have an account? <Link to="/login" className="auth-link">Sign in</Link>
        </p>
      </div>
      <div id="recaptcha-container" />
    </div>
  )
}

function GoogleIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 18 18" style={{ marginRight: 8 }}>
      <path fill="#4285F4" d="M17.64 9.2c0-.637-.057-1.251-.164-1.84H9v3.481h4.844c-.209 1.125-.843 2.078-1.796 2.717v2.258h2.908c1.702-1.567 2.684-3.875 2.684-6.615z"/>
      <path fill="#34A853" d="M9 18c2.43 0 4.467-.806 5.956-2.184l-2.908-2.258c-.806.54-1.837.86-3.048.86-2.344 0-4.328-1.584-5.036-3.711H.957v2.332A8.997 8.997 0 0 0 9 18z"/>
      <path fill="#FBBC05" d="M3.964 10.707A5.41 5.41 0 0 1 3.682 9c0-.593.102-1.17.282-1.707V4.961H.957A8.996 8.996 0 0 0 0 9c0 1.452.348 2.827.957 4.039l3.007-2.332z"/>
      <path fill="#EA4335" d="M9 3.58c1.321 0 2.508.454 3.44 1.345l2.582-2.58C13.463.891 11.426 0 9 0A8.997 8.997 0 0 0 .957 4.961L3.964 7.293C4.672 5.163 6.656 3.58 9 3.58z"/>
    </svg>
  )
}

function friendlyError(code: string): string {
  switch (code) {
    case 'auth/email-already-in-use': return 'An account with this email already exists.'
    case 'auth/invalid-phone-number': return 'Invalid phone number. Use a valid Canadian number.'
    case 'auth/too-many-requests':    return 'Too many attempts. Please wait and try again.'
    case 'auth/popup-closed-by-user': return 'Google sign-in was cancelled.'
    default:                          return 'Something went wrong. Please try again.'
  }
}
