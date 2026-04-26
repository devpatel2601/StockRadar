import { useNavigate } from 'react-router-dom'
import type { InvestorProfile } from '../types'
import { useReports } from '../hooks/useReports'
import { formatDate, formatCurrency } from '../lib/format'
import ProfileForm from './ProfileForm'

export default function Dashboard() {
  const navigate = useNavigate()
  const { reports, analyzing, error, runAnalysis, deleteReport } = useReports()

  async function handleAnalyze(profile: InvestorProfile) {
    const report = await runAnalysis(profile)
    if (report) navigate(`/report/${report.id}`)
  }

  async function handleDelete(id: string, e: React.MouseEvent) {
    e.stopPropagation()
    await deleteReport(id)
  }

  return (
    <>
      <div className="card">
        <div className="card-title">New Analysis</div>
        {error && <div className="error-banner">{error}</div>}
        <ProfileForm onSubmit={handleAnalyze} loading={analyzing} />
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
                <div className="report-card-top">
                  <div>
                    <div className="report-card-heading">
                      {r.profile.country} &middot; {r.profile.riskTolerance}
                    </div>
                    <div className="report-card-meta">
                      {r.profile.goal} &middot; {r.profile.timelineYears}yr
                      &middot; {formatCurrency(r.profile.investmentAmount)}
                    </div>
                    <div className="report-card-sectors">
                      {r.profile.sectorInterests.slice(0, 3).join(', ')}
                      {r.profile.sectorInterests.length > 3 && ` +${r.profile.sectorInterests.length - 3}`}
                    </div>
                  </div>
                  <span
                    className={`badge ${
                      r.status === 'COMPLETED' ? 'badge-success'
                      : r.status === 'FAILED'    ? 'badge-danger'
                      : 'badge-warning'
                    }`}
                  >
                    {r.status}
                  </span>
                </div>

                <div className="report-card-footer">
                  <span>{formatDate(r.generatedAt)}</span>
                  <button
                    className="btn btn-danger btn-sm"
                    onClick={e => handleDelete(r.id, e)}
                  >
                    Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </>
  )
}
