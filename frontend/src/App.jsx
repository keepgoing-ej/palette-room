import { Routes, Route, Navigate } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import GalleryPage from './pages/GalleryPage'
import ProtectedRoute from './ProtectedRoute'

function App() {
    return (
        <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/gallery" element={
                <ProtectedRoute>
                    <GalleryPage />
                </ProtectedRoute>
            } />
            <Route path="/" element={<Navigate to="/login" />} />
        </Routes>
    )
}

export default App