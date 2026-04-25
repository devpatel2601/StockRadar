import { useState } from 'react'
import type { InvestorProfile, RiskTolerance, InvestmentGoal, AccountType } from '../types'
import { SECTORS } from '../types'

interface Props {
  onSubmit: (profile: InvestorProfile) => void
  loading: boolean
}

const ACCOUNTS: AccountType[] = ['TFSA', 'RRSP', 'NON_REGISTERED']

export default function ProfileForm({ onSubmit, loading }: Props) {
  const [form, setForm] = useState<InvestorProfile>({
    country: 'Canada',
    investmentAmount: 10000,
    riskTolerance: 'MODERATE',
    timelineYears: 5,
    sectorInterests: [],
    goal: 'GROWTH',
    accounts: ['TFSA'],
    currentHoldings: [],
  })
  const [holdingsText, setHoldingsText] = useState('')

  function toggle<T>(arr: T[], item: T): T[] {
    return arr.includes(item) ? arr.filter(x => x !== item) : [...arr, item]
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    onSubmit({
      ...form,
      currentHoldings: holdingsText
        .split(',')
        .map(s => s.trim())
        .filter(Boolean),
    })
  }

  return (
    <form onSubmit={handleSubmit}>
      <div className="form-grid">
        <div className="form-group">
          <label>Country</label>
          <input
            value={form.country}
            onChange={e => setForm({ ...form, country: e.target.value })}
            placeholder="Canada"
          />
        </div>

        <div className="form-group">
          <label>Investment Amount (CAD $)</label>
          <input
            type="number"
            value={form.investmentAmount}
            min={0}
            onChange={e => setForm({ ...form, investmentAmount: Number(e.target.value) })}
          />
        </div>

        <div className="form-group">
          <label>Risk Tolerance</label>
          <select
            value={form.riskTolerance}
            onChange={e => setForm({ ...form, riskTolerance: e.target.value as RiskTolerance })}
          >
            <option value="CONSERVATIVE">Conservative</option>
            <option value="MODERATE">Moderate</option>
            <option value="AGGRESSIVE">Aggressive</option>
          </select>
        </div>

        <div className="form-group">
          <label>Timeline (years)</label>
          <input
            type="number"
            value={form.timelineYears}
            min={1}
            max={40}
            onChange={e => setForm({ ...form, timelineYears: Number(e.target.value) })}
          />
        </div>

        <div className="form-group">
          <label>Investment Goal</label>
          <select
            value={form.goal}
            onChange={e => setForm({ ...form, goal: e.target.value as InvestmentGoal })}
          >
            <option value="GROWTH">Growth</option>
            <option value="INCOME">Income</option>
            <option value="PRESERVATION">Preservation</option>
            <option value="SPECULATIVE">Speculative</option>
          </select>
        </div>

        <div className="form-group full">
          <label>Sectors of Interest</label>
          <div className="checkbox-group">
            {SECTORS.map(s => (
              <div
                key={s}
                className={`checkbox-item ${form.sectorInterests.includes(s) ? 'selected' : ''}`}
                onClick={() => setForm({ ...form, sectorInterests: toggle(form.sectorInterests, s) })}
              >
                {s}
              </div>
            ))}
          </div>
        </div>

        <div className="form-group full">
          <label>Accounts</label>
          <div className="checkbox-group">
            {ACCOUNTS.map(a => (
              <div
                key={a}
                className={`checkbox-item ${form.accounts.includes(a) ? 'selected' : ''}`}
                onClick={() => setForm({ ...form, accounts: toggle(form.accounts, a) })}
              >
                {a.replace('_', ' ')}
              </div>
            ))}
          </div>
        </div>

        <div className="form-group full">
          <label>Current Holdings (optional — comma-separated tickers)</label>
          <input
            value={holdingsText}
            onChange={e => setHoldingsText(e.target.value)}
            placeholder="e.g. SHOP.TO, CNQ.TO, BN.TO"
          />
        </div>
      </div>

      <div style={{ marginTop: 20 }}>
        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? (
            <>
              <div className="spinner" />
              Analyzing... (this takes 1-3 minutes)
            </>
          ) : (
            'Run Investment Analysis'
          )}
        </button>
      </div>
    </form>
  )
}
