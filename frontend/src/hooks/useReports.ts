import { useCallback, useEffect, useState } from 'react'
import type { InvestorProfile, ResearchReport } from '../types'
import { api } from '../services/api'

export function useReports() {
  const [reports, setReports] = useState<ResearchReport[]>([])
  const [analyzing, setAnalyzing] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    api.getReports().then(setReports).catch(() => {})
  }, [])

  const runAnalysis = useCallback(async (profile: InvestorProfile): Promise<ResearchReport | null> => {
    setAnalyzing(true)
    setError(null)
    try {
      const report = await api.runAnalysis(profile)
      setReports(prev => [report, ...prev])
      return report
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Analysis failed. Is the backend running?')
      return null
    } finally {
      setAnalyzing(false)
    }
  }, [])

  const deleteReport = useCallback(async (id: string) => {
    await api.deleteReport(id)
    setReports(prev => prev.filter(r => r.id !== id))
  }, [])

  return { reports, analyzing, error, runAnalysis, deleteReport }
}
