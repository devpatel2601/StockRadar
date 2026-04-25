import { useEffect, useState } from 'react'
import type { ResearchReport } from '../types'

const PHASE_ORDER = [
  { key: 'MACRO_ENVIRONMENT', label: 'Phase 1 — Macro Environment', detail: 'Checking BoC rates, TSX, CPI, Fed stance, CAD/USD…' },
  { key: 'SECTOR_PULSE',      label: 'Phase 2 — Sector Pulse',      detail: 'Scanning sector momentum, ETF benchmarks, catalysts…' },
  { key: 'SMART_MONEY',       label: 'Phase 3 — Smart Money',       detail: 'Tracking 13F filings, Buffett, Ackman, ARK, CPP…' },
  { key: 'STOCK_SCREENING',   label: 'Phase 4 — Stock Screening',   detail: 'Screening candidates, earnings, analyst targets…' },
  { key: 'PORTFOLIO_CONSTRUCTION', label: 'Phase 5 — Portfolio Construction', detail: 'Synthesizing picks, sizing positions, writing briefing…' },
]

interface Props {
  report: ResearchReport
}

export default function AnalysisProgress({ report }: Props) {
  const [elapsed, setElapsed] = useState(0)

  useEffect(() => {
    const start = new Date(report.generatedAt).getTime()
    const t = setInterval(() => {
      setElapsed(Math.floor((Date.now() - start) / 1000))
    }, 1000)
    return () => clearInterval(t)
  }, [report.generatedAt])

  const completedKeys = new Set(Object.keys(report.phases))
  const activeIndex = PHASE_ORDER.findIndex(p => !completedKeys.has(p.key))

  const minutes = Math.floor(elapsed / 60)
  const seconds = elapsed % 60
  const elapsedStr = minutes > 0 ? `${minutes}m ${seconds}s` : `${seconds}s`

  return (
    <div className="ap-overlay">
      <div className="ap-card">
        <div className="ap-header">
          <div className="spinner ap-spinner" />
          <h2 className="ap-title">Running Investment Analysis</h2>
          <p className="ap-subtitle">
            {completedKeys.size} of {PHASE_ORDER.length} phases complete
            &nbsp;·&nbsp; {elapsedStr} elapsed
          </p>
        </div>

        <div className="ap-phases">
          {PHASE_ORDER.map((phase, i) => {
            const isDone = completedKeys.has(phase.key)
            const isActive = i === activeIndex
            const state = isDone ? 'done' : isActive ? 'active' : 'pending'
            return (
              <div key={phase.key} className={`ap-phase ap-phase--${state}`}>
                <div className="ap-phase-icon">
                  {isDone && '✓'}
                  {isActive && <div className="spinner ap-phase-spinner" />}
                  {!isDone && !isActive && <span className="ap-phase-dot" />}
                </div>
                <div className="ap-phase-body">
                  <div className="ap-phase-label">{phase.label}</div>
                  {isActive && <div className="ap-phase-detail">{phase.detail}</div>}
                </div>
              </div>
            )
          })}
        </div>

        <p className="ap-note">
          AI is searching the web and synthesizing results in real time. Do not close this tab.
        </p>
      </div>
    </div>
  )
}
