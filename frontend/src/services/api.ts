import axios from 'axios'
import { auth } from '../firebase'
import type { InvestorProfile, ResearchReport, UserProfile } from '../types'

const client = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
  timeout: 15_000,
})

// Attach Firebase ID token to every request
client.interceptors.request.use(async config => {
  const user = auth.currentUser
  if (user) {
    const token = await user.getIdToken()
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export const api = {
  // ── Auth ──────────────────────────────────────────────────────────────────
  getMe(): Promise<UserProfile> {
    return client.get<UserProfile>('/auth/me').then(r => r.data)
  },

  createProfile(data: {
    email: string; firstName: string; lastName: string; phone: string
    country?: string; province?: string; ageRange?: string; profession?: string
  }): Promise<UserProfile> {
    return client.post<UserProfile>('/auth/profile', data).then(r => r.data)
  },

  updateProfile(data: Partial<{
    firstName: string; lastName: string
    country: string; province: string; ageRange: string; profession: string
  }>): Promise<UserProfile> {
    return client.patch<UserProfile>('/auth/me', data).then(r => r.data)
  },

  // ── Research ──────────────────────────────────────────────────────────────
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
