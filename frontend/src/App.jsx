import { useState } from 'react'
import { api } from './lib/api'
import { saveTokens, isLoggedIn, clearTokens } from './lib/auth'

function App() {
    // mode: 'login'(로그인) 또는 'signup'(가입) 화면 전환용
    const [mode, setMode] = useState('login')

    // 입력값 상태
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [nickname, setNickname] = useState('')

    // 화면에 보여줄 메시지 (에러 or 성공)
    const [message, setMessage] = useState('')

    // 로그인 여부를 화면에 반영하기 위한 상태
    const [loggedIn, setLoggedIn] = useState(isLoggedIn())

    // ── 로그인 처리 ──
    async function handleLogin() {
        setMessage('')
        try {
            // 서버에 { email, password } 보내고 { accessToken, refreshToken } 받음
            const data = await api.post('/api/auth/login', { email, password })
            saveTokens(data.accessToken, data.refreshToken) // 토큰 저장
            setLoggedIn(true)
            setMessage('로그인 성공')
        } catch (err) {
            setMessage(err.message) // 실패 메시지 표시 (예: 비번 틀림)
        }
    }

    // ── 가입 처리 ──
    async function handleSignup() {
        setMessage('')
        try {
            await api.post('/api/auth/signup', { email, password, nickname })
            setMessage('가입 성공. 이제 로그인하세요.')
            setMode('login') // 가입 끝나면 로그인 탭으로
        } catch (err) {
            setMessage(err.message)
        }
    }

    // ── 로그아웃 (확인용) ──
    function handleLogout() {
        clearTokens()
        setLoggedIn(false)
        setMessage('로그아웃됨')
    }

    // 로그인된 상태면 간단한 확인 화면만 보여준다
    if (loggedIn) {
        return (
            <div style={box}>
                <h1 style={title}>Palette Room</h1>
                <p style={{ color: 'var(--ink-muted)' }}>로그인된 상태입니다.</p>
                <button style={btn} onClick={handleLogout}>로그아웃</button>
                {message && <p style={msg}>{message}</p>}
            </div>
        )
    }

    return (
        <div style={box}>
            <h1 style={title}>Palette Room</h1>

            {/* 탭 전환 */}
            <div style={{ display: 'flex', gap: 'var(--s2)', marginBottom: 'var(--s4)' }}>
                <button
                    style={mode === 'login' ? tabActive : tab}
                    onClick={() => { setMode('login'); setMessage('') }}
                >로그인</button>
                <button
                    style={mode === 'signup' ? tabActive : tab}
                    onClick={() => { setMode('signup'); setMessage('') }}
                >가입</button>
            </div>

            {/* 입력 필드 */}
            <input
                style={input}
                type="email"
                placeholder="이메일"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
            />
            <input
                style={input}
                type="password"
                placeholder="비밀번호"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
            />
            {/* 닉네임은 가입 화면에서만 */}
            {mode === 'signup' && (
                <input
                    style={input}
                    type="text"
                    placeholder="닉네임"
                    value={nickname}
                    onChange={(e) => setNickname(e.target.value)}
                />
            )}

            {/* 제출 버튼 */}
            {mode === 'login'
                ? <button style={btn} onClick={handleLogin}>로그인</button>
                : <button style={btn} onClick={handleSignup}>가입하기</button>
            }

            {/* 메시지 */}
            {message && <p style={msg}>{message}</p>}
        </div>
    )
}

// ── 인라인 스타일 (토큰 사용) ──
// 지금은 확인이 목적이라 인라인으로. 다음 과제에서 CSS 파일로 정리한다.
const box = {
    maxWidth: '360px',
    margin: '80px auto',
    padding: 'var(--s5)',
    background: 'var(--surface)',
    border: '1px solid var(--line)',
    borderRadius: 'var(--radius)',
    display: 'flex',
    flexDirection: 'column',
    gap: 'var(--s3)',
}
const title = {
    fontFamily: 'var(--font-display)',
    fontSize: 'var(--t-display)',
    marginBottom: 'var(--s2)',
}
const input = {
    padding: 'var(--s3)',
    fontSize: 'var(--t-body)',
    border: '1px solid var(--line)',
    borderRadius: 'var(--radius)',
    fontFamily: 'var(--font-body)',
}
const btn = {
    padding: 'var(--s3)',
    fontSize: 'var(--t-body)',
    background: 'var(--accent)',
    color: '#fff',
    borderRadius: 'var(--radius)',
}
const tab = {
    padding: 'var(--s2) var(--s3)',
    color: 'var(--ink-muted)',
    borderBottom: '2px solid transparent',
}
const tabActive = {
    padding: 'var(--s2) var(--s3)',
    color: 'var(--ink)',
    borderBottom: '2px solid var(--accent)',
}
const msg = {
    fontSize: 'var(--t-caption)',
    color: 'var(--ink-muted)',
}

export default App