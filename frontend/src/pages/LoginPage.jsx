import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../lib/api'
import { saveTokens } from '../lib/auth'

function LoginPage() {
    const navigate = useNavigate()   // 주소 이동시키는 도구

    const [mode, setMode] = useState('login')
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [nickname, setNickname] = useState('')
    const [message, setMessage] = useState('')

    async function handleLogin() {
        setMessage('')
        try {
            const data = await api.post('/api/auth/login', { email, password })
            saveTokens(data.accessToken, data.refreshToken)
            navigate('/gallery')   // 로그인 성공 → 갤러리로 이동
        } catch (err) {
            setMessage(err.message)
        }
    }

    async function handleSignup() {
        setMessage('')
        try {
            await api.post('/api/auth/signup', { email, password, nickname })
            setMessage('가입 성공. 이제 로그인하세요.')
            setMode('login')
        } catch (err) {
            setMessage(err.message)
        }
    }

    return (
        <div style={box}>
            <h1 style={title}>Palette Room</h1>

            <div style={{ display: 'flex', gap: 'var(--s2)', marginBottom: 'var(--s4)' }}>
                <button style={mode === 'login' ? tabActive : tab}
                        onClick={() => { setMode('login'); setMessage('') }}>로그인</button>
                <button style={mode === 'signup' ? tabActive : tab}
                        onClick={() => { setMode('signup'); setMessage('') }}>가입</button>
            </div>

            <input style={input} type="email" placeholder="이메일"
                   value={email} onChange={(e) => setEmail(e.target.value)} />
            <input style={input} type="password" placeholder="비밀번호"
                   value={password} onChange={(e) => setPassword(e.target.value)} />
            {mode === 'signup' && (
                <input style={input} type="text" placeholder="닉네임"
                       value={nickname} onChange={(e) => setNickname(e.target.value)} />
            )}

            {mode === 'login'
                ? <button style={btn} onClick={handleLogin}>로그인</button>
                : <button style={btn} onClick={handleSignup}>가입하기</button>}

            {message && <p style={msg}>{message}</p>}
        </div>
    )
}

const box = { maxWidth: '360px', margin: '80px auto', padding: 'var(--s5)',
    background: 'var(--surface)', border: '1px solid var(--line)',
    borderRadius: 'var(--radius)', display: 'flex', flexDirection: 'column', gap: 'var(--s3)' }
const title = { fontFamily: 'var(--font-display)', fontSize: 'var(--t-display)', marginBottom: 'var(--s2)' }
const input = { padding: 'var(--s3)', fontSize: 'var(--t-body)', border: '1px solid var(--line)',
    borderRadius: 'var(--radius)', fontFamily: 'var(--font-body)' }
const btn = { padding: 'var(--s3)', fontSize: 'var(--t-body)', background: 'var(--accent)',
    color: '#fff', borderRadius: 'var(--radius)' }
const tab = { padding: 'var(--s2) var(--s3)', color: 'var(--ink-muted)', borderBottom: '2px solid transparent' }
const tabActive = { padding: 'var(--s2) var(--s3)', color: 'var(--ink)', borderBottom: '2px solid var(--accent)' }
const msg = { fontSize: 'var(--t-caption)', color: 'var(--ink-muted)' }

export default LoginPage