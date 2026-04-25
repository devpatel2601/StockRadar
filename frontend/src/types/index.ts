export type RiskTolerance = 'CONSERVATIVE' | 'MODERATE' | 'AGGRESSIVE'
export type InvestmentGoal = 'GROWTH' | 'INCOME' | 'PRESERVATION' | 'SPECULATIVE'
export type AccountType = 'TFSA' | 'RRSP' | 'NON_REGISTERED'
export type ReportStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED'

export interface InvestorProfile {
  country: string
  investmentAmount: number
  riskTolerance: RiskTolerance
  timelineYears: number
  sectorInterests: string[]
  goal: InvestmentGoal
  accounts: AccountType[]
  currentHoldings: string[]
}

export interface PhaseResult {
  phaseName: string
  searchQueries: string[]
  synthesis: string
  completedAt: string
  success: boolean
  errorMessage?: string
}

export interface ResearchReport {
  id: string
  generatedAt: string
  profile: InvestorProfile
  phases: Record<string, PhaseResult>
  finalBriefing: string
  status: ReportStatus
}

export const PHASE_LABELS: Record<string, string> = {
  MACRO_ENVIRONMENT: 'Phase 1 — Macro Environment',
  SECTOR_PULSE: 'Phase 2 — Sector Pulse',
  SMART_MONEY: 'Phase 3 — Smart Money',
  STOCK_SCREENING: 'Phase 4 — Stock Screening',
  PORTFOLIO_CONSTRUCTION: 'Phase 5 — Portfolio Construction',
}

export const SECTORS = [
  'Technology', 'Energy', 'Healthcare', 'Financials',
  'Mining', 'Real Estate', 'Consumer Discretionary',
  'Industrials', 'Utilities', 'Materials',
]
