import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../lib/api'
import { clearTokens } from '../lib/auth'

function GalleryPage() {
    const navigate = useNavigate()

    const [artworks, setArtworks] = useState([])
    const [page, setPage] = useState(0)           // 현재 페이지 (0부터 시작)
    const [totalPages, setTotalPages] = useState(0) // 전체 페이지 수
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    // page가 바뀔 때마다 다시 불러온다
    useEffect(() => {
        async function fetchArtworks() {
            setLoading(true)
            setError('')
            try {
                const data = await api.get(`/api/artworks?page=${page}&size=20`)
                setArtworks(data.content)
                setTotalPages(data.totalPages)
                window.scrollTo(0, 0)   // 페이지 넘기면 맨 위로
            } catch (err) {
                setError(err.message)
            } finally {
                setLoading(false)
            }
        }
        fetchArtworks()
    }, [page])

    function handleLogout() {
        clearTokens()
        navigate('/login')
    }


    // 현재 페이지가 속한 10칸 블록의 시작 (0,10,20…)
    const blockStart = Math.floor(page / 10) * 10
    // 이번 블록에 그릴 번호들 (끝 페이지 넘지 않게 자름)
    const pageNumbers = []
    for (let p = blockStart; p < Math.min(blockStart + 10, totalPages); p++) {
        pageNumbers.push(p)
    }

    return (
        <div style={{ padding: 'var(--s6)', maxWidth: 'var(--maxw)', margin: '0 auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--s5)' }}>
                <h1 style={{ fontFamily: 'var(--font-display)', fontSize: 'var(--t-display)' }}>Gallery</h1>
                <button style={{ color: 'var(--ink-muted)' }} onClick={handleLogout}>로그아웃</button>
            </div>

            {loading && <p style={{ color: 'var(--ink-muted)' }}>불러오는 중…</p>}
            {error && <p style={{ color: 'var(--danger)' }}>{error}</p>}

            {!loading && !error && (
                <>
                    <div style={grid}>
                        {artworks.map((art) => (
                            <div key={art.id} style={card}>
                                {art.imageUrl
                                    ? <img src={art.imageUrl} alt={art.title} style={img} />
                                    : <div style={{ ...img, background: 'var(--line)' }} />}
                                <p style={cardTitle}>{art.title}</p>
                                <p style={cardMeta}>{art.artist}</p>
                                <p style={cardMeta}>{art.dateDisplay}</p>
                            </div>
                        ))}
                    </div>

                    {/* 페이징 — 10개씩 번호 블록 */}
                    <div style={pager}>
                        {/* 이전 10칸 */}
                        <button
                            style={pageBtn}
                            onClick={() => setPage(Math.max(0, blockStart - 10))}
                            disabled={blockStart === 0}
                        >«</button>

                        {/* 이전 1칸 */}
                        <button
                            style={pageBtn}
                            onClick={() => setPage(page - 1)}
                            disabled={page === 0}
                        >‹</button>

                        {/* 번호 버튼 10개 */}
                        {pageNumbers.map((p) => (
                            <button
                                key={p}
                                style={p === page ? pageBtnActive : pageBtn}
                                onClick={() => setPage(p)}
                            >{p + 1}</button>
                        ))}

                        {/* 다음 1칸 */}
                        <button
                            style={pageBtn}
                            onClick={() => setPage(page + 1)}
                            disabled={page >= totalPages - 1}
                        >›</button>

                        {/* 다음 10칸 */}
                        <button
                            style={pageBtn}
                            onClick={() => setPage(Math.min(totalPages - 1, blockStart + 10))}
                            disabled={blockStart + 10 >= totalPages}
                        >»</button>
                    </div>
                </>
            )}
        </div>
    )
}

const grid = {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))',
    gap: 'var(--s4)',
}
const card = { display: 'flex', flexDirection: 'column', gap: 'var(--s1)' }
const img = { width: '100%', aspectRatio: '3 / 4', objectFit: 'cover', borderRadius: 'var(--radius)' }
const cardTitle = { fontSize: 'var(--t-body)', marginTop: 'var(--s2)' }
const cardMeta = { fontSize: 'var(--t-caption)', color: 'var(--ink-muted)' }
 // 버튼 간격 수정
const pager = {
    display: 'flex', justifyContent: 'center', alignItems: 'center', gap: 'var(--s2)',
    flexWrap: 'wrap',
    marginTop: 'var(--s6)', paddingTop: 'var(--s5)', borderTop: '1px solid var(--line)',
}
 // 페이징 버튼
const pageBtn = {
    padding: 'var(--s1) var(--s3)', border: '1px solid var(--line)',
    borderRadius: 'var(--radius)', color: 'var(--ink)',
    fontSize: 'var(--t-caption)',
} //페이징 버튼
const pageBtnActive = {
    padding: 'var(--s1) var(--s3)', border: '1px solid #6B4E9E',
    borderRadius: 'var(--radius)', background: '#6B4E9E', color: '#fff',
    fontSize: 'var(--t-caption)',
}

export default GalleryPage