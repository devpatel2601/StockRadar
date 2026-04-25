import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import ReactMarkdown from 'react-markdown'
import type { ResearchReport } from '../types'
import { PHASE_LABELS } from '../types'
import { api } from '../services/api'

export default function ReportViewer() {
  const { id } = useParams<{ id: string }>()
  const [report, setReport] = useState<ResearchReport | null>(null)
  const [activePhase, setActivePhase] = useState<string>('PORTFOLIO_CONSTRUCTION')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!id) return
    api.getReport(id)
      .then(r => { setReport(r); setLoading(false) })
      .catch(() => setLoading(false))
  }, [id])

  if (loading) {
    return (
      <div className="loading-overlay">
        <div className="spinner" style={{ width: 36, height: 36 }} />
        <span>Loading report...</span>
      </div>
    )
  }

  if (!report) {
    return (
      <div className="card">
        <p>Report not found. <Link to="/">Go back</Link></p>
      </div>
    )
  }

  const phaseKeys = Object.keys(PHASE_LABELS)
  const currentPhase = report.phases[activePhase]

  return (
    <div>
      <div style={{ marginBottom: 20, display: 'flex', alignItems: 'center', gap: 12 }}>
        <Link to="/" style={{ fontSize: 13, color: 'var(--text-muted)' }}>← Dashboard</Link>
        <span style={{ color: 'var(--border)' }}>|</span>
        <span style={{ fontSize: 13 }}>
          {report.profile.country} · {report.profile.riskTolerance} · {report.profile.goal} · {report.profile.timelineYears}yr
        </span>
        <span style={{ marginLeft: 'auto', fontSize: 12, color: 'var(--text-muted)' }}>
          Generated {new Date(report.generatedAt).toLocaleString()}
        </span>
      </div>

      <div className="phase-tabs">
        {phaseKeys.map(key => {
          const phase = report.phases[key]
          return (
            <button
              key={key}
              className={`phase-tab ${activePhase === key ? 'active' : ''} ${phase && !phase.success ? 'failed' : ''}`}
              onClick={() => setActivePhase(key)}
            >
              {PHASE_LABELS[key]}
              {phase && !phase.success && ' ⚠'}
            </button>
          )
        })}
      </div>

      <div className="card">
        <div className="card-title">{PHASE_LABELS[activePhase]}</div>

        {!currentPhase ? (
          <p style={{ color: 'var(--text-muted)' }}>No data for this phase.</p>
        ) : !currentPhase.success ? (
          <div style={{ color: 'var(--danger)' }}>
            Phase failed: {currentPhase.errorMessage}
          </div>
        ) : (
          <>
            {currentPhase.searchQueries?.length > 0 && (
              <details style={{ marginBottom: 16 }}>
                <summary style={{ fontSize: 12, color: 'var(--text-muted)', cursor: 'pointer' }}>
                  {currentPhase.searchQueries.length} search queries used
                </summary>
                <ul style={{ marginTop: 8, paddingLeft: 20 }}>
                  {currentPhase.searchQueries.map((q, i) => (
                    <li key={i} style={{ fontSize: 12, color: 'var(--text-muted)', marginBottom: 2 }}>{q}</li>
                  ))}
                </ul>
              </details>
            )}
            <div className="markdown">
              <ReactMarkdown>{currentPhase.synthesis}</ReactMarkdown>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
