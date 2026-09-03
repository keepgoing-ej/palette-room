import { useNavigate, useLocation } from 'react-router-dom'
import { clearTokens } from '../lib/auth'

function NavBar() {
    const navigate = useNavigate()
    const { pathname } = useLocation()

    function handleLogout() {
        clearTokens()
        navigate('/login')
    }

    return (
        <nav style={bar}>
            <div style={left}>
                <span style={logo} onClick={() => navigate('/gallery')}>Palette Room</span>
                <button
                    style={pathname === '/gallery' ? linkActive : link}
                    onClick={() => navigate('/gallery')}
                >갤러리</button>
                <button
                    style={pathname === '/search' ? linkActive : link}
                    onClick={() => navigate('/search')}
                >색으로 찾기</button>
            </div>
            <button style={link} onClick={handleLogout}>로그아웃</button>
        </nav>
    )
}

const bar = {
    display: 'flex', justifyContent: 'space-between', alignItems: 'center',
    padding: 'var(--s3) var(--s6)', borderBottom: '1px solid var(--line)',
    maxWidth: 'var(--maxw)', margin: '0 auto',
}
const left = { display: 'flex', alignItems: 'center', gap: 'var(--s5)' }
const logo = { fontFamily: 'var(--font-display)', fontSize: 'var(--t-title)', cursor: 'pointer' }
const link = { color: 'var(--ink-muted)', fontSize: 'var(--t-caption)', paddingBottom: '2px', borderBottom: '2px solid transparent' }
const linkActive = { color: 'var(--ink)', fontSize: 'var(--t-caption)', paddingBottom: '2px', borderBottom: '2px solid var(--ink)' }

export default NavBar