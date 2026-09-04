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

function isLowChroma(hex) {
    const h = hex.replace('#', '')
    const r = parseInt(h.slice(0, 2), 16)
    const g = parseInt(h.slice(2, 4), 16)
    const b = parseInt(h.slice(4, 6), 16)
    return (Math.max(r, g, b) - Math.min(r, g, b)) < 30
}

function SearchPage() {
    const navigate = useNavigate()
    const [colorGroup, setColorGroup] = useState('warm')
    const [artCategory, setArtCategory] = useState('')
    const [selected, setSelected] = useState(null)       // 현재 선택된 색 (팔레트/피커 공통)
    const [customColor, setCustomColor] = useState('#BA865C')
    const [tolerance, setTolerance] = useState(15)
    const [keyword, setKeyword] = useState('')
    const [results, setResults] = useState([])
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState('')
    const [searched, setSearched] = useState(false)
    const [lowChromaWarning, setLowChromaWarning] = useState(false)

    // [변경] 통합 검색 — 색·텍스트 어느 조합이든 처리
    //  color만 / text만 / color+text 병행
    async function runSearch({ hex = selected, cat = artCategory, tol = tolerance, kw = keyword }) {
        const hasColor = !!hex
        const hasText = kw.trim().length > 0
        if (!hasColor && !hasText) return   // 둘 다 없으면 아무것도 안 함

        setSearched(true)
        setLoading(true); setError('')
        try {
            let data
            if (hasColor) {
                // 색 검색 (+ 텍스트 있으면 keyword로 병행)
                const catParam = cat ? `&category=${cat}` : ''
                const kwParam = hasText ? `&keyword=${encodeURIComponent(kw.trim())}` : ''
                data = await api.get(`/api/artworks/search?hex=${encodeURIComponent(hex)}&tolerance=${tol}&limit=30${catParam}${kwParam}`)
            } else {
                // 텍스트만 → 제목 검색 API (Page 응답 → content, id를 artworkId로)
                const res = await api.get(`/api/artworks/search-text?keyword=${encodeURIComponent(kw.trim())}&size=30`)
                data = res.content.map(a => ({ ...a, artworkId: a.id }))
            }
            setResults(data)
        } catch (err) { setError(err.message) } finally { setLoading(false) }
    }

    // 팔레트/피커에서 색 선택
    function pickColor(hex) {
        setSelected(hex)
        setLowChromaWarning(isLowChroma(hex))
        runSearch({ hex })
    }
    // 오차 변경 → 색 있으면 재검색
    function changeTol(tol) {
        setTolerance(tol)
        if (selected) runSearch({ tol })
    }
    // 종류 변경 → 색 있으면 재검색
    function changeCat(cat) {
        setArtCategory(cat)
        if (selected) runSearch({ cat })
    }
    // 텍스트 검색 버튼/Enter
    function doTextSearch() {
        runSearch({ kw: keyword })
    }
    // 색 선택 해제(텍스트만 검색하고 싶을 때)
    function clearColor() {
        setSelected(null)
        setLowChromaWarning(false)
        if (keyword.trim()) runSearch({ hex: null, kw: keyword })
        else { setResults([]); setSearched(false) }
    }

    const hasColor = !!selected

    return (
        <>
            <NavBar />
            <div style={{ padding: 'var(--s6)', maxWidth: 'var(--maxw)', margin: '0 auto' }}>
                {/* [변경] 3분할 균등 그리드 */}
                <div style={topGrid}>
                    {/* 1. 팔레트 */}
                    <div style={col}>
                        <div style={tabs}>
                            {Object.keys(PALETTES).map((key) => (
                                <button key={key} onClick={() => setColorGroup(key)} style={colorGroup === key ? tabActive : tab}>
                                    {PALETTES[key].label}
                                </button>
                            ))}
                        </div>
                        <div style={palette}>
                            {PALETTES[colorGroup].colors.map((hex) => (
                                <button key={hex} onClick={() => pickColor(hex)}
                                        style={{ ...tile, background: hex, outline: selected === hex ? '3px solid var(--ink)' : 'none', outlineOffset: '2px' }}
                                        aria-label={hex} />
                            ))}
                        </div>
                    </div>

                    <div style={divider} />

                    {/* 2. 직접 색 고르기 */}
                    <div style={col}>
                        <p style={{ ...muted, marginBottom: 'var(--s2)' }}>또는 직접 고르기</p>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--s2)', flexWrap: 'wrap' }}>
                            <input type="color" value={customColor} onChange={(e) => setCustomColor(e.target.value)} style={colorInput} />
                            <span style={mono}>{customColor.toUpperCase()}</span>
                            <button style={searchBtn} onClick={() => pickColor(customColor.toUpperCase())}>이 색으로</button>
                        </div>
                    </div>

                    <div style={divider} />

                    {/* 3. 이름으로 검색 */}
                    <div style={col}>
                        <p style={{ ...muted, marginBottom: 'var(--s2)' }}>이름으로 검색 (영문)</p>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--s2)' }}>
                            <input
                                type="text"
                                value={keyword}
                                onChange={(e) => setKeyword(e.target.value)}
                                onKeyDown={(e) => { if (e.key === 'Enter') doTextSearch() }}
                                placeholder="예: woman, river, horse"
                                style={textInput}
                            />
                            <button style={searchBtn} onClick={doTextSearch}>검색</button>
                        </div>
                    </div>
                </div>

                {/* [변경] 현재 선택 상태 배지 (색+텍스트 병행 시 뭘로 찾는지 보여줌) */}
                {(hasColor || keyword.trim()) && (
                    <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--s2)', marginBottom: 'var(--s4)', flexWrap: 'wrap' }}>
                        <span style={muted}>검색 조건:</span>
                        {hasColor && (
                            <span style={badge}>
                                <span style={{ width: 12, height: 12, borderRadius: '50%', background: selected, display: 'inline-block' }} />
                                {selected}
                                <button style={badgeX} onClick={clearColor} aria-label="색 제거">×</button>
                            </span>
                        )}
                        {keyword.trim() && <span style={badge}>"{keyword.trim()}"</span>}
                    </div>
                )}

                {/* 오차·종류 필터 — 색 검색일 때만 */}
                {hasColor && (
                    <>
                        <div style={row}>
                            <span style={muted}>허용 오차</span>
                            <input type="range" min="5" max="40" value={tolerance}
                                   onChange={(e) => changeTol(Number(e.target.value))}
                                   style={{ width: 160, accentColor: 'var(--accent)' }} />
                            <span style={mono}>{tolerance}</span>
                            <span style={{ ...muted, fontSize: 'var(--t-micro)' }}>작을수록 정확 · 클수록 넓게</span>
                        </div>
                        <div style={catRow}>
                            {CATEGORIES.map((c) => (
                                <button key={c.key} onClick={() => changeCat(c.key)}
                                        style={artCategory === c.key ? catActive : catBtn}>
                                    {c.label}
                                </button>
                            ))}
                        </div>
                    </>
                )}

                {/* 무채색 안내 */}
                {lowChromaWarning && (
                    <div style={notice}>
                        흰색·회색·검정 같은 무채색은 작품 배경·여백과 색이 겹쳐 정확도가 낮습니다.
                        <b> 종류 필터(도자·유리, 조각·유물 등)와 함께</b> 쓰면 원하는 작품을 더 잘 찾을 수 있어요.
                    </div>
                )}

                {/* 결과 */}
                {loading && <p style={muted}>찾는 중…</p>}
                {error && <p style={{ color: 'var(--danger)' }}>{error}</p>}
                {searched && !loading && !error && results.length === 0 && (
                    <p style={muted}>결과가 없어요. 다른 색이나 검색어를 시도해보세요.</p>
                )}

                {!loading && !error && results.length > 0 && (
                    <div style={grid}>
                        {results.map((art) => (
                            <div key={art.artworkId} style={card} onClick={() => navigate(`/artwork/${art.artworkId}`)}>
                                {art.imageUrl
                                    ? <img src={art.imageUrl} alt={art.title} style={img} />
                                    : <div style={{ ...img, background: 'var(--line)' }} />}
                                {hasColor && art.hex && (
                                    <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--s1)', marginTop: 'var(--s2)' }}>
                                        <span style={{ width: 12, height: 12, borderRadius: '50%', background: art.hex, flexShrink: 0 }} />
                                        <span style={cardMeta}>{art.hex}</span>
                                    </div>
                                )}
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

// [변경] 3분할 균등 그리드
const topGrid = { display: 'grid', gridTemplateColumns: '1fr auto 1fr auto 1fr', alignItems: 'flex-start', gap: 'var(--s5)', marginBottom: 'var(--s5)' }
const col = { minWidth: 0 }
const divider = { width: 1, alignSelf: 'stretch', background: 'var(--line)' }
const palette = { display: 'flex', gap: 'var(--s2)', flexWrap: 'wrap' }
const tile = { width: 48, height: 48, borderRadius: 'var(--radius)', border: '1px solid var(--line)', cursor: 'pointer' }
const row = { display: 'flex', alignItems: 'center', gap: 'var(--s3)', marginBottom: 'var(--s4)', flexWrap: 'wrap' }
const muted = { color: 'var(--ink-muted)', fontSize: 'var(--t-caption)' }
const mono = { fontFamily: 'var(--font-mono)', fontSize: 'var(--t-caption)' }
const colorInput = { width: 44, height: 44, border: '1px solid var(--line)', borderRadius: 'var(--radius)', cursor: 'pointer', padding: 0, flexShrink: 0 }
const textInput = { flex: 1, minWidth: 0, padding: 'var(--s2) var(--s3)', border: '1px solid var(--line)', borderRadius: 'var(--radius)', fontSize: 'var(--t-caption)', fontFamily: 'var(--font-body)' }
const searchBtn = { padding: 'var(--s2) var(--s3)', background: 'var(--accent)', color: '#fff', borderRadius: 'var(--radius)', fontSize: 'var(--t-caption)', whiteSpace: 'nowrap', flexShrink: 0 }
const grid = { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: 'var(--s4)' }
const card = { display: 'flex', flexDirection: 'column', gap: 'var(--s1)', cursor: 'pointer' }
const img = { width: '100%', aspectRatio: '3 / 4', objectFit: 'cover', borderRadius: 'var(--radius)' }
const cardTitle = { fontSize: 'var(--t-body)', marginTop: 'var(--s1)' }
const cardMeta = { fontSize: 'var(--t-caption)', color: 'var(--ink-muted)' }
const tabs = { display: 'flex', gap: 'var(--s3)', marginBottom: 'var(--s3)', flexWrap: 'wrap' }
const tab = { padding: '0 0 var(--s1)', fontSize: 'var(--t-caption)', color: 'var(--ink-muted)', borderBottom: '2px solid transparent' }
const tabActive = { padding: '0 0 var(--s1)', fontSize: 'var(--t-caption)', color: 'var(--ink)', borderBottom: '2px solid var(--ink)' }
const catRow = { display: 'flex', gap: 'var(--s2)', flexWrap: 'wrap', marginBottom: 'var(--s5)' }
const catBtn = { padding: 'var(--s1) var(--s3)', border: '1px solid var(--line)', borderRadius: 'var(--radius)', color: 'var(--ink-muted)', fontSize: 'var(--t-caption)' }
const catActive = { padding: 'var(--s1) var(--s3)', border: '1px solid var(--ink)', borderRadius: 'var(--radius)', background: 'var(--ink)', color: '#fff', fontSize: 'var(--t-caption)' }
const notice = { padding: 'var(--s3)', marginBottom: 'var(--s5)', background: 'var(--gallery-bg)', border: '1px solid var(--line)', borderRadius: 'var(--radius)', fontSize: 'var(--t-caption)', color: 'var(--ink-muted)', lineHeight: 1.6 }
// [변경] 검색 조건 배지
const badge = { display: 'inline-flex', alignItems: 'center', gap: 'var(--s1)', padding: 'var(--s1) var(--s2)', border: '1px solid var(--line)', borderRadius: 'var(--radius)', fontSize: 'var(--t-caption)', fontFamily: 'var(--font-mono)' }
const badgeX = { marginLeft: '2px', color: 'var(--ink-muted)', fontSize: 'var(--t-body)', lineHeight: 1, cursor: 'pointer' }

export default SearchPage