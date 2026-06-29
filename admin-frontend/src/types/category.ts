export interface ExamCategory {
  id: number
  name: string
  icon?: string
  description?: string
  sortOrder: number
  status: number
  subjects?: Subject[]
}

export interface Subject {
  id: number
  categoryId: number
  name: string
  icon?: string
  totalQuestions: number
  sortOrder: number
  status: number
  chapters?: Chapter[]
}

export interface Chapter {
  id: number
  subjectId: number
  name: string
  questionCount: number
  sortOrder: number
  tags?: Tag[]
}

export interface Tag {
  id: number
  chapterId: number
  name: string
  questionCount: number
}
