import { Navigate } from 'react-router-dom'
import { isLoggedIn } from './lib/auth'

// 로그인 상태면 안의 화면을 보여주고, 아니면 로그인으로 보낸다
function ProtectedRoute({ children }) {
    if (!isLoggedIn()) {
        return <Navigate to="/login" />
    }
    return children
}

export default ProtectedRoute