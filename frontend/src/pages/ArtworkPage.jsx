import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { api } from '../lib/api'
import NavBar from "../components/NavBar.jsx";

function ArtworkPage() {
    const { id } = useParams()         // URL의 :id
    const navigate = useNavigate()

    const [art, setArt] = useState(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    useEffect(() => {
        async function fetchArtwork() {
            setLoading(true)
            setError('')
            try {
                const data = await api.get(`/api/artworks/${id}`)
                setArt(data)
            } catch (err) {
                setError(err.message)
            } finally {
                setLoading(false)
            }
        }
        fetchArtwork()
    }, [id])

    if (loading) return <div style={wrap}><p style={muted}>불러오는 중…</p></div>
    if (error) return <div style={wrap}><p style={{ color: 'var(--danger)' }}>{error}</p></div>
    if (!art) return null

    return (
        <>
            <NavBar />
            <div style={wrap}></div>
            {/* 뒤로 */}
            <button style={back} onClick={() => navigate(-1)}>← 돌아가기</button>

            <div style={grid}>
                {/* 왼쪽: 이미지 */}
                <div>
                    {art.imageUrl
                        ? <img src={art.imageUrl} alt={art.title} style={img} />
                        : <div style={{ ...img, aspectRatio: '3/4', background: 'var(--line)' }} />}
                </div>

                {/* 오른쪽: 정보 */}
                <div>
                    <h1 style={title}>{art.title}</h1>
                    {art.artist && <p style={artist}>{art.artist}</p>}

                    <dl style={meta}>
                        {art.dateDisplay && <><dt style={dt}>제작 시기</dt><dd style={dd}>{art.dateDisplay}</dd></>}
                        {art.medium && <><dt style={dt}>재질</dt><dd style={dd}>{art.medium}</dd></>}
                        {art.department && <><dt style={dt}>부서</dt><dd style={dd}>{art.department}</dd></>}
                        {art.source && <><dt style={dt}>소장</dt><dd style={dd}>{art.source}</dd></>}
                    </dl>

                    {art.sourceUrl && (
                        <a href={art.sourceUrl} target="_blank" rel="noopener noreferrer" style={link}>
                            원본 페이지에서 보기 ↗
                        </a>
                    )}
                </div>
            </div>
        </>
    )
}

const wrap = { padding: 'var(--s6)', maxWidth: 'var(--maxw)', margin: '0 auto' }
const back = { color: 'var(--ink-muted)', fontSize: 'var(--t-caption)', marginBottom: 'var(--s5)' }
const grid = { display: 'grid', gridTemplateColumns: 'minmax(0, 1.2fr) 1fr', gap: 'var(--s7)', alignItems: 'start' }
const img = { width: '100%', objectFit: 'contain', borderRadius: 'var(--radius)', maxHeight: '80vh' }
const title = { fontFamily: 'var(--font-display)', fontSize: 'var(--t-display)', lineHeight: 1.2 }
const artist = { fontSize: 'var(--t-title)', color: 'var(--ink-muted)', marginTop: 'var(--s2)' }
const meta = { marginTop: 'var(--s6)', display: 'grid', gridTemplateColumns: 'auto 1fr', gap: 'var(--s2) var(--s4)' }
const dt = { fontSize: 'var(--t-caption)', color: 'var(--ink-muted)' }
const dd = { fontSize: 'var(--t-caption)', color: 'var(--ink)' }
const link = { display: 'inline-block', marginTop: 'var(--s6)', fontSize: 'var(--t-caption)', color: 'var(--accent)', borderBottom: '1px solid var(--accent)' }
const muted = { color: 'var(--ink-muted)' }

export default ArtworkPage