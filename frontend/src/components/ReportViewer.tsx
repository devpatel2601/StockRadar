import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import ReactMarkdown from 'react-markdown'
import type { ResearchReport } from '../types'
import { PHASE_LABELS } from '../types'
import { api } from '../services/api'
import { formatDate, formatCurrency, phaseCount } from '../lib/format'
import AnalysisProgress from './AnalysisProgress'

const FINAL_TAB = 'FINAL_REPORT'
const POLL_INTERVAL_MS = 3_000
const IN_FLIGHT = new Set(['PENDING', 'IN_PROGRESS'])

export default function ReportViewer() {
  const { id } = useParams<{ id: string }>()
  const [report, setReport] = useState<ResearchReport | null>(null)
  const [activeTab, setActiveTab] = useState<string>(FINAL_TAB)
  const [loading, setLoading] = useState(true)

  // Initial load
  useEffect(() => {
    if (!id) return
    api.getReport(id)
      .then(r => {
        setReport(r)
        setActiveTab(r.finalBriefing ? FINAL_TAB : 'PORTFOLIO_CONSTRUCTION')
        setLoading(false)
      })
      .catch(() => setLoading(false))
  }, [id])

  // Poll while PENDING or IN_PROGRESS
  useEffect(() => {
    if (!report || !IN_FLIGHT.has(report.status)) return

    const timer = setInterval(() => {
      api.getReport(report.id).then(updated => {
        setReport(updated)
        if (!IN_FLIGHT.has(updated.status)) {
          clearInterval(timer)
          if (updated.finalBriefing) setActiveTab(FINAL_TAB)
        }
      }).catch(() => {})
    }, POLL_INTERVAL_MS)

    return () => clearInterval(timer)
  }, [report?.id, report?.status])

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
        <p>Report not found. <Link to="/">Go back</Link></p>
      </div>
    )
  }

  const isRunning = IN_FLIGHT.has(report.status)
  const { ok, failed } = phaseCount(report.phases)
  const phaseKeys = Object.keys(PHASE_LABELS)
  const currentIsPhase = activeTab !== FINAL_TAB
  const currentPhase = currentIsPhase ? report.phases[activeTab] : null

  return (
    <>
      {isRunning && <AnalysisProgress report={report} />}

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
            <span className={`badge ${isRunning ? 'badge-warning' : failed > 0 ? 'badge-warning' : 'badge-success'}`}>
              {isRunning ? report.status : `${ok}/${ok + failed} phases OK`}
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
              <p style={{ color: 'var(--text-muted)' }}>
                {isRunning ? 'Waiting for this phase to complete…' : 'No data for this phase.'}
              </p>
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
                      <summary>{currentPhase.searchQueries.length} search queries used</summary>
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
    </>
  )
}
