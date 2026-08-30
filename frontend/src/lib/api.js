// 모든 API 요청이 지나는 단 하나의 창구.
// 토큰 부착, JSON 변환, 에러 처리를 여기서 한 번에 처리한다.
// 화면 코드는 fetch를 직접 쓰지 않고 이 함수만 부른다.

import { getAccessToken } from './auth'

const BASE_URL = 'http://localhost:8080'

async function request(method, path, body) {
    // 1) 기본 헤더 — 보낼 데이터가 JSON임을 알림
    const headers = { 'Content-Type': 'application/json' }

    // 2) 로그인 상태면 출입증(accessToken)을 헤더에 붙인다.
    //    ⚠️ 'Bearer' 뒤에 공백 하나 반드시. 이게 틀리면 서버가 토큰을 못 읽는다.
    const token = getAccessToken()
    if (token) {
        headers['Authorization'] = `Bearer ${token}`
    }

    // 3) 실제 요청
    const res = await fetch(`${BASE_URL}${path}`, {
        method,
        headers,
        body: body ? JSON.stringify(body) : undefined,
    })

    // 4) 응답이 실패(400,401,500...)면 에러를 던진다 → 화면에서 catch로 잡음
    if (!res.ok) {
        let message = `요청 실패 (${res.status})`
        try {
            const errData = await res.json()
            if (errData.message) message = errData.message
        } catch {
            // 응답 본문이 비었거나 JSON이 아니면 위 기본 메시지 사용
        }
        throw new Error(message)
    }

    // 5) 성공 — 본문이 없을 수도 있으니(204) 안전하게 파싱
    if (res.status === 204) return null
    const text = await res.text()
    return text ? JSON.parse(text) : null
}

// 화면에서 쓸 짧은 함수들
export const api = {
    get:  (path)        => request('GET', path),
    post: (path, body)  => request('POST', path, body),
    put:  (path, body)  => request('PUT', path, body),
    del:  (path)        => request('DELETE', path),
}