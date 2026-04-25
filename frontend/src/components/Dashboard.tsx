import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import type { InvestorProfile, ResearchReport } from '../types'
import { api } from '../services/api'
import ProfileForm from './ProfileForm'

export default function Dashboard() {
  const navigate = useNavigate()
  const [reports, setReports] = useState<ResearchReport[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    api.getReports()
      .then(setReports)
      .catch(() => {})
  }, [])

  async function handleAnalyze(profile: InvestorProfile) {
    setLoading(true)
    setError(null)
    try {
      const report = await api.runAnalysis(profile)
      setReports(prev => [report, ...prev])
      navigate(`/report/${report.id}`)
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Analysis failed. Is the backend running?'
      setError(msg)
    } finally {
      setLoading(false)
    }
  }

  async function handleDelete(id: string, e: React.MouseEvent) {
    e.stopPropagation()
    await api.deleteReport(id)
    setReports(prev => prev.filter(r => r.id !== id))
  }

  return (
    <div>
      <div className="card">
        <div className="card-title">New Analysis</div>
        {error && (
          <div style={{ color: 'var(--danger)', marginBottom: 16, fontSize: 13 }}>
            {error}
          </div>
        )}
        <ProfileForm onSubmit={handleAnalyze} loading={loading} />
      </div>

      {reports.length > 0 && (
        <div className="card">
          <div className="card-title">Past Reports</div>
          <div className="reports-grid">
            {reports.map(r => (
              <div
                key={r.id}
                className="report-card"
                onClick={() => navigate(`/report/${r.id}`)}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <div style={{ fontWeight: 600, marginBottom: 4 }}>
                      {r.profile.country} · {r.profile.riskTolerance}
                    </div>
                    <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>
                      {r.profile.goal} · {r.profile.timelineYears}yr · ${r.profile.investmentAmount.toLocaleString()}
                    </div>
                  </div>
                  <span className={`badge ${r.status === 'COMPLETED' ? 'badge-success' : r.status === 'FAILED' ? 'badge-danger' : 'badge-warning'}`}>
                    {r.status}
                  </span>
                </div>
                <div className="report-meta" style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>{new Date(r.generatedAt).toLocaleString()}</span>
                  <button
                    className="btn btn-danger btn-sm"
                    onClick={(e) => handleDelete(r.id, e)}
                  >
                    Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
