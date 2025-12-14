import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../auth'

export default function LoginPage() {
    const nav = useNavigate()
    const { login, loading } = useAuth()
    const [u, setU] = useState('')
    const [p, setP] = useState('')
    const [err, setErr] = useState('')

    async function onSubmit(e) {
        e.preventDefault()
        setErr('')
        try {
            const res = await login(u, p)
            if (res.role === 'ADMIN') nav('/admin/users')
            else nav('/client/devices')
        } catch (ex) {
            setErr('Login failed: ' + ex.message)
        }
    }

    return (
        <div className="container">
            <div className="card" style={{ maxWidth: 420, margin: '80px auto' }}>
                <h2 style={{ textAlign: 'center', marginBottom: 24 }}>Login</h2>
                <form onSubmit={onSubmit} className="grid">
                    <div>
                        <label>Username</label>
                        <input
                            className="input"
                            value={u}
                            onChange={e => setU(e.target.value)}
                            placeholder="Enter your username"
                            autoFocus
                        />
                    </div>
                    <div>
                        <label>Password</label>
                        <input
                            className="input"
                            type="password"
                            value={p}
                            onChange={e => setP(e.target.value)}
                            placeholder="Enter your password"
                        />
                    </div>
                    {err && <div style={{ color: '#ff8e8e', padding: 8, textAlign: 'center' }}>{err}</div>}
                    <button className="btn" disabled={loading || !u || !p}>
                        {loading ? 'Signing in...' : 'Sign in'}
                    </button>
                </form>
            </div>
        </div>
    )
}