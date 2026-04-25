import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import ReactMarkdown from 'react-markdown'
import type { ResearchReport } from '../types'
import { PHASE_LABELS } from '../types'
import { api } from '../services/api'
import { formatDate, formatCurrency, phaseCount } from '../lib/format'

const FINAL_TAB = 'FINAL_REPORT'

export default function ReportViewer() {
  const { id } = useParams<{ id: string }>()
  const [report, setReport] = useState<ResearchReport | null>(null)
  const [activeTab, setActiveTab] = useState<string>(FINAL_TAB)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!id) return
    api
      .getReport(id)
      .then(r => {
        setReport(r)
        setActiveTab(r.finalBriefing ? FINAL_TAB : 'PORTFOLIO_CONSTRUCTION')
        setLoading(false)
      })
      .catch(() => setLoading(false))
  }, [id])

  if (loading) {
    return (
      <div className="loading-overlay">
        <div className="spinner" style={{ width: 36, height: 36 }} />
        <span>Loading report…</span>
      </div>
    )
  }

  if (!report) {
    return (
      <div className="card">
        <p>
          Report not found. <Link to="/">Go back</Link>
        </p>
      </div>
    )
  }

  const { ok, failed } = phaseCount(report.phases)
  const phaseKeys = Object.keys(PHASE_LABELS)
  const currentIsPhase = activeTab !== FINAL_TAB
  const currentPhase = currentIsPhase ? report.phases[activeTab] : null

  return (
    <div>
      {/* ── Breadcrumb + profile summary ── */}
      <div className="rv-header">
        <Link to="/" className="rv-back">← Dashboard</Link>
        <div className="rv-profile">
          <span className="rv-profile-chip">{report.profile.country}</span>
          <span className="rv-profile-chip">{report.profile.riskTolerance}</span>
          <span className="rv-profile-chip">{report.profile.goal}</span>
          <span className="rv-profile-chip">{report.profile.timelineYears}yr</span>
          <span className="rv-profile-chip">{formatCurrency(report.profile.investmentAmount)}</span>
          {report.profile.sectorInterests.map(s => (
            <span key={s} className="rv-profile-chip rv-chip-sector">{s}</span>
          ))}
        </div>
        <div className="rv-meta">
          <span className={`badge ${failed > 0 ? 'badge-warning' : 'badge-success'}`}>
            {ok}/{ok + failed} phases OK
          </span>
          <span className="rv-date">{formatDate(report.generatedAt)}</span>
        </div>
      </div>

      {/* ── Tabs ── */}
      <div className="phase-tabs">
        {report.finalBriefing && (
          <button
            className={`phase-tab phase-tab--final ${activeTab === FINAL_TAB ? 'active' : ''}`}
            onClick={() => setActiveTab(FINAL_TAB)}
          >
            Final Report
          </button>
        )}
        {phaseKeys.map(key => {
          const phase = report.phases[key]
          return (
            <button
              key={key}
              className={`phase-tab ${activeTab === key ? 'active' : ''} ${phase && !phase.success ? 'failed' : ''}`}
              onClick={() => setActiveTab(key)}
            >
              {PHASE_LABELS[key]}
              {phase && !phase.success && ' ⚠'}
            </button>
          )
        })}
      </div>

      {/* ── Content ── */}
      <div className="card">
        {activeTab === FINAL_TAB ? (
          <>
            <div className="card-title">Final Investment Briefing</div>
            <div className="markdown">
              <ReactMarkdown>{report.finalBriefing}</ReactMarkdown>
            </div>
          </>
        ) : !currentPhase ? (
          <>
            <div className="card-title">{PHASE_LABELS[activeTab]}</div>
            <p style={{ color: 'var(--text-muted)' }}>No data for this phase.</p>
          </>
        ) : (
          <>
            <div className="card-title">{PHASE_LABELS[activeTab]}</div>
            {!currentPhase.success ? (
              <div style={{ color: 'var(--danger)' }}>
                Phase failed: {currentPhase.errorMessage}
              </div>
            ) : (
              <>
                {currentPhase.searchQueries?.length > 0 && (
                  <details className="search-queries">
                    <summary>
                      {currentPhase.searchQueries.length} search queries used
                    </summary>
                    <ul>
                      {currentPhase.searchQueries.map((q, i) => (
                        <li key={i}>{q}</li>
                      ))}
                    </ul>
                  </details>
                )}
                <div className="markdown">
                  <ReactMarkdown>{currentPhase.synthesis}</ReactMarkdown>
                </div>
              </>
            )}
          </>
        )}
      </div>
    </div>
  )
}
