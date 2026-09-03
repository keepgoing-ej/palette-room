import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../lib/api'
import NavBar from '../components/NavBar'

const PALETTES = {
    warm: { label: '따뜻한 색', colors: ['#CCB781', '#C2703D', '#B0413E', '#8A4A2F', '#6E3B2A'] },
    cool: { label: '차가운 색', colors: ['#7A9CB0', '#3E7CC4', '#2E6E6A', '#274B6E', '#233D4D'] },
    neutral: { label: '중성색', colors: ['#6B7A45', '#4A6B3A', '#7A5C8E', '#5B3A6B', '#8E3A5C'] },
}

const CATEGORIES = [
    { key: '', label: '전체' },
    { key: 'painting', label: '회화' },
    { key: 'print', label: '판화·드로잉' },
    { key: 'ceramic', label: '도자·유리' },
    { key: 'sculpture', label: '조각·유물' },
]

function SearchPage() {
    const navigate = useNavigate()
    const [colorGroup, setColorGroup] = useState('warm')     // 색 계열 탭
    const [artCategory, setArtCategory] = useState('')        // 종류 필터 ('' = 전체)
    const [selected, setSelected] = useState(null)
    const [customColor, setCustomColor] = useState('#BA865C')
    const [tolerance, setTolerance] = useState(15)
    const [results, setResults] = useState([])
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState('')
    const [searched, setSearched] = useState(false)

    // 색 선택 → 검색 (현재 tolerance·category 사용)
    async function handleSearch(hex) {
        setSelected(hex)
        setSearched(true)
        await runSearch(hex, tolerance, artCategory)
    }

    // 오차 슬라이더 → 즉시 재검색
    async function handleTolChange(tol) {
        setTolerance(tol)
        if (selected) await runSearch(selected, tol, artCategory)
    }

    // 종류 필터 → 즉시 재검색
    async function handleCatChange(cat) {
        setArtCategory(cat)
        if (selected) await runSearch(selected, tolerance, cat)
    }

    // 실제 API 호출 (공통) — hex·tol·cat 모두 인자로 받아 최신값 보장
    async function runSearch(hex, tol, cat) {
        setLoading(true)
        setError('')
        try {
            const catParam = cat ? `&category=${cat}` : ''
            const data = await api.get(`/api/artworks/search?hex=${encodeURIComponent(hex)}&tolerance=${tol}&limit=30${catParam}`)
            setResults(data)
        } catch (err) {
            setError(err.message)
        } finally {
            setLoading(false)
        }
    }

    return (
        <>
            <NavBar />
            <div style={{ padding: 'var(--s6)', maxWidth: 'var(--maxw)', margin: '0 auto' }}>
                <div style={topRow}>
                    <div>
                        <div style={tabs}>
                            {Object.keys(PALETTES).map((key) => (
                                <button key={key} onClick={() => setColorGroup(key)} style={colorGroup === key ? tabActive : tab}>
                                    {PALETTES[key].label}
                                </button>
                            ))}
                        </div>
                        <div style={palette}>
                            {PALETTES[colorGroup].colors.map((hex) => (
                                <button key={hex} onClick={() => handleSearch(hex)}
                                        style={{ ...tile, background: hex, outline: selected === hex ? '3px solid var(--ink)' : 'none', outlineOffset: '2px' }}
                                        aria-label={hex} />
                            ))}
                        </div>
                    </div>

                    <div style={divider} />

                    <div>
                        <p style={{ ...muted, marginBottom: 'var(--s2)' }}>또는 직접 고르기</p>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--s3)' }}>
                            <input type="color" value={customColor} onChange={(e) => setCustomColor(e.target.value)} style={colorInput} />
                            <span style={mono}>{customColor.toUpperCase()}</span>
                            <button style={searchBtn} onClick={() => handleSearch(customColor.toUpperCase())}>이 색으로 검색</button>
                        </div>
                    </div>
                </div>

                {/* 허용 오차 */}
                <div style={row}>
                    <span style={muted}>허용 오차</span>
                    <input type="range" min="5" max="40" value={tolerance}
                           onChange={(e) => handleTolChange(Number(e.target.value))}
                           style={{ width: 160, accentColor: 'var(--accent)' }} />
                    <span style={mono}>{tolerance}</span>
                    <span style={{ ...muted, fontSize: 'var(--t-micro)' }}>작을수록 정확 · 클수록 넓게</span>
                </div>

                {/* 종류 필터 */}
                <div style={catRow}>
                    {CATEGORIES.map((c) => (
                        <button key={c.key} onClick={() => handleCatChange(c.key)}
                                style={artCategory === c.key ? catActive : catBtn}>
                            {c.label}
                        </button>
                    ))}
                </div>

                {/* 결과 */}
                {loading && <p style={muted}>찾는 중…</p>}
                {error && <p style={{ color: 'var(--danger)' }}>{error}</p>}
                {searched && !loading && !error && results.length === 0 && (
                    <p style={muted}>이 색과 가까운 작품이 없어요. 오차를 넓히거나 다른 색·종류를 골라보세요.</p>
                )}

                {!loading && !error && results.length > 0 && (
                    <div style={grid}>
                        {results.map((art) => (
                            <div key={art.artworkId} style={card} onClick={() => navigate(`/artwork/${art.artworkId}`)}>
                                {art.imageUrl
                                    ? <img src={art.imageUrl} alt={art.title} style={img} />
                                    : <div style={{ ...img, background: 'var(--line)' }} />}
                                <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--s1)', marginTop: 'var(--s2)' }}>
                                    <span style={{ width: 12, height: 12, borderRadius: '50%', background: art.hex, flexShrink: 0 }} />
                                    <span style={cardMeta}>{art.hex}</span>
                                </div>
                                <p style={cardTitle}>{art.title}</p>
                                {art.artist && <p style={cardMeta}>{art.artist}</p>}
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </>
    )
}

const palette = { display: 'flex', gap: 'var(--s2)', flexWrap: 'wrap', marginBottom: 'var(--s4)' }
const topRow = { display: 'flex', alignItems: 'flex-start', gap: 'var(--s6)', marginBottom: 'var(--s5)', flexWrap: 'wrap' }
const divider = { width: 1, alignSelf: 'stretch', background: 'var(--line)' }
const tile = { width: 56, height: 56, borderRadius: 'var(--radius)', border: '1px solid var(--line)', cursor: 'pointer' }
const row = { display: 'flex', alignItems: 'center', gap: 'var(--s3)', marginBottom: 'var(--s4)', flexWrap: 'wrap' }
const muted = { color: 'var(--ink-muted)', fontSize: 'var(--t-caption)' }
const mono = { fontFamily: 'var(--font-mono)', fontSize: 'var(--t-caption)' }
const colorInput = { width: 48, height: 48, border: '1px solid var(--line)', borderRadius: 'var(--radius)', cursor: 'pointer', padding: 0 }
const searchBtn = { padding: 'var(--s2) var(--s4)', background: 'var(--accent)', color: '#fff', borderRadius: 'var(--radius)', fontSize: 'var(--t-caption)' }
const grid = { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: 'var(--s4)' }
const card = { display: 'flex', flexDirection: 'column', gap: 'var(--s1)', cursor: 'pointer' }
const img = { width: '100%', aspectRatio: '3 / 4', objectFit: 'cover', borderRadius: 'var(--radius)' }
const cardTitle = { fontSize: 'var(--t-body)', marginTop: 'var(--s1)' }
const cardMeta = { fontSize: 'var(--t-caption)', color: 'var(--ink-muted)' }
const tabs = { display: 'flex', gap: 'var(--s4)', marginBottom: 'var(--s3)' }
const tab = { padding: '0 0 var(--s1)', fontSize: 'var(--t-caption)', color: 'var(--ink-muted)', borderBottom: '2px solid transparent' }
const tabActive = { padding: '0 0 var(--s1)', fontSize: 'var(--t-caption)', color: 'var(--ink)', borderBottom: '2px solid var(--ink)' }
const catRow = { display: 'flex', gap: 'var(--s2)', flexWrap: 'wrap', marginBottom: 'var(--s5)' }
const catBtn = { padding: 'var(--s1) var(--s3)', border: '1px solid var(--line)', borderRadius: 'var(--radius)', color: 'var(--ink-muted)', fontSize: 'var(--t-caption)' }
const catActive = { padding: 'var(--s1) var(--s3)', border: '1px solid var(--ink)', borderRadius: 'var(--radius)', background: 'var(--ink)', color: '#fff', fontSize: 'var(--t-caption)' }

export default SearchPage