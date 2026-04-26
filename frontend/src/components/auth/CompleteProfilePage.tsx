import { useEffect, useRef, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { PhoneAuthProvider, RecaptchaVerifier } from 'firebase/auth'
import { auth } from '../../firebase'

const PROVINCES   = ['Alberta','British Columbia','Manitoba','New Brunswick','Newfoundland and Labrador','Nova Scotia','Ontario','Prince Edward Island','Quebec','Saskatchewan','Northwest Territories','Nunavut','Yukon']
const AGE_RANGES  = ['18–25','26–35','36–45','46–55','56–65','65+']
const PROFESSIONS = ['Technology','Finance / Banking','Healthcare','Education','Real Estate','Retail / Sales','Self-employed','Student','Retired','Other']

export default function CompleteProfilePage() {
  const navigate  = useNavigate()
  const location  = useLocation()
  const prefill   = (location.state || {}) as { firstName?: string; lastName?: string; email?: string }
  const recaptchaRef = useRef<RecaptchaVerifier | null>(null)

  const [form, setForm] = useState({
    firstName: prefill.firstName || '',
    lastName:  prefill.lastName  || '',
    phone: '', province: '', ageRange: '', profession: '',
  })
  const [error, setError]     = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    recaptchaRef.current = new RecaptchaVerifier(auth, 'recaptcha-container-cp', { size: 'invisible' })
    return () => { recaptchaRef.current?.clear() }
  }, [])

  const set = (k: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) =>
    setForm(f => ({ ...f, [k]: e.target.value }))

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    const rawPhone = form.phone.replace(/\D/g, '')
    if (rawPhone.length !== 10) { setError('Enter a valid 10-digit Canadian phone number.'); return }

    setLoading(true)
    try {
      const provider = new PhoneAuthProvider(auth)
      const verificationId = await provider.verifyPhoneNumber(`+1${rawPhone}`, recaptchaRef.current!)
      navigate('/verify-phone', {
        state: {
          verificationId,
          phone: rawPhone,
          profileData: {
            email: prefill.email || auth.currentUser?.email || '',
            firstName: form.firstName,
            lastName:  form.lastName,
            phone: rawPhone,
            country: 'Canada',
            province: form.province,
            ageRange: form.ageRange,
            profession: form.profession,
          },
          skipEmailVerify: true, // Google users already have verified email
        }
      })
    } catch (err: any) {
      setError('Failed to send SMS. Check your phone number and try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card auth-card--wide">
        <div className="auth-logo">👤</div>
        <h1 className="auth-title">Complete your profile</h1>
        <p className="auth-subtitle">Just a few more details to finish setting up your account.</p>

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
              <label>Phone Number (Canada)</label>
              <div className="phone-input">
                <span className="phone-prefix">+1</span>
                <input value={form.phone} onChange={set('phone')} placeholder="(416) 555-0100" required maxLength={14} />
              </div>
            </div>
            <div className="form-group">
              <label>Province <span className="optional-label">optional</span></label>
              <select value={form.province} onChange={set('province')}>
                <option value="">Select…</option>
                {PROVINCES.map(p => <option key={p}>{p}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label>Age Range <span className="optional-label">optional</span></label>
              <select value={form.ageRange} onChange={set('ageRange')}>
                <option value="">Select…</option>
                {AGE_RANGES.map(a => <option key={a}>{a}</option>)}
              </select>
            </div>
            <div className="form-group auth-col-full">
              <label>Profession <span className="optional-label">optional</span></label>
              <select value={form.profession} onChange={set('profession')}>
                <option value="">Select…</option>
                {PROFESSIONS.map(p => <option key={p}>{p}</option>)}
              </select>
            </div>
          </div>

          <button type="submit" className="btn btn-primary btn-full" disabled={loading} style={{ marginTop: 20 }}>
            {loading ? <><div className="spinner" /> Sending SMS…</> : 'Continue →'}
          </button>
        </form>
        <div id="recaptcha-container-cp" />
      </div>
    </div>
  )
}
