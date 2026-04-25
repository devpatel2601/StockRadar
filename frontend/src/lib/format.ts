export function formatDate(iso: string): string {
  return new Date(iso).toLocaleString('en-CA', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('en-CA', {
    style: 'currency',
    currency: 'CAD',
    maximumFractionDigits: 0,
  }).format(amount)
}

export function phaseCount(phases: Record<string, { success: boolean }>): { ok: number; failed: number } {
  const values = Object.values(phases)
  return {
    ok: values.filter(p => p.success).length,
    failed: values.filter(p => !p.success).length,
  }
}
