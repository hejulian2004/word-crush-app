export type AdminTab = 'overview' | 'users' | 'words'

export interface AdminUser {
  id: number
  username: string
  role: 'ADMIN' | 'USER' | string
  status: number
  createdAt: string
  updatedAt: string
}

export interface AdminWord {
  id: number
  english: string
  pronunciation: string
  chinese: string
  contentVersion: number
  status: number
  createdAt: string
  updatedAt: string
}

export interface AdminPage<T> {
  items: T[]
  page: number
  size: number
  total: number
}

export interface AdminOverview {
  totalUsers: number
  activeUsers: number
  totalWords: number
  activeWords: number
}

export interface AdminMe {
  id: number
  username: string
  role: string
}

export interface LoginResponse {
  username: string
  uid: string
  token: string
}

export interface ImportResult {
  added: number
  updated: number
  disabled: number
  total: number
  skipped: number
}
