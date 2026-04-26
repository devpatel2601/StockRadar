import type { ReactNode } from 'react'

type AccountType = 'tfsa' | 'rrsp' | 'fhsa' | 'nonreg' | 'all'

interface MonthItem {
  badge: AccountType
  text: string
}

interface Month {
  name: string
  items: MonthItem[]
}

interface Topic {
  id: string
  title: string
  accounts: AccountType[]
  content: ReactNode
}

function Badge({ type }: { type: AccountType }) {
  const labels: Record<AccountType, string> = {
    tfsa: 'TFSA', rrsp: 'RRSP', fhsa: 'FHSA', nonreg: 'Non-Reg', all: 'All Accounts',
  }
  return <span className={`tax-badge tax-badge--${type}`}>{labels[type]}</span>
}

const MONTHS: Month[] = [
  {
    name: 'January',
    items: [
      { badge: 'tfsa', text: 'Jan 1 — TFSA contribution room resets. $7,000 new room added for 2026. Top up immediately if you have available funds.' },
    ],
  },
  {
    name: 'February',
    items: [
      { badge: 'rrsp', text: 'Feb 28 — RRSP contribution deadline for the 2025 tax year (first 60 days of 2026). Mar 1 in leap years.' },
    ],
  },
  {
    name: 'March',
    items: [
      { badge: 'nonreg', text: 'Mar 31 — T3 slips (funds/trusts) due. Amended T3s can arrive late — consider waiting before filing if you hold fund units.' },
    ],
  },
  {
    name: 'April',
    items: [
      { badge: 'all', text: 'Apr 30 — Personal tax return deadline for most Canadians. Taxes owing are due today even if you are self-employed.' },
    ],
  },
  { name: 'May', items: [] },
  {
    name: 'June',
    items: [
      { badge: 'all', text: 'Jun 15 — Extended filing deadline for self-employed individuals. Any balance owing was still due Apr 30 — interest accrues from May 1.' },
    ],
  },
  { name: 'July', items: [] },
  { name: 'August', items: [] },
  { name: 'September', items: [] },
  {
    name: 'October',
    items: [
      { badge: 'nonreg', text: 'Early Oct — Begin reviewing non-registered accounts for unrealized losses. Identify tax-loss harvesting candidates early.' },
    ],
  },
  {
    name: 'November',
    items: [
      { badge: 'nonreg', text: 'Nov — Finalize harvesting plan. Account for the 30-day superficial loss window: if selling Dec 31, you cannot repurchase until Feb 1.' },
    ],
  },
  {
    name: 'December',
    items: [
      { badge: 'nonreg', text: 'Dec 31 — Last day to realize capital losses for 2026. Trade must settle by Dec 31 (most brokers: T+1 settlement, so trade by Dec 30).' },
      { badge: 'all', text: 'Dec 31 — Last day for charitable donations to count toward a 2026 tax receipt.' },
      { badge: 'tfsa', text: 'Dec 31 — If you over-contributed to your TFSA, withdraw the excess by Dec 31 to stop the 1%/month penalty from carrying into January.' },
    ],
  },
]

const TOPICS: Topic[] = [
  {
    id: 'tfsa',
    title: 'TFSA — Tax-Free Savings Account',
    accounts: ['tfsa'],
    content: (
      <div className="tax-topic-content">
        <div className="tax-info-grid">
          <div className="tax-info-item">
            <span className="tax-info-label">2026 Annual Limit</span>
            <span className="tax-info-value">$7,000</span>
          </div>
          <div className="tax-info-item">
            <span className="tax-info-label">Cumulative Room Since 2009</span>
            <span className="tax-info-value">$102,000</span>
          </div>
          <div className="tax-info-item">
            <span className="tax-info-label">Over-contribution Penalty</span>
            <span className="tax-info-value tax-info-value--warn">1% / month</span>
          </div>
          <div className="tax-info-item">
            <span className="tax-info-label">Eligibility</span>
            <span className="tax-info-value">Canadian residents, age 18+, valid SIN</span>
          </div>
        </div>
        <div className="tax-topic-body-section">
          <h4>How room works</h4>
          <p>Contributions reduce your available room. When you withdraw, the withdrawn amount is added back to your room — but not until January 1 of the following year. Re-contributing in the same year triggers an over-contribution penalty.</p>
        </div>
        <div className="tax-topic-body-section">
          <h4>What to hold inside a TFSA</h4>
          <p>Prioritize high-growth assets. Because growth and withdrawals are completely tax-free, the TFSA rewards compounding most. Growth stocks and equity ETFs are ideal candidates.</p>
          <div className="tax-warning-box">
            <strong>US dividends lose 15% permanently inside a TFSA.</strong> The US–Canada tax treaty withholding does not apply to TFSAs (only RRSPs). US dividend stocks belong in an RRSP, not a TFSA.
          </div>
        </div>
      </div>
    ),
  },
  {
    id: 'rrsp',
    title: 'RRSP — Registered Retirement Savings Plan',
    accounts: ['rrsp'],
    content: (
      <div className="tax-topic-content">
        <div className="tax-info-grid">
          <div className="tax-info-item">
            <span className="tax-info-label">2025 Contribution Limit</span>
            <span className="tax-info-value">18% of 2024 earned income</span>
          </div>
          <div className="tax-info-item">
            <span className="tax-info-label">2025 Dollar Cap</span>
            <span className="tax-info-value">$31,560</span>
          </div>
          <div className="tax-info-item">
            <span className="tax-info-label">Deadline (for 2025 taxes)</span>
            <span className="tax-info-value">Feb 28, 2026</span>
          </div>
          <div className="tax-info-item">
            <span className="tax-info-label">Mandatory Conversion</span>
            <span className="tax-info-value">Dec 31 of your 71st year → RRIF</span>
          </div>
        </div>
        <div className="tax-topic-body-section">
          <h4>The tax deduction math</h4>
          <p>Every dollar contributed to an RRSP reduces your taxable income by one dollar. If your marginal rate is 43%, a $10,000 contribution saves you $4,300 in taxes today. The money grows tax-deferred until withdrawal, when it is taxed as ordinary income — ideally in retirement when your income (and rate) is lower.</p>
        </div>
        <div className="tax-topic-body-section">
          <h4>Spousal RRSP</h4>
          <p>You can contribute to a spousal RRSP using your own contribution room. This shifts future taxable income to the lower-earning spouse at withdrawal. Withdrawals are attributed back to you if taken within 3 calendar years of the last spousal contribution.</p>
        </div>
        <div className="tax-topic-body-section">
          <h4>RRIF conversion at 71</h4>
          <p>You must collapse your RRSP by December 31 of the year you turn 71 and convert it to a RRIF (or purchase an annuity). A RRIF requires minimum annual withdrawals starting at roughly 5.28% at age 72, rising each year. These withdrawals are taxed as income.</p>
        </div>
        <div className="tax-topic-body-section">
          <h4>Best assets to hold in an RRSP</h4>
          <p>US dividend-paying stocks and REITs benefit most from RRSP sheltering — the US–Canada tax treaty eliminates the 15% withholding tax that applies in every other account type. Also good: bonds and interest-bearing assets whose income would otherwise be 100% taxable.</p>
        </div>
      </div>
    ),
  },
  {
    id: 'fhsa',
    title: 'FHSA — First Home Savings Account',
    accounts: ['fhsa'],
    content: (
      <div className="tax-topic-content">
        <div className="tax-info-grid">
          <div className="tax-info-item">
            <span className="tax-info-label">Annual Limit</span>
            <span className="tax-info-value">$8,000 / year</span>
          </div>
          <div className="tax-info-item">
            <span className="tax-info-label">Lifetime Limit</span>
            <span className="tax-info-value">$40,000</span>
          </div>
          <div className="tax-info-item">
            <span className="tax-info-label">Tax Treatment (in)</span>
            <span className="tax-info-value">Deductible like RRSP</span>
          </div>
          <div className="tax-info-item">
            <span className="tax-info-label">Tax Treatment (out)</span>
            <span className="tax-info-value">Tax-free like TFSA</span>
          </div>
        </div>
        <div className="tax-topic-body-section">
          <h4>Eligibility</h4>
          <p>You must be a Canadian resident, at least 18 years old, and a first-time home buyer (you and your spouse/common-law partner cannot have owned a qualifying home in the current year or the preceding four calendar years).</p>
        </div>
        <div className="tax-topic-body-section">
          <h4>The double tax advantage</h4>
          <p>The FHSA is uniquely powerful: contributions are tax-deductible (like an RRSP), and qualifying withdrawals for a first home purchase are tax-free (like a TFSA). No other account type offers both benefits simultaneously.</p>
        </div>
        <div className="tax-topic-body-section">
          <h4>Carry-forward room</h4>
          <p>Unused annual room (up to $8,000) can be carried forward to the next year. Maximum carry-forward is $8,000 per year (you cannot accumulate multiple years and deploy them at once). Open the account early to start accumulating room even if you do not contribute immediately.</p>
        </div>
        <div className="tax-topic-body-section">
          <h4>If you do not buy a home</h4>
          <p>If you close the account without making a qualifying withdrawal, the funds can be transferred tax-free to your RRSP or RRIF — no RRSP room consumed. The account can stay open for 15 years or until December 31 of the year you turn 71.</p>
        </div>
      </div>
    ),
  },
  {
    id: 'capgains',
    title: 'Capital Gains — Inclusion Rates & Exemptions',
    accounts: ['nonreg'],
    content: (
      <div className="tax-topic-content">
        <div className="tax-info-grid">
          <div className="tax-info-item">
            <span className="tax-info-label">Inclusion Rate (≤ $250k/yr, individuals)</span>
            <span className="tax-info-value">50%</span>
          </div>
          <div className="tax-info-item">
            <span className="tax-info-label">Inclusion Rate (&gt; $250k/yr, individuals)</span>
            <span className="tax-info-value tax-info-value--warn">66.67%</span>
          </div>
          <div className="tax-info-item">
            <span className="tax-info-label">Inclusion Rate (corporations/trusts)</span>
            <span className="tax-info-value tax-info-value--warn">66.67% (all gains)</span>
          </div>
          <div className="tax-info-item">
            <span className="tax-info-label">Primary Residence Exemption</span>
            <span className="tax-info-value">100% of gain exempt</span>
          </div>
        </div>
        <div className="tax-topic-body-section">
          <h4>How inclusion works</h4>
          <p>Canada taxes only a portion of capital gains. If you sell a stock for a $10,000 gain and your inclusion rate is 50%, only $5,000 is added to your taxable income and taxed at your marginal rate. The $250,000 threshold per individual resets each calendar year.</p>
          <div className="tax-warning-box">
            <strong>2024 change:</strong> The inclusion rate for gains above $250,000/year for individuals (and all corporate gains) rose from 50% to 66.67% effective June 25, 2024. This change is still subject to ongoing legislative confirmation — consult a tax professional for large realized gains.
          </div>
        </div>
        <div className="tax-topic-body-section">
          <h4>Primary Residence Exemption (PRE)</h4>
          <p>A home that qualifies as your principal residence for every year you own it is fully exempt from capital gains tax. You can only designate one property per family unit per year. Strategic designation of a second property (cottage, rental) for certain years can shelter some gain from each.</p>
        </div>
        <div className="tax-topic-body-section">
          <h4>Losses</h4>
          <p>Capital losses can only offset capital gains, not other income. Net capital losses can be carried back 3 years or carried forward indefinitely to offset future capital gains.</p>
        </div>
      </div>
    ),
  },
  {
    id: 'dividends',
    title: 'Canadian Dividends vs. Foreign Income',
    accounts: ['nonreg', 'rrsp', 'tfsa'],
    content: (
      <div className="tax-topic-content">
        <div className="tax-info-grid">
          <div className="tax-info-item">
            <span className="tax-info-label">Eligible Canadian Dividends</span>
            <span className="tax-info-value">Grossed-up 38%, then a tax credit</span>
          </div>
          <div className="tax-info-item">
            <span className="tax-info-label">Non-Eligible Canadian Dividends</span>
            <span className="tax-info-value">Grossed-up 15%, smaller credit</span>
          </div>
          <div className="tax-info-item">
            <span className="tax-info-label">US Dividends (Non-Reg / TFSA)</span>
            <span className="tax-info-value tax-info-value--warn">15% withholding + taxed as income</span>
          </div>
          <div className="tax-info-item">
            <span className="tax-info-label">US Dividends (RRSP)</span>
            <span className="tax-info-value">No withholding (treaty benefit)</span>
          </div>
        </div>
        <div className="tax-topic-body-section">
          <h4>Eligible dividends (Canadian public corporations)</h4>
          <p>Dividends paid by most Canadian public companies are "eligible dividends." The dividend tax credit (DTC) offsets a significant portion of the tax. At average Canadian marginal rates, eligible dividends from Canadian companies are among the most tax-efficient income types in a non-registered account — more efficient than interest, roughly similar to capital gains.</p>
        </div>
        <div className="tax-topic-body-section">
          <h4>Non-eligible dividends</h4>
          <p>Dividends from Canadian-controlled private corporations (CCPCs) and some other sources are non-eligible. They receive a smaller gross-up and a smaller DTC, making them taxed at a higher effective rate than eligible dividends.</p>
        </div>
        <div className="tax-topic-body-section">
          <h4>US and foreign dividends</h4>
          <p>Dividends from US companies are foreign income — they receive no dividend tax credit in Canada and are taxed as ordinary income at your full marginal rate. Additionally, the US withholds 15% at source in most account types. The RRSP is the only account where the US–Canada tax treaty eliminates the withholding entirely. Hold US dividend payers in your RRSP whenever possible.</p>
        </div>
      </div>
    ),
  },
  {
    id: 'assetalocation',
    title: 'Account Strategy — What to Hold Where',
    accounts: ['tfsa', 'rrsp', 'fhsa', 'nonreg'],
    content: (
      <div className="tax-topic-content">
        <table className="tax-table">
          <thead>
            <tr>
              <th>Asset Type</th>
              <th>Best Account</th>
              <th>Reason</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>US dividend stocks / REITs</td>
              <td><Badge type="rrsp" /></td>
              <td>Treaty eliminates 15% US withholding; defers tax on dividends</td>
            </tr>
            <tr>
              <td>High-growth stocks / equity ETFs</td>
              <td><Badge type="tfsa" /></td>
              <td>All growth and withdrawals are completely tax-free forever</td>
            </tr>
            <tr>
              <td>Canadian dividend stocks</td>
              <td><Badge type="tfsa" /></td>
              <td>Tax-free dividend income; or Non-Reg for dividend tax credit</td>
            </tr>
            <tr>
              <td>Bonds / GICs / interest income</td>
              <td><Badge type="rrsp" /></td>
              <td>Interest is 100% taxable — shelter it. Growth is modest, so RRSP beats TFSA here</td>
            </tr>
            <tr>
              <td>First home savings</td>
              <td><Badge type="fhsa" /></td>
              <td>Deductible contributions + tax-free withdrawal — unmatched for first-time buyers</td>
            </tr>
            <tr>
              <td>Taxable Canadian-dividend ETFs</td>
              <td><Badge type="nonreg" /></td>
              <td>Dividend tax credit makes these relatively efficient in Non-Reg vs. interest income</td>
            </tr>
          </tbody>
        </table>
        <div className="tax-topic-body-section">
          <p>This is a general framework. Actual optimal placement depends on your marginal tax rate, account sizes, time horizon, and withdrawal plan. Maximize registered accounts before using Non-Reg for long-term investing.</p>
        </div>
      </div>
    ),
  },
  {
    id: 'taxloss',
    title: 'Tax-Loss Harvesting & the Superficial Loss Rule',
    accounts: ['nonreg'],
    content: (
      <div className="tax-topic-content">
        <div className="tax-info-grid">
          <div className="tax-info-item">
            <span className="tax-info-label">Deadline to Realize Losses</span>
            <span className="tax-info-value">Dec 31 (trade must settle)</span>
          </div>
          <div className="tax-info-item">
            <span className="tax-info-label">Superficial Loss Window</span>
            <span className="tax-info-value tax-info-value--warn">30 days before + after sale</span>
          </div>
          <div className="tax-info-item">
            <span className="tax-info-label">Loss Carryback</span>
            <span className="tax-info-value">3 years</span>
          </div>
          <div className="tax-info-item">
            <span className="tax-info-label">Loss Carryforward</span>
            <span className="tax-info-value">Indefinite</span>
          </div>
        </div>
        <div className="tax-topic-body-section">
          <h4>How it works</h4>
          <p>If you hold a position at a loss in a non-registered account, you can sell it before December 31 to realize the capital loss. That loss offsets capital gains you have already realized this year, reducing your tax bill. If losses exceed gains, the net capital loss can be carried back 3 years or forward indefinitely.</p>
        </div>
        <div className="tax-topic-body-section">
          <h4>The superficial loss rule (Canada's wash-sale equivalent)</h4>
          <p>If you sell a security at a loss and either you, your spouse, or a corporation controlled by either of you buys the same or identical security within 30 calendar days before or after the sale — the loss is "superficial" and is denied. The denied loss is added to the adjusted cost base of the repurchased security (deferred, not permanent).</p>
          <div className="tax-warning-box">
            <strong>Practical implication:</strong> Sell a loser on Dec 31 → you cannot repurchase that exact security until Feb 1 (the next day after the 30-day window closes). You can immediately buy a similar-but-not-identical ETF to maintain market exposure.
          </div>
        </div>
        <div className="tax-topic-body-section">
          <h4>What counts as "identical"</h4>
          <p>Securities are identical if they confer the same rights and obligations (same ticker, same fund). Switching from one S&P 500 ETF (e.g., XUS) to another (e.g., VFV) is generally considered safe. Switching between different-but-similar Canadian bank stocks is also generally safe. Always confirm with a tax professional for large amounts.</p>
        </div>
        <div className="tax-topic-body-section">
          <h4>Settlement dates matter</h4>
          <p>Canadian equities settle T+1. If December 31 is a trading day, your trade must execute by Dec 30 to guarantee settlement by Dec 31. Check your broker's year-end settlement advisory each fall.</p>
        </div>
      </div>
    ),
  },
]

export default function TaxCalendarPage() {
  return (
    <div className="tax-cal">
      <div className="tax-cal-hero">
        <h1 className="tax-cal-hero-title">Canadian Tax &amp; Investment Year Calendar</h1>
        <p className="tax-cal-hero-sub">
          Key deadlines, contribution limits, and account strategies for Canadian investors.
          Canada-wide · No login required · Updated for 2026.
        </p>
        <div className="tax-cal-hero-badges">
          <Badge type="tfsa" />
          <Badge type="rrsp" />
          <Badge type="fhsa" />
          <Badge type="nonreg" />
        </div>
      </div>

      <section className="tax-cal-section">
        <h2 className="tax-cal-section-title">Monthly Action Calendar</h2>
        <div className="tax-cal-months">
          {MONTHS.map(m => (
            <div key={m.name} className={`tax-month-card${m.items.length > 0 ? ' tax-month-card--active' : ''}`}>
              <div className="tax-month-name">{m.name}</div>
              {m.items.length > 0 ? (
                <ul className="tax-month-items">
                  {m.items.map((item, i) => (
                    <li key={i}>
                      <Badge type={item.badge} />
                      <span>{item.text}</span>
                    </li>
                  ))}
                </ul>
              ) : (
                <p className="tax-month-empty">No major deadlines</p>
              )}
            </div>
          ))}
        </div>
      </section>

      <section className="tax-cal-section">
        <h2 className="tax-cal-section-title">Key Topics</h2>
        <div className="tax-cal-topics">
          {TOPICS.map(topic => (
            <details key={topic.id} className="tax-topic">
              <summary className="tax-topic-summary">
                <span className="tax-topic-title">{topic.title}</span>
                <span className="tax-topic-summary-badges">
                  {topic.accounts.map(a => <Badge key={a} type={a} />)}
                </span>
                <span className="tax-topic-chevron">›</span>
              </summary>
              <div className="tax-topic-body">{topic.content}</div>
            </details>
          ))}
        </div>
      </section>

      <div className="tax-cal-footer">
        <p>
          This page is for general informational purposes only and does not constitute financial, tax, or investment advice.
          Contribution limits and inclusion rates are subject to change by the CRA and Parliament. Always consult a qualified
          tax professional or financial advisor for advice specific to your situation.
        </p>
      </div>
    </div>
  )
}
