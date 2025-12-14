import React, { createContext, useContext, useEffect, useState } from 'react'
import { api } from './api'

const AuthCtx = createContext(null)

export function AuthProvider({ children }) {
    const [token, setToken] = useState(() => localStorage.getItem('token'))
    const [role, setRole] = useState(() => localStorage.getItem('role'))
    const [username, setUsername] = useState(() => localStorage.getItem('username'))
    const [userId, setUserId] = useState(() => localStorage.getItem('userId'))
    const [name, setName] = useState(() => localStorage.getItem('name'))
    const [loading, setLoading] = useState(false)

    async function login(username, password) {
        setLoading(true)
        try {
            const res = await api.login(username, password)
            localStorage.setItem('token', res.token)
            localStorage.setItem('role', res.role)
            localStorage.setItem('username', res.username)
            localStorage.setItem('userId', res.userId)

            setToken(res.token)
            setRole(res.role)
            setUsername(res.username)
            setUserId(res.userId)

            try {
                const userDetails = await api.getUser(res.userId)
                localStorage.setItem('name', userDetails.name)
                setName(userDetails.name)
            } catch {
                localStorage.setItem('name', res.username)
                setName(res.username)
            }

            return res
        } finally {
            setLoading(false)
        }
    }

    function logout() {
        localStorage.removeItem('token')
        localStorage.removeItem('role')
        localStorage.removeItem('username')
        localStorage.removeItem('userId')
        localStorage.removeItem('name')
        setToken(null)
        setRole(null)
        setUsername(null)
        setUserId(null)
        setName(null)
    }

    return (
        <AuthCtx.Provider value={{ token, role, username, userId, name, login, logout, loading }}>
            {children}
        </AuthCtx.Provider>
    )
}

export function useAuth() {
    return useContext(AuthCtx)
}