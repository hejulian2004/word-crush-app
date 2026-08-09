import type {
  AdminMe,
  AdminOverview,
  AdminPage,
  AdminUser,
  AdminWord,
  ImportResult,
  LoginResponse,
} from './types'

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? '/api'
const TOKEN_KEY = 'word-crush-admin-token'

export class ApiError extends Error {
  status: number

  constructor(message: string, status = 500) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function saveToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

async function request<T>(path: string, init: RequestInit = {}) {
  const headers = new Headers(init.headers)
  if (init.body && !(init.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json')
  }
  const token = getToken()
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  const response = await fetch(`${API_BASE}${path}`, { ...init, headers })
  const payload = (await response.json().catch(() => null)) as {
    code?: number
    msg?: string
    data?: T
  } | null
  if (!response.ok || !payload || payload.code !== 200) {
    throw new ApiError(payload?.msg || `请求失败（${response.status}）`, response.status)
  }
  return payload.data as T
}

export function login(username: string, password: string) {
  return request<LoginResponse>('/user/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  })
}

export function getAdminMe() {
  return request<AdminMe>('/admin/me')
}

export function getOverview() {
  return request<AdminOverview>('/admin/overview')
}

export function getUsers(params: { query?: string; status?: string; page: number; size: number }) {
  const search = new URLSearchParams({ page: String(params.page), size: String(params.size) })
  if (params.query) search.set('query', params.query)
  if (params.status) search.set('status', params.status)
  return request<AdminPage<AdminUser>>(`/admin/users?${search.toString()}`)
}

export function updateUserStatus(id: number, status: number) {
  return request<AdminUser>(`/admin/users/${id}/status`, {
    method: 'PUT',
    body: JSON.stringify({ status }),
  })
}

export function resetUserPassword(id: number, password: string) {
  return request<void>(`/admin/users/${id}/password`, {
    method: 'PUT',
    body: JSON.stringify({ password }),
  })
}

export function getWords(params: { query?: string; status?: string; page: number; size: number }) {
  const search = new URLSearchParams({ page: String(params.page), size: String(params.size) })
  if (params.query) search.set('query', params.query)
  if (params.status) search.set('status', params.status)
  return request<AdminPage<AdminWord>>(`/admin/words?${search.toString()}`)
}

export function createWord(payload: Pick<AdminWord, 'id' | 'english' | 'pronunciation' | 'chinese'>) {
  return request<AdminWord>('/admin/words', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateWord(id: number, payload: Pick<AdminWord, 'english' | 'pronunciation' | 'chinese' | 'status'>) {
  return request<AdminWord>(`/admin/words/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function importWords(file: File, replace: boolean) {
  const body = new FormData()
  body.append('file', file)
  return request<ImportResult>(`/admin/words/import?replace=${replace}`, {
    method: 'POST',
    body,
  })
}

export async function downloadWords() {
  const token = getToken()
  const response = await fetch(`${API_BASE}/admin/words/export`, {
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  })
  if (!response.ok) {
    throw new ApiError('下载词表失败', response.status)
  }
  const blob = await response.blob()
  const url = URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = 'wordbook.csv'
  document.body.appendChild(anchor)
  anchor.click()
  anchor.remove()
  URL.revokeObjectURL(url)
}
