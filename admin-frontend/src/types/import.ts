export interface ImportTask {
  id: number
  fileName: string
  fileSize: number
  categoryId: number
  totalRows: number
  successCount: number
  failCount: number
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED'
  errorFileUrl?: string
  createdAt: string
  completedAt?: string
}

export interface ImportProgress {
  taskId: number
  totalRows: number
  currentRow: number
  successCount: number
  failCount: number
  percent: number
  status: string
  errors?: string[]
}
