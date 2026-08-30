// 토큰 저장소 — localStorage를 감싸서 이름 오타를 방지한다.
// 화면 코드는 이 함수들만 쓰고 localStorage를 직접 만지지 않는다.

const ACCESS = 'accessToken'
const REFRESH = 'refreshToken'

// 로그인 성공 시: 토큰 2개를 한 번에 저장
export function saveTokens(accessToken, refreshToken) {
    localStorage.setItem(ACCESS, accessToken)
    localStorage.setItem(REFRESH, refreshToken)
}

// 요청에 붙일 때: accessToken 꺼내기 (없으면 null)
export function getAccessToken() {
    return localStorage.getItem(ACCESS)
}

// 재발급할 때: refreshToken 꺼내기
export function getRefreshToken() {
    return localStorage.getItem(REFRESH)
}

// 로그인 여부 — accessToken이 있으면 로그인된 것으로 본다
export function isLoggedIn() {
    return !!localStorage.getItem(ACCESS)
}

// 로그아웃 — 토큰 둘 다 삭제
export function clearTokens() {
    localStorage.removeItem(ACCESS)
    localStorage.removeItem(REFRESH)
}