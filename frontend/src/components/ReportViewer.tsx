import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import ReactMarkdown from 'react-markdown'
import type { ResearchReport, PhaseResult, StockPrice } from '../types'
import { PHASE_LABELS } from '../types'
import { api } from '../services/api'
import { formatDate, formatCurrency, phaseCount } from '../lib/format'
import AnalysisProgress from './AnalysisProgress'

const FINAL_TAB = 'FINAL_REPORT'
const POLL_INTERVAL_MS = 3_000
const IN_FLIGHT = new Set(['PENDING', 'IN_PROGRESS'])

// ── Score helpers ────────────────────────────────────────────────────────────

function scoreNum(scores: Record<string, string> | undefined, key: string): number | null {
  const v = scores?.[key]
  if (!v) return null
  const n = parseInt(v, 10)
  return isNaN(n) ? null : n
}

function scoreStr(scores: Record<string, string> | undefined, key: string): string | null {
  return scores?.[key] ?? null
}

// ── Sub-components ───────────────────────────────────────────────────────────

function RegimeBadge({ regime }: { regime: string }) {
  const map: Record<string, { label: string; cls: string }> = {
    BULL_MARKET:  { label: 'Bull Market',  cls: 'regime-bull' },
    BEAR_MARKET:  { label: 'Bear Market',  cls: 'regime-bear' },
    TRANSITION:   { label: 'Transition',   cls: 'regime-transition' },
    NEUTRAL:      { label: 'Neutral',      cls: 'regime-neutral' },
  }
  const info = map[regime] ?? { label: regime, cls: 'regime-neutral' }
  return <span className={`regime-badge ${info.cls}`}>{info.label}</span>
}

function SentimentBadge({ sentiment, label }: { sentiment: string; label: string }) {
  const cls = sentiment === 'BULLISH' ? 'sentiment-bull'
            : sentiment === 'BEARISH' ? 'sentiment-bear'
            : 'sentiment-neutral'
  return (
    <div className="score-item">
      <span className="score-label">{label}</span>
      <span className={`sentiment-badge ${cls}`}>{sentiment}</span>
    </div>
  )
}

function ScoreBar({ label, score, colorClass }: { label: string; score: number; colorClass?: string }) {
  const pct = Math.min(100, Math.max(0, score))
  const cls = colorClass ?? (pct >= 70 ? 'bar-high' : pct >= 40 ? 'bar-mid' : 'bar-low')
  return (
    <div className="score-item">
      <div className="score-item-header">
        <span className="score-label">{label}</span>
        <span className="score-value">{pct}/100</span>
      </div>
      <div className="score-track">
        <div className={`score-fill ${cls}`} style={{ width: `${pct}%` }} />
      </div>
    </div>
  )
}

function ScoreDashboard({ phases }: { phases: Record<string, PhaseResult> }) {
  const macro     = phases['MACRO_ENVIRONMENT']
  const smart     = phases['SMART_MONEY']
  const portfolio = phases['PORTFOLIO_CONSTRUCTION']

  const regime          = scoreStr(macro?.scores, 'regime')
  const riskSentiment   = scoreStr(macro?.scores, 'riskSentiment')
  const macroScore      = scoreNum(macro?.scores, 'macroScore')
  const instSentiment   = scoreStr(smart?.scores, 'institutionalSentiment')
  const instConviction  = scoreNum(smart?.scores, 'institutionalConviction')
  const portfolioHealth = scoreNum(portfolio?.scores, 'portfolioHealthScore')

  const hasAny = regime || macroScore !== null || instSentiment || portfolioHealth !== null
  if (!hasAny) return null

  return (
    <div className="score-dashboard">
      <div className="score-dashboard-title">Market Intelligence Scores</div>
      <div className="score-dashboard-grid">
        {regime && (
          <div className="score-item">
            <span className="score-label">Market Regime</span>
            <RegimeBadge regime={regime} />
          </div>
        )}
        {riskSentiment && (
          <div className="score-item">
            <span className="score-label">Risk Sentiment</span>
            <span className={`sentiment-badge ${riskSentiment === 'RISK_ON' ? 'sentiment-bull' : riskSentiment === 'RISK_OFF' ? 'sentiment-bear' : 'sentiment-neutral'}`}>
              {riskSentiment.replace('_', '-')}
            </span>
          </div>
        )}
        {macroScore !== null && <ScoreBar label="Macro Score" score={macroScore} />}
        {instSentiment && <SentimentBadge sentiment={instSentiment} label="Smart Money" />}
        {instConviction !== null && <ScoreBar label="Institutional Conviction" score={instConviction} />}
        {portfolioHealth !== null && <ScoreBar label="Portfolio Health" score={portfolioHealth} colorClass={portfolioHealth >= 70 ? 'bar-high' : portfolioHealth >= 50 ? 'bar-mid' : 'bar-low'} />}
      </div>
    </div>
  )
}

function SectorScores({ scores }: { scores?: Record<string, string> }) {
  if (!scores) return null
  const entries = Object.entries(scores).filter(([k]) => k.startsWith('sector_'))
  if (entries.length === 0) return null
  return (
    <div className="phase-scores">
      <div className="phase-scores-title">Sector Momentum Scores</div>
      {entries.map(([key, val]) => {
        const n = parseInt(val, 10)
        return isNaN(n) ? null : (
          <ScoreBar key={key} label={key.replace('sector_', '')} score={n} />
        )
      })}
    </div>
  )
}

function StockScores({ scores, livePrices, loadingPrices }: {
  scores?: Record<string, string>
  livePrices: Record<string, StockPrice>
  loadingPrices: boolean
}) {
  if (!scores) return null
  const stocks  = Object.entries(scores).filter(([k]) => k.startsWith('stock_'))
  const canslim = Object.fromEntries(
    Object.entries(scores).filter(([k]) => k.startsWith('canslim_'))
  )
  if (stocks.length === 0) return null
  return (
    <div className="phase-scores">
      <div className="phase-scores-title">
        Stock Conviction Scores
        {loadingPrices && <span className="live-price-loading"> · fetching live prices…</span>}
      </div>
      <div className="stock-scores-grid">
        {stocks.map(([key, val]) => {
          const ticker = key.replace('stock_', '')
          const conviction = parseInt(val, 10)
          const canslimVal = canslim[`canslim_${ticker}`]
          const live = livePrices[ticker.toUpperCase()]
          return (
            <div key={ticker} className="stock-score-card">
              <div className="stock-score-ticker">{ticker}</div>
              {live?.found ? (
                <div className="live-price-block">
                  <span className="live-price-value">
                    {live.currency} {live.price.toFixed(2)}
                  </span>
                  <span className={`live-price-change ${live.change >= 0 ? 'price-up' : 'price-down'}`}>
                    {live.change >= 0 ? '+' : ''}{live.change.toFixed(2)} ({live.changePercent.toFixed(2)}%)
                  </span>
                  <span className="live-price-label">Yahoo Finance · live</span>
                </div>
              ) : !loadingPrices && (
                <div className="live-price-unavailable">Price unavailable</div>
              )}
              <div className="stock-score-body">
                {!isNaN(conviction) && (
                  <div className="stock-conviction-row">
                    <span>Conviction</span>
                    <span className={`conviction-pill ${conviction >= 75 ? 'pill-high' : conviction >= 50 ? 'pill-mid' : 'pill-low'}`}>
                      {conviction}/100
                    </span>
                  </div>
                )}
                {canslimVal && (
                  <div className="stock-conviction-row">
                    <span>CANSLIM</span>
                    <span className={`conviction-pill ${parseInt(canslimVal) >= 5 ? 'pill-high' : parseInt(canslimVal) >= 3 ? 'pill-mid' : 'pill-low'}`}>
                      {canslimVal}/7
                    </span>
                  </div>
                )}
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}

// ── Main component ───────────────────────────────────────────────────────────

export default function ReportViewer() {
  const { id } = useParams<{ id: string }>()
  const [report, setReport] = useState<ResearchReport | null>(null)
  const [activeTab, setActiveTab] = useState<string>(FINAL_TAB)
  const [loading, setLoading] = useState(true)
  const [livePrices, setLivePrices] = useState<Record<string, StockPrice>>({})
  const [loadingPrices, setLoadingPrices] = useState(false)

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

  // Fetch live prices once the report completes and stock scores are available
  useEffect(() => {
    if (!report || report.status !== 'COMPLETED') return
    const stockScores = report.phases['STOCK_SCREENING']?.scores
    if (!stockScores) return

    const tickers = Object.keys(stockScores)
      .filter(k => k.startsWith('stock_'))
      .map(k => k.replace('stock_', ''))

    if (tickers.length === 0) return

    setLoadingPrices(true)
    api.getStockPrices(tickers)
      .then(prices => {
        const map: Record<string, StockPrice> = {}
        prices.forEach(p => { map[p.ticker.toUpperCase()] = p })
        setLivePrices(map)
      })
      .catch(() => {})
      .finally(() => setLoadingPrices(false))
  }, [report?.status, report?.id])

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
            {report.profile.screeningStrategy && report.profile.screeningStrategy !== 'BALANCED' && (
              <span className="rv-profile-chip rv-chip-strategy">{report.profile.screeningStrategy}</span>
            )}
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

        {/* ── Score Dashboard (visible once analysis has scores) ── */}
        {!isRunning && Object.keys(report.phases).length > 0 && (
          <ScoreDashboard phases={report.phases} />
        )}

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
                  {/* Phase-specific score visualizations */}
                  {activeTab === 'SECTOR_PULSE' && (
                    <SectorScores scores={currentPhase.scores} />
                  )}
                  {activeTab === 'STOCK_SCREENING' && (
                    <StockScores scores={currentPhase.scores} livePrices={livePrices} loadingPrices={loadingPrices} />
                  )}

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
