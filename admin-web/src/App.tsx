import { useEffect, useState, type FormEvent, type ReactNode } from 'react'
import {
  ApiError,
  clearToken,
  createWord,
  downloadWords,
  getAdminMe,
  getOverview,
  getToken,
  getUsers,
  getWords,
  importWords,
  login,
  resetUserPassword,
  saveToken,
  updateUserStatus,
  updateWord,
} from './api'
import type {
  AdminMe,
  AdminOverview,
  AdminTab,
  AdminUser,
  AdminWord,
  ImportResult,
} from './types'

type ToastTone = 'success' | 'error'
type IconName =
  | 'grid'
  | 'users'
  | 'book'
  | 'upload'
  | 'logout'
  | 'sun'
  | 'search'
  | 'refresh'
  | 'plus'
  | 'edit'
  | 'pause'
  | 'play'
  | 'download'
  | 'arrow-left'
  | 'arrow-right'
  | 'key'
  | 'shield'
  | 'check'
  | 'alert'
  | 'x'
  | 'spark'
  | 'chevron-down'

interface ToastState {
  message: string
  tone: ToastTone
}

function messageOf(error: unknown) {
  return error instanceof Error ? error.message : '操作失败，请稍后重试'
}

function formatNumber(value: number) {
  return new Intl.NumberFormat('zh-CN').format(value)
}

function formatDate(value?: string) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '—'
  return new Intl.DateTimeFormat('zh-CN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}

function initials(value: string) {
  return value.slice(0, 2).toUpperCase()
}

function Icon({ name, size = 18 }: { name: IconName; size?: number }) {
  const common = {
    fill: 'none',
    stroke: 'currentColor',
    strokeWidth: 1.8,
    strokeLinecap: 'round' as const,
    strokeLinejoin: 'round' as const,
  }
  let content: ReactNode
  switch (name) {
    case 'grid':
      content = <><rect {...common} x="3" y="3" width="7" height="7" rx="1" /><rect {...common} x="14" y="3" width="7" height="7" rx="1" /><rect {...common} x="3" y="14" width="7" height="7" rx="1" /><rect {...common} x="14" y="14" width="7" height="7" rx="1" /></>
      break
    case 'users':
      content = <><path {...common} d="M16 20v-1.5a4 4 0 0 0-4-4H7a4 4 0 0 0-4 4V20" /><circle {...common} cx="9.5" cy="7" r="3.5" /><path {...common} d="M17 3.2a3.5 3.5 0 0 1 0 6.8M21 20v-1.2a4 4 0 0 0-3-3.8" /></>
      break
    case 'book':
      content = <><path {...common} d="M4 5.5A2.5 2.5 0 0 1 6.5 3H20v16H6.5A2.5 2.5 0 0 0 4 21.5z" /><path {...common} d="M4 5.5v16M8 7h8M8 11h8" /></>
      break
    case 'upload':
      content = <><path {...common} d="M12 16V4M7.5 8.5 12 4l4.5 4.5M4 14v5h16v-5" /></>
      break
    case 'logout':
      content = <><path {...common} d="M10 5H5v14h5M14 8l4 4-4 4M18 12H9" /></>
      break
    case 'sun':
      content = <><circle {...common} cx="12" cy="12" r="4" /><path {...common} d="M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M4.93 19.07l1.41-1.41M17.66 6.34l1.41-1.41" /></>
      break
    case 'search':
      content = <><circle {...common} cx="10.8" cy="10.8" r="6.8" /><path {...common} d="m16 16 5 5" /></>
      break
    case 'refresh':
      content = <><path {...common} d="M20 11a8 8 0 0 0-14.9-4L3 10M3 5v5h5M4 13a8 8 0 0 0 14.9 4L21 14M21 19v-5h-5" /></>
      break
    case 'plus':
      content = <><path {...common} d="M12 5v14M5 12h14" /></>
      break
    case 'edit':
      content = <><path {...common} d="M4 20h4l10.8-10.8a2.1 2.1 0 0 0-3-3L5 17v3zM14.5 7.5l3 3" /></>
      break
    case 'pause':
      content = <><rect {...common} x="4" y="4" width="16" height="16" rx="4" /><path {...common} d="M10 9v6M14 9v6" /></>
      break
    case 'play':
      content = <><rect {...common} x="4" y="4" width="16" height="16" rx="4" /><path {...common} d="m10 8 5 4-5 4z" /></>
      break
    case 'download':
      content = <><path {...common} d="M12 4v11M7.5 11.5 12 16l4.5-4.5M4 19h16" /></>
      break
    case 'arrow-left':
      content = <><path {...common} d="M19 12H5M11 6l-6 6 6 6" /></>
      break
    case 'arrow-right':
      content = <><path {...common} d="M5 12h14M13 6l6 6-6 6" /></>
      break
    case 'key':
      content = <><circle {...common} cx="8" cy="15" r="4" /><path {...common} d="m11 12 7-7M17 5l2 2M14.5 7.5l2 2" /></>
      break
    case 'shield':
      content = <><path {...common} d="M12 3 20 6v5c0 5-3.3 8.3-8 10-4.7-1.7-8-5-8-10V6z" /><path {...common} d="m8.5 12 2.2 2.2 4.8-5" /></>
      break
    case 'check':
      content = <path {...common} d="m5 12 4.3 4.3L19 7" />
      break
    case 'alert':
      content = <><path {...common} d="M10.3 4.3 2.8 17a2 2 0 0 0 1.7 3h15a2 2 0 0 0 1.7-3L13.7 4.3a2 2 0 0 0-3.4 0z" /><path {...common} d="M12 9v4M12 16h.01" /></>
      break
    case 'x':
      content = <><path {...common} d="m6 6 12 12M18 6 6 18" /></>
      break
    case 'spark':
      content = <><path {...common} d="m12 3 1.7 5.3L19 10l-5.3 1.7L12 17l-1.7-5.3L5 10l5.3-1.7zM19 16l.7 2.3L22 19l-2.3.7L19 22l-.7-2.3L16 19l2.3-.7z" /></>
      break
    case 'chevron-down':
      content = <path {...common} d="m6 9 6 6 6-6" />
      break
  }
  return <svg aria-hidden="true" width={size} height={size} viewBox="0 0 24 24">{content}</svg>
}

function StatusBadge({ active, label }: { active: boolean; label?: string }) {
  return <span className={`status-badge ${active ? 'is-active' : 'is-paused'}`}>
    <span className="status-dot" />
    {label ?? (active ? '启用中' : '已停用')}
  </span>
}

function LoginScreen({
  onSubmit,
  submitting,
  error,
}: {
  onSubmit: (username: string, password: string) => Promise<void>
  submitting: boolean
  error: string
}) {
  const [username, setUsername] = useState('admin')
  const [password, setPassword] = useState('')

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    await onSubmit(username, password)
  }

  return <main className="login-page">
    <section className="login-story">
      <div className="story-noise" />
      <div className="story-topline"><span className="brand-stamp">WC</span><span>WORD CRUSH / OPERATIONS</span></div>
      <div className="story-copy">
        <p className="eyebrow light">WORDS THAT STICK</p>
        <h1>把每一个<br /><em>词</em>，打磨得更好。</h1>
        <p>词库、用户与学习体验的幕后工作台。今天也让词语保持锋利。</p>
      </div>
      <div className="story-footer"><span>EST. 2024</span><span className="story-line" /><span>SHANGHAI / CN</span></div>
    </section>
    <section className="login-panel">
      <div className="login-panel-inner">
        <div className="login-mark"><span>W</span><i /></div>
        <p className="eyebrow">ADMIN ACCESS</p>
        <h2>欢迎回到词库室</h2>
        <p className="login-intro">使用管理员账号进入 Word Crush 运营后台。</p>
        <form className="login-form" onSubmit={submit}>
          <label>管理员账号<input value={username} onChange={(event) => setUsername(event.target.value)} autoComplete="username" placeholder="输入用户名" required /></label>
          <label>登录密码<input type="password" value={password} onChange={(event) => setPassword(event.target.value)} autoComplete="current-password" placeholder="输入密码" required /></label>
          {error && <div className="form-error"><Icon name="alert" size={16} />{error}</div>}
          <button className="button button-primary button-wide" type="submit" disabled={submitting}>
            {submitting ? <span className="button-spinner" /> : <Icon name="arrow-right" size={17} />}
            {submitting ? '正在验证…' : '进入管理端'}
          </button>
        </form>
        <div className="login-note"><Icon name="shield" size={15} />仅限拥有 ADMIN 角色的账号访问</div>
      </div>
    </section>
  </main>
}

function AppShell({
  activeTab,
  onTabChange,
  admin,
  onLogout,
  children,
}: {
  activeTab: AdminTab
  onTabChange: (tab: AdminTab) => void
  admin: AdminMe
  onLogout: () => void
  children: ReactNode
}) {
  const nav: { id: AdminTab; label: string; icon: IconName; hint: string }[] = [
    { id: 'overview', label: '总览', icon: 'grid', hint: 'PULSE' },
    { id: 'users', label: '用户管理', icon: 'users', hint: 'PEOPLE' },
    { id: 'words', label: '单词管理', icon: 'book', hint: 'CATALOG' },
  ]
  const pageTitle = nav.find((item) => item.id === activeTab)

  return <div className="app-shell">
    <aside className="sidebar">
      <div className="sidebar-brand">
        <div className="brand-symbol"><span>W</span><i /></div>
        <div><strong>word<span>crush</span></strong><small>ADMIN STUDIO</small></div>
      </div>
      <div className="sidebar-rule" />
      <p className="sidebar-label">WORKSPACE</p>
      <nav className="main-nav">
        {nav.map((item) => <button key={item.id} className={`nav-item ${activeTab === item.id ? 'is-active' : ''}`} onClick={() => onTabChange(item.id)}>
          <span className="nav-icon"><Icon name={item.icon} size={18} /></span>
          <span className="nav-text"><strong>{item.label}</strong><small>{item.hint}</small></span>
          {activeTab === item.id && <span className="nav-active-mark" />}
        </button>)}
      </nav>
      <div className="sidebar-bottom">
        <div className="sidebar-tip"><Icon name="spark" size={17} /><div><strong>词库小提醒</strong><p>每次导入都会保留内容版本，方便客户端同步。</p></div></div>
        <div className="operator-card"><div className="avatar avatar-small">{initials(admin.username)}</div><div className="operator-info"><strong>{admin.username}</strong><span>SUPER ADMIN</span></div><button className="icon-button icon-button-dark" onClick={onLogout} aria-label="退出登录" title="退出登录"><Icon name="logout" size={16} /></button></div>
      </div>
    </aside>
    <main className="content-area">
      <header className="topbar">
        <div><p className="topbar-crumb">WORD CRUSH <span>/</span> {pageTitle?.hint}</p><h1>{pageTitle?.label}</h1></div>
        <div className="topbar-actions"><span className="live-status"><i />系统运行中</span><div className="topbar-date"><Icon name="sun" size={16} />{new Intl.DateTimeFormat('zh-CN', { month: 'long', day: 'numeric', weekday: 'short' }).format(new Date())}</div><div className="avatar avatar-top">{initials(admin.username)}</div></div>
      </header>
      <div className="page-content">{children}</div>
    </main>
  </div>
}

function OverviewView({ onNavigate, notify }: { onNavigate: (tab: AdminTab) => void; notify: (message: string, tone?: ToastTone) => void }) {
  const [data, setData] = useState<AdminOverview | null>(null)
  const [loading, setLoading] = useState(true)

  const load = async () => {
    setLoading(true)
    try {
      setData(await getOverview())
    } catch (error) {
      notify(messageOf(error), 'error')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { void load() }, [])

  const activeWordRatio = data && data.totalWords > 0 ? Math.round((data.activeWords / data.totalWords) * 100) : 0
  const activeUserRatio = data && data.totalUsers > 0 ? Math.round((data.activeUsers / data.totalUsers) * 100) : 0

  return <div className="view view-overview">
    <div className="view-intro intro-overview"><div><p className="eyebrow">GOOD MORNING, OPERATOR</p><h2>今天的词库，<em>有点意思。</em></h2><p>从这里查看 Word Crush 的内容脉搏，保持每一次练习都新鲜。</p></div><button className="button button-ghost" onClick={() => void load()}><Icon name="refresh" size={16} />刷新数据</button></div>
    <section className="pulse-grid">
      <div className="pulse-card pulse-card-main"><div className="pulse-card-header"><div><p className="eyebrow light">CATALOG PULSE</p><h3>词库脉搏</h3></div><span className="pulse-orbit"><i /><i /><i /></span></div><div className="pulse-number">{loading ? '—' : formatNumber(data?.activeWords ?? 0)}<span> 个启用词条</span></div><div className="pulse-meter"><span style={{ width: `${activeWordRatio}%` }} /></div><div className="pulse-meta"><span>总词条 {formatNumber(data?.totalWords ?? 0)}</span><strong>{activeWordRatio}% <small>正在服务玩家</small></strong></div><div className="pulse-sparkline"><span style={{ height: '34%' }} /><span style={{ height: '48%' }} /><span style={{ height: '42%' }} /><span style={{ height: '64%' }} /><span style={{ height: '52%' }} /><span style={{ height: '79%' }} /><span style={{ height: '71%' }} /><span style={{ height: '88%' }} /><span style={{ height: '82%' }} /><span style={{ height: '100%' }} /></div></div>
      <div className="pulse-card pulse-card-light"><div className="metric-icon metric-icon-coral"><Icon name="users" size={20} /></div><p className="eyebrow">USERS</p><h3>{loading ? '—' : formatNumber(data?.activeUsers ?? 0)}</h3><p className="metric-caption">活跃用户</p><div className="metric-foot"><span>共 {formatNumber(data?.totalUsers ?? 0)} 个账号</span><strong>{activeUserRatio}%</strong></div></div>
      <div className="pulse-card pulse-card-light pulse-card-cream"><div className="metric-icon metric-icon-green"><Icon name="book" size={20} /></div><p className="eyebrow">WORDS IN PLAY</p><h3>{loading ? '—' : formatNumber(data?.totalWords ?? 0)}</h3><p className="metric-caption">词库总量</p><div className="metric-foot"><span>可编辑 · 可版本化</span><strong>v{data ? '1' : '—'}</strong></div></div>
    </section>
    <section className="overview-lower">
      <div className="section-card action-card"><div className="section-card-heading"><div><p className="eyebrow">SHORTCUTS</p><h3>现在去做什么？</h3></div><Icon name="spark" size={20} /></div><div className="shortcut-list"><button onClick={() => onNavigate('words')}><span className="shortcut-index">01</span><span><strong>整理词库</strong><small>编辑释义、发音或上线状态</small></span><Icon name="arrow-right" size={17} /></button><button onClick={() => onNavigate('words')}><span className="shortcut-index">02</span><span><strong>导入新词表</strong><small>上传 CSV，批量更新内容版本</small></span><Icon name="upload" size={17} /></button><button onClick={() => onNavigate('users')}><span className="shortcut-index">03</span><span><strong>看看用户</strong><small>管理账号状态与登录凭证</small></span><Icon name="arrow-right" size={17} /></button></div></div>
      <div className="section-card guide-card"><div className="guide-illustration"><span className="guide-letter">Aa</span><span className="guide-dot dot-one" /><span className="guide-dot dot-two" /><span className="guide-dot dot-three" /></div><div className="guide-copy"><p className="eyebrow">EDITOR'S NOTE</p><h3>让词义保持<br /><em>清醒。</em></h3><p>导入 CSV 时请保持四列结构：<code>id, english, pronunciation, chinese</code>。已存在的 ID 会更新，不会重复。</p><button className="text-button" onClick={() => onNavigate('words')}>打开单词管理 <Icon name="arrow-right" size={15} /></button></div></div>
    </section>
  </div>
}

function SearchControl({ value, onChange, onSubmit, placeholder }: { value: string; onChange: (value: string) => void; onSubmit: () => void; placeholder: string }) {
  return <form className="search-control" onSubmit={(event) => { event.preventDefault(); onSubmit() }}><Icon name="search" size={17} /><input value={value} onChange={(event) => onChange(event.target.value)} placeholder={placeholder} /><button type="submit" aria-label="搜索"><Icon name="arrow-right" size={16} /></button></form>
}

function TableToolbar({ children }: { children: ReactNode }) {
  return <div className="table-toolbar">{children}</div>
}

function Pagination({ page, size, total, onChange }: { page: number; size: number; total: number; onChange: (page: number) => void }) {
  const totalPages = Math.max(1, Math.ceil(total / size))
  return <div className="pagination"><span>显示 <strong>{total === 0 ? 0 : page * size + 1}–{Math.min((page + 1) * size, total)}</strong> / {formatNumber(total)}</span><div><button className="icon-button" disabled={page <= 0} onClick={() => onChange(page - 1)} aria-label="上一页"><Icon name="arrow-left" size={15} /></button><span className="page-number">{page + 1} <i>/</i> {totalPages}</span><button className="icon-button" disabled={page >= totalPages - 1} onClick={() => onChange(page + 1)} aria-label="下一页"><Icon name="arrow-right" size={15} /></button></div></div>
}

function UsersView({ notify }: { notify: (message: string, tone?: ToastTone) => void }) {
  const pageSize = 8
  const [items, setItems] = useState<AdminUser[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(0)
  const [draftQuery, setDraftQuery] = useState('')
  const [query, setQuery] = useState('')
  const [status, setStatus] = useState('')
  const [loading, setLoading] = useState(true)
  const [reload, setReload] = useState(0)
  const [resetTarget, setResetTarget] = useState<AdminUser | null>(null)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    getUsers({ query, status, page, size: pageSize }).then((result) => {
      if (!cancelled) { setItems(result.items); setTotal(result.total) }
    }).catch((error) => {
      if (!cancelled) notify(messageOf(error), 'error')
    }).finally(() => { if (!cancelled) setLoading(false) })
    return () => { cancelled = true }
  }, [query, status, page, reload])

  const changeSearch = () => { setQuery(draftQuery.trim()); setPage(0) }
  const toggleStatus = async (user: AdminUser) => {
    const nextStatus = user.status === 1 ? 0 : 1
    if (nextStatus === 0 && !window.confirm(`确定停用用户「${user.username}」吗？`)) return
    try {
      await updateUserStatus(user.id, nextStatus)
      notify(nextStatus === 1 ? '用户已重新启用' : '用户已停用')
      setReload((value) => value + 1)
    } catch (error) { notify(messageOf(error), 'error') }
  }

  return <div className="view">
    <div className="view-intro"><div><p className="eyebrow">PEOPLE / ACCESS</p><h2>用户，保持有序。</h2><p>查看账号状态、角色与最近活动，必要时快速收回访问权限。</p></div><button className="button button-ghost" onClick={() => setReload((value) => value + 1)}><Icon name="refresh" size={16} />刷新列表</button></div>
    <section className="section-card data-card"><TableToolbar><SearchControl value={draftQuery} onChange={setDraftQuery} onSubmit={changeSearch} placeholder="搜索用户名…" /><div className="toolbar-spacer" /><label className="select-control"><span>状态</span><select value={status} onChange={(event) => { setStatus(event.target.value); setPage(0) }}><option value="">全部用户</option><option value="1">启用中</option><option value="0">已停用</option></select><Icon name="chevron-down" size={15} /></label><button className="button button-ghost button-compact" onClick={() => { setQuery(''); setDraftQuery(''); setStatus(''); setPage(0) }}>重置</button></TableToolbar>
      <div className="table-scroll"><table className="data-table"><thead><tr><th>用户</th><th>角色</th><th>状态</th><th>加入时间</th><th>最近更新</th><th className="align-right">操作</th></tr></thead><tbody>{loading ? <LoadingRows columns={6} /> : items.length === 0 ? <EmptyRows columns={6} label="还没有匹配的用户" /> : items.map((user) => <tr key={user.id}><td><div className="person-cell"><div className={`avatar avatar-user avatar-tone-${user.id % 4}`}>{initials(user.username)}</div><div><strong>{user.username}</strong><small>ID · {user.id}</small></div></div></td><td>{user.role === 'ADMIN' ? <span className="role-badge"><Icon name="shield" size={13} />ADMIN</span> : <span className="role-text">PLAYER</span>}</td><td><StatusBadge active={user.status === 1} /></td><td className="muted-cell">{formatDate(user.createdAt)}</td><td className="muted-cell">{formatDate(user.updatedAt)}</td><td><div className="row-actions"><button className="table-action" onClick={() => setResetTarget(user)} title="重置密码"><Icon name="key" size={15} />重置密码</button><button className="icon-button" onClick={() => void toggleStatus(user)} aria-label={user.status === 1 ? '停用用户' : '启用用户'} title={user.status === 1 ? '停用用户' : '启用用户'}><Icon name={user.status === 1 ? 'pause' : 'play'} size={16} /></button></div></td></tr>)}</tbody></table></div><Pagination page={page} size={pageSize} total={total} onChange={setPage} /></section>
    {resetTarget && <ResetPasswordModal user={resetTarget} onClose={() => setResetTarget(null)} notify={notify} />}
  </div>
}

function LoadingRows({ columns }: { columns: number }) {
  return <>{Array.from({ length: 5 }, (_, row) => <tr className="loading-row" key={row}>{Array.from({ length: columns }, (_, column) => <td key={column}><span /></td>)}</tr>)}</>
}

function EmptyRows({ columns, label }: { columns: number; label: string }) {
  return <tr><td colSpan={columns}><div className="empty-state"><div className="empty-icon"><Icon name="search" size={18} /></div><strong>{label}</strong><span>换个关键词，或者清空筛选试试。</span></div></td></tr>
}

function ResetPasswordModal({ user, onClose, notify }: { user: AdminUser; onClose: () => void; notify: (message: string, tone?: ToastTone) => void }) {
  const [password, setPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const submit = async (event: FormEvent) => {
    event.preventDefault()
    setSubmitting(true)
    try {
      await resetUserPassword(user.id, password)
      notify(`已重置 ${user.username} 的登录密码`)
      onClose()
    } catch (error) { notify(messageOf(error), 'error') } finally { setSubmitting(false) }
  }
  return <Modal title="重置登录密码" eyebrow="ACCOUNT SECURITY" onClose={onClose}><form className="modal-form" onSubmit={submit}><p className="modal-context">正在为 <strong>{user.username}</strong> 设置新密码。至少 6 位字符。</p><label>新密码<input type="password" value={password} onChange={(event) => setPassword(event.target.value)} minLength={6} maxLength={64} autoFocus required placeholder="输入新密码" /></label><div className="modal-actions"><button type="button" className="button button-ghost" onClick={onClose}>取消</button><button type="submit" className="button button-primary" disabled={submitting}>{submitting ? '保存中…' : '确认重置'}</button></div></form></Modal>
}

function WordsView({ notify }: { notify: (message: string, tone?: ToastTone) => void }) {
  const pageSize = 8
  const [items, setItems] = useState<AdminWord[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(0)
  const [draftQuery, setDraftQuery] = useState('')
  const [query, setQuery] = useState('')
  const [status, setStatus] = useState('1')
  const [loading, setLoading] = useState(true)
  const [reload, setReload] = useState(0)
  const [editing, setEditing] = useState<AdminWord | null | undefined>(undefined)
  const [importOpen, setImportOpen] = useState(false)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    getWords({ query, status, page, size: pageSize }).then((result) => {
      if (!cancelled) { setItems(result.items); setTotal(result.total) }
    }).catch((error) => {
      if (!cancelled) notify(messageOf(error), 'error')
    }).finally(() => { if (!cancelled) setLoading(false) })
    return () => { cancelled = true }
  }, [query, status, page, reload])

  const changeSearch = () => { setQuery(draftQuery.trim()); setPage(0) }
  const toggleStatus = async (word: AdminWord) => {
    const nextStatus = word.status === 1 ? 0 : 1
    if (nextStatus === 0 && !window.confirm(`停用「${word.english}」后，玩家将不再看到它。继续吗？`)) return
    try {
      await updateWord(word.id, { english: word.english, pronunciation: word.pronunciation, chinese: word.chinese, status: nextStatus })
      notify(nextStatus === 1 ? '词条已重新上线' : '词条已停用')
      setReload((value) => value + 1)
    } catch (error) { notify(messageOf(error), 'error') }
  }

  const handleImport = (result: ImportResult) => {
    const skipped = result.skipped > 0 ? `，跳过 ${result.skipped} 行表头` : ''
    notify(`词表已更新：新增 ${result.added}，修改 ${result.updated}，停用 ${result.disabled}${skipped}`)
    setImportOpen(false)
    setReload((value) => value + 1)
  }

  return <div className="view">
    <div className="view-intro"><div><p className="eyebrow">CATALOG / CONTENT</p><h2>单词，值得被好好编辑。</h2><p>管理玩家每天遇见的词。每次内容变化都会生成新版本，客户端可安全同步。</p></div><div className="intro-actions"><button className="button button-ghost" onClick={() => void downloadWords().then(() => notify('当前启用词表已开始下载')).catch((error) => notify(messageOf(error), 'error'))}><Icon name="download" size={16} />下载 CSV</button><button className="button button-coral" onClick={() => setImportOpen(true)}><Icon name="upload" size={16} />上传词表</button></div></div>
    <section className="section-card data-card word-data-card"><TableToolbar><SearchControl value={draftQuery} onChange={setDraftQuery} onSubmit={changeSearch} placeholder="搜索英文、音标或释义…" /><div className="toolbar-spacer" /><label className="select-control"><span>查看</span><select value={status} onChange={(event) => { setStatus(event.target.value); setPage(0) }}><option value="1">启用词条</option><option value="0">已停用</option><option value="">全部词条</option></select><Icon name="chevron-down" size={15} /></label><button className="button button-ghost button-compact" onClick={() => { setQuery(''); setDraftQuery(''); setStatus('1'); setPage(0) }}>重置</button><button className="button button-dark button-compact" onClick={() => setEditing(null)}><Icon name="plus" size={15} />新增</button></TableToolbar>
      <div className="table-scroll"><table className="data-table words-table"><thead><tr><th className="word-id-head">#</th><th>单词</th><th>音标</th><th>释义</th><th>版本</th><th>状态</th><th>更新</th><th className="align-right">编辑</th></tr></thead><tbody>{loading ? <LoadingRows columns={8} /> : items.length === 0 ? <EmptyRows columns={8} label="没有找到词条" /> : items.map((word) => <tr key={word.id}><td className="word-id">{String(word.id).padStart(4, '0')}</td><td><strong className="english-word">{word.english}</strong></td><td className="pronunciation">{word.pronunciation}</td><td><span className="meaning-cell">{word.chinese}</span></td><td><span className="version-chip">v{word.contentVersion}</span></td><td><StatusBadge active={word.status === 1} /></td><td className="muted-cell">{formatDate(word.updatedAt)}</td><td><div className="row-actions"><button className="icon-button" onClick={() => setEditing(word)} aria-label={`编辑 ${word.english}`} title="编辑"><Icon name="edit" size={16} /></button><button className="icon-button" onClick={() => void toggleStatus(word)} aria-label={word.status === 1 ? '停用词条' : '启用词条'} title={word.status === 1 ? '停用' : '启用'}><Icon name={word.status === 1 ? 'pause' : 'play'} size={16} /></button></div></td></tr>)}</tbody></table></div><Pagination page={page} size={pageSize} total={total} onChange={setPage} /></section>
    {editing !== undefined && <WordModal word={editing} onClose={() => setEditing(undefined)} onSaved={() => { setEditing(undefined); setReload((value) => value + 1) }} notify={notify} />}
    {importOpen && <ImportModal onClose={() => setImportOpen(false)} onImported={handleImport} notify={notify} />}
  </div>
}

function WordModal({ word, onClose, onSaved, notify }: { word: AdminWord | null; onClose: () => void; onSaved: () => void; notify: (message: string, tone?: ToastTone) => void }) {
  const [id, setId] = useState(word?.id ? String(word.id) : '')
  const [english, setEnglish] = useState(word?.english ?? '')
  const [pronunciation, setPronunciation] = useState(word?.pronunciation ?? '')
  const [chinese, setChinese] = useState(word?.chinese ?? '')
  const [status, setStatus] = useState(word?.status ?? 1)
  const [submitting, setSubmitting] = useState(false)
  const isCreate = word === null

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    setSubmitting(true)
    try {
      if (isCreate) {
        await createWord({ id: Number(id), english, pronunciation, chinese })
        notify(`已新增词条「${english}」`)
      } else {
        await updateWord(word.id, { english, pronunciation, chinese, status })
        notify(`已保存「${english}」的内容`)
      }
      onSaved()
    } catch (error) { notify(messageOf(error), 'error') } finally { setSubmitting(false) }
  }

  return <Modal title={isCreate ? '新增词条' : '编辑词条'} eyebrow={isCreate ? 'NEW ENTRY' : `ENTRY / ${String(word.id).padStart(4, '0')}`} onClose={onClose}><form className="modal-form word-form" onSubmit={submit}><div className="form-grid"><label>词条 ID<input type="number" value={id} onChange={(event) => setId(event.target.value)} disabled={!isCreate} min={1} required placeholder="例如 5201" /></label><label>英文<input value={english} onChange={(event) => setEnglish(event.target.value)} maxLength={128} required placeholder="abandon" /></label></div><label>音标<input value={pronunciation} onChange={(event) => setPronunciation(event.target.value)} maxLength={255} required placeholder="/əˈbændən/" /></label><label>中文释义<textarea value={chinese} onChange={(event) => setChinese(event.target.value)} maxLength={1024} required rows={4} placeholder="v. 遗弃；离开；放弃" /></label>{!isCreate && <label>状态<span className="status-toggle"><button type="button" className={status === 1 ? 'selected' : ''} onClick={() => setStatus(1)}><i />启用</button><button type="button" className={status === 0 ? 'selected paused' : ''} onClick={() => setStatus(0)}><i />停用</button></span></label>}<div className="modal-actions"><button type="button" className="button button-ghost" onClick={onClose}>取消</button><button type="submit" className="button button-primary" disabled={submitting}>{submitting ? '保存中…' : '保存词条'}</button></div></form></Modal>
}

function ImportModal({ onClose, onImported, notify }: { onClose: () => void; onImported: (result: ImportResult) => void; notify: (message: string, tone?: ToastTone) => void }) {
  const [file, setFile] = useState<File | null>(null)
  const [replace, setReplace] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const submit = async (event: FormEvent) => {
    event.preventDefault()
    if (!file) { notify('请先选择 CSV 文件', 'error'); return }
    setSubmitting(true)
    try { onImported(await importWords(file, replace)) } catch (error) { notify(messageOf(error), 'error') } finally { setSubmitting(false) }
  }
  return <Modal title="上传新词表" eyebrow="BULK IMPORT" onClose={onClose}><form className="modal-form import-form" onSubmit={submit}><label className={`drop-zone ${file ? 'has-file' : ''}`}><input type="file" accept=".csv,text/csv" onChange={(event) => setFile(event.target.files?.[0] ?? null)} /><span className="drop-icon"><Icon name={file ? 'check' : 'upload'} size={22} /></span><strong>{file ? file.name : '选择 CSV 文件'}</strong><small>{file ? `${(file.size / 1024).toFixed(1)} KB · 已准备上传` : '或将文件拖到这里 · UTF-8 编码'}</small></label><div className="import-format"><span className="format-label">CSV FORMAT</span><code>id, english, pronunciation, chinese</code><p>支持带表头文件；同一 ID 会更新原词条内容。</p></div><label className="switch-row"><span><strong>同步停用缺失词条</strong><small>开启后，原词库中不在本次文件里的词条会被停用。</small></span><button type="button" className={`switch ${replace ? 'is-on' : ''}`} onClick={() => setReplace((value) => !value)} aria-label="同步停用缺失词条"><i /></button></label><div className="modal-actions"><button type="button" className="button button-ghost" onClick={onClose}>取消</button><button type="submit" className="button button-coral" disabled={submitting}>{submitting ? '导入中…' : '开始导入'}</button></div></form></Modal>
}

function Modal({ title, eyebrow, onClose, children }: { title: string; eyebrow: string; onClose: () => void; children: ReactNode }) {
  return <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose() }}><section className="modal" role="dialog" aria-modal="true" aria-label={title}><div className="modal-header"><div><p className="eyebrow">{eyebrow}</p><h3>{title}</h3></div><button className="icon-button" onClick={onClose} aria-label="关闭"><Icon name="x" size={18} /></button></div>{children}</section></div>
}

function Toast({ toast }: { toast: ToastState | null }) {
  if (!toast) return null
  return <div className={`toast toast-${toast.tone}`}><span className="toast-icon"><Icon name={toast.tone === 'success' ? 'check' : 'alert'} size={15} /></span>{toast.message}</div>
}

export default function App() {
  const [token, setToken] = useState<string | null>(() => getToken())
  const [admin, setAdmin] = useState<AdminMe | null>(null)
  const [checking, setChecking] = useState(Boolean(getToken()))
  const [activeTab, setActiveTab] = useState<AdminTab>('overview')
  const [loginError, setLoginError] = useState('')
  const [loggingIn, setLoggingIn] = useState(false)
  const [toast, setToast] = useState<ToastState | null>(null)

  const notify = (message: string, tone: ToastTone = 'success') => {
    setToast({ message, tone })
    window.setTimeout(() => setToast(null), 3600)
  }

  useEffect(() => {
    if (!token) { setChecking(false); setAdmin(null); return }
    let cancelled = false
    setChecking(true)
    getAdminMe().then((me) => { if (!cancelled) setAdmin(me) }).catch(() => {
      if (!cancelled) { clearToken(); setToken(null); setAdmin(null) }
    }).finally(() => { if (!cancelled) setChecking(false) })
    return () => { cancelled = true }
  }, [token])

  const handleLogin = async (username: string, password: string) => {
    setLoggingIn(true)
    setLoginError('')
    try {
      const session = await login(username, password)
      saveToken(session.token)
      const currentAdmin = await getAdminMe()
      setToken(session.token)
      setAdmin(currentAdmin)
      setActiveTab('overview')
    } catch (error) {
      clearToken()
      setToken(null)
      setAdmin(null)
      setLoginError(error instanceof ApiError && error.status === 403 ? '这个账号没有管理端权限。' : messageOf(error))
    } finally { setLoggingIn(false) }
  }

  const logout = () => { clearToken(); setToken(null); setAdmin(null); setLoginError('') }

  if (checking) return <div className="app-loading"><div className="loading-logo">W</div><span>正在打开词库室…</span></div>
  if (!token || !admin) return <LoginScreen onSubmit={handleLogin} submitting={loggingIn} error={loginError} />

  return <><AppShell activeTab={activeTab} onTabChange={setActiveTab} admin={admin} onLogout={logout}>{activeTab === 'overview' && <OverviewView onNavigate={setActiveTab} notify={notify} />}{activeTab === 'users' && <UsersView notify={notify} />}{activeTab === 'words' && <WordsView notify={notify} />}</AppShell><Toast toast={toast} /></>
}
