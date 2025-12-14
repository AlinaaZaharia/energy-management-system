import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './auth'
import NavBar from './components/NavBar'
import ProtectedRoute from './components/ProtectedRoute'
import LoginPage from './pages/Login'
import AdminUsers from './pages/AdminUsers'
import AdminDevices from './pages/AdminDevices'
import ClientDevices from './pages/ClientDevices'
import NotificationToast from './components/NotificationToast'
import ChatBox from './components/ChatBox'


function Home() {
    const { token } = useAuth()

    if (!token) {
        return <Navigate to="/login" replace />
    }

    return (
        <div className="container">
            <div className="card" style={{ textAlign: 'center', padding: 40 }}>
                <h2>Welcome to the System</h2>
                <p>Use the navigation menu above to access your features.</p>
            </div>
        </div>
    )
}

export default function App() {
    return (
        <AuthProvider>
            <NavBar />
            <NotificationToast />
            <ChatBox />
            <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/login" element={<LoginPage />} />

                <Route path="/admin/users" element={
                    <ProtectedRoute roles={["ADMIN"]}>
                        <AdminUsers />
                    </ProtectedRoute>
                } />
                <Route path="/admin/devices" element={
                    <ProtectedRoute roles={["ADMIN"]}>
                        <AdminDevices />
                    </ProtectedRoute>
                } />

                <Route path="/client/devices" element={
                    <ProtectedRoute roles={["CLIENT", "ADMIN"]}>
                        <ClientDevices />
                    </ProtectedRoute>
                } />

                <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
        </AuthProvider>
    )
}