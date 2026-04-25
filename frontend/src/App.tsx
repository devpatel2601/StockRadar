import { Routes, Route, Link } from 'react-router-dom'
import Dashboard from './components/Dashboard'
import ReportViewer from './components/ReportViewer'
import './index.css'

export default function App() {
  return (
    <div className="app">
      <header className="app-header">
        <Link to="/" className="app-title">
          Investment Research Analyst
        </Link>
        <span className="app-subtitle">Powered by Claude AI</span>
      </header>
      <main className="app-main">
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/report/:id" element={<ReportViewer />} />
        </Routes>
      </main>
    </div>
  )
}
