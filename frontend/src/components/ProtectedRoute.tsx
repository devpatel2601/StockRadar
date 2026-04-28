import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { user, loading } = useAuth()

  if (loading) {
    return (
      <div className="loading-overlay">
        <div className="spinner" style={{ width: 36, height: 36 }} />
      </div>
    )
  }

  // Easy to change: swap <Navigate to="/login"> with something else to flip a route public
  if (!user) return <Navigate to="/login" replace />
  return <>{children}</>
}
