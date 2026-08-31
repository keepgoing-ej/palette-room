import { useNavigate } from 'react-router-dom'
import { clearTokens } from '../lib/auth'

function GalleryPage() {
    const navigate = useNavigate()

    function handleLogout() {
        clearTokens()
        navigate('/login')   // 로그아웃 → 로그인 화면으로
    }

    return (
        <div style={{ padding: 'var(--s6)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h1 style={{ fontFamily: 'var(--font-display)', fontSize: 'var(--t-display)' }}>
                    Gallery
                </h1>
                <button style={{ color: 'var(--ink-muted)' }} onClick={handleLogout}>로그아웃</button>
            </div>
            <p style={{ color: 'var(--ink-muted)', marginTop: 'var(--s4)' }}>
                여기에 작품 목록이 들어갈 예정
            </p>
        </div>
    )
}

export default GalleryPage