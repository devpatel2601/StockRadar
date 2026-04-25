import { useEffect, useState } from 'react'

const PHASES = [
  {
    label: 'Phase 1 — Macro Environment',
    detail: 'Checking BoC rates, TSX, CPI, Fed stance, CAD/USD, geopolitical events…',
  },
  {
    label: 'Phase 2 — Sector Pulse',
    detail: 'Scanning sector momentum, ETF benchmarks, catalysts and risks…',
  },
  {
    label: 'Phase 3 — Smart Money',
    detail: 'Tracking 13F filings, Buffett, Ackman, ARK, CPP, Brookfield, Fairfax…',
  },
  {
    label: 'Phase 4 — Stock Screening',
    detail: 'Screening candidates, earnings results, analyst price targets…',
  },
  {
    label: 'Phase 5 — Portfolio Construction',
    detail: 'Synthesizing picks, sizing positions, writing final briefing…',
  },
]

const PHASE_SECONDS = 18

export default function AnalysisProgress() {
  const [elapsed, setElapsed] = useState(0)

  useEffect(() => {
    const start = Date.now()
    const t = setInterval(() => {
      setElapsed(Math.floor((Date.now() - start) / 1000))
    }, 1000)
    return () => clearInterval(t)
  }, [])

  const phaseIndex = Math.min(Math.floor(elapsed / PHASE_SECONDS), PHASES.length - 1)
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
            Elapsed: <strong>{elapsedStr}</strong> &nbsp;·&nbsp; Estimated 60–120s total
          </p>
        </div>

        <div className="ap-phases">
          {PHASES.map((phase, i) => {
            const state = i < phaseIndex ? 'done' : i === phaseIndex ? 'active' : 'pending'
            return (
              <div key={i} className={`ap-phase ap-phase--${state}`}>
                <div className="ap-phase-icon">
                  {state === 'done' && '✓'}
                  {state === 'active' && <div className="spinner ap-phase-spinner" />}
                  {state === 'pending' && <span className="ap-phase-dot" />}
                </div>
                <div className="ap-phase-body">
                  <div className="ap-phase-label">{phase.label}</div>
                  {state === 'active' && (
                    <div className="ap-phase-detail">{phase.detail}</div>
                  )}
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
