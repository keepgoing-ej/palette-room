import { Routes, Route, Navigate } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import GalleryPage from './pages/GalleryPage'
import ProtectedRoute from './ProtectedRoute'
import SearchPage from "./pages/SearchPage.jsx";
import ArtworkPage from "./pages/ArtworkPage.jsx";

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
            <Route path="/search" element={
                <ProtectedRoute>
                    <SearchPage />
                </ProtectedRoute>
            } />
            <Route path="/artwork/:id" element={
                <ProtectedRoute>
                    <ArtworkPage />
                </ProtectedRoute>
            } />
        </Routes>
    )
}

export default App