import React from 'react'
import { Link, useLocation } from 'react-router-dom'
import { useAuth } from '../auth'

export default function NavBar() {
    const { role, username, name, logout, token } = useAuth()
    const location = useLocation()

    if (location.pathname === '/login' && !token) {
        return null
    }

    return (
        <header>
            <Link to="/" style={{ fontWeight: 700, fontSize: 20 }}>SD</Link>
            <nav>
                {role === 'ADMIN' && (
                    <>
                        <Link to="/admin/users">Users</Link>
                        <Link to="/admin/devices">Devices</Link>
                    </>
                )}
                {role === 'CLIENT' && <Link to="/client/devices">My Devices</Link>}
            </nav>
            <div style={{ marginLeft: 'auto' }}>
                {token ? (
                    <div className="row">
                        <span style={{ opacity: .8 }}>
                            Hello, <strong>{name || username}</strong> <span style={{ opacity: 0.6 }}>({role})</span>
                        </span>
                        <button className="btn" onClick={logout}>Logout</button>
                    </div>
                ) : (
                    <Link to="/login" className="btn">Login</Link>
                )}
            </div>
        </header>
    )
}