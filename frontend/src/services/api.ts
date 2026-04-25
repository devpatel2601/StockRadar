import axios from 'axios'
import type { InvestorProfile, ResearchReport } from '../types'

const client = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
  timeout: 300_000, // 5 minutes — full analysis takes time
})

export const api = {
  runAnalysis(profile: InvestorProfile): Promise<ResearchReport> {
    return client.post<ResearchReport>('/research/analyze', profile).then(r => r.data)
  },

  getReports(): Promise<ResearchReport[]> {
    return client.get<ResearchReport[]>('/research/reports').then(r => r.data)
  },

  getReport(id: string): Promise<ResearchReport> {
    return client.get<ResearchReport>(`/research/reports/${id}`).then(r => r.data)
  },

  deleteReport(id: string): Promise<void> {
    return client.delete(`/research/reports/${id}`).then(() => undefined)
  },
}
