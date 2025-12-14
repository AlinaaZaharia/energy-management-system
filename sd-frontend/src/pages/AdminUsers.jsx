import React, { useEffect, useState } from 'react'
import { api } from '../api'

export default function AdminUsers() {
    const [users, setUsers] = useState([])
    const [form, setForm] = useState({
        username: '',
        email: '',
        role: 'CLIENT',
        name: '',
        city: '',
        dateOfBirth: '',
        password: ''
    })
    const [editingUser, setEditingUser] = useState(null)
    const [loading, setLoading] = useState(false)
    const [err, setErr] = useState('')

    async function load() {
        setLoading(true)
        try {
            const base = await api.listUsers()

            const enriched = await Promise.all(base.map(async u => {
                try {
                    const { role } = await api.getRoleByUserId(u.id)
                    return { ...u, role }
                } catch {
                    return { ...u, role: 'CLIENT' }
                }
            }))

            setUsers(enriched)
        } catch (ex) {
            setErr(ex.message || String(ex))
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => { load() }, [])

    const onChange = (field) => (e) => setForm(v => ({ ...v, [field]: e.target.value }))

    function startEdit(user) {
        setEditingUser(user.id)
        setForm({
            username: user.username,
            email: user.email,
            role: user.role || 'CLIENT',
            name: user.name || '',
            city: user.city || '',
            dateOfBirth: user.dateOfBirth || '',
            password: ''
        })
        setErr('')
        window.scrollTo({ top: 0, behavior: 'smooth' })
    }

    function cancelEdit() {
        setEditingUser(null)
        setForm({
            username: '',
            email: '',
            role: 'CLIENT',
            name: '',
            city: '',
            dateOfBirth: '',
            password: ''
        })
        setErr('')
    }

    async function create() {
        setErr('')

        if (!form.username || !form.email) {
            setErr('Username and email are required')
            return
        }
        if (!form.password) {
            setErr('Password is required')
            return
        }

        let createdUserId = null
        try {
            const userDto = {
                username: form.username,
                email: form.email,
                name: form.name || form.username,
                city: form.city || 'N/A',
                dateOfBirth: form.dateOfBirth || '2000-01-01'
            }

            const response = await fetch('/api/users', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('token')}`
                },
                body: JSON.stringify(userDto)
            })

            if (!response.ok) {
                const errorText = await response.text()
                throw new Error(`Failed to create user: ${errorText}`)
            }

            const location = response.headers.get('Location')

            if (!location) {
                const responseBody = await response.text()
                if (responseBody) {
                    const data = JSON.parse(responseBody)
                    createdUserId = data.id
                } else {
                    throw new Error('No Location header or response body with user ID')
                }
            } else {
                createdUserId = location.split('/').pop()
            }

            await api.createCredential({
                userId: createdUserId,
                username: form.username,
                email: form.email,
                password: form.password,
                role: form.role
            })

            setForm({
                username: '',
                email: '',
                role: 'CLIENT',
                name: '',
                city: '',
                dateOfBirth: '',
                password: ''
            })

            alert('User created successfully!')
            load()

        } catch (ex) {
            console.error('Create user error:', ex)

            if (createdUserId) {
                try {
                    await api.deleteUser(createdUserId)
                    console.log('Rolled back user creation')
                } catch (rollbackErr) {
                    console.error('Rollback failed:', rollbackErr)
                }
            }
            setErr(ex.message || String(ex))
        }
    }

    async function update() {
        setErr('')

        if (!form.username || !form.email) {
            setErr('Username and email are required')
            return
        }

        try {
            const userDto = {
                username: form.username,
                email: form.email,
                name: form.name || form.username,
                city: form.city || 'N/A',
                dateOfBirth: form.dateOfBirth || '2000-01-01'
            }
            await api.updateUser(editingUser, userDto)

            const credentialUpdate = {
                userId: editingUser,
                username: form.username,
                email: form.email,
                role: form.role
            }

            if (form.password) {
                credentialUpdate.password = form.password
            }

            await api.updateCredential(editingUser, credentialUpdate)

            alert('User updated successfully!')
            cancelEdit()
            load()

        } catch (ex) {
            console.error('Update user error:', ex)
            setErr(ex.message || String(ex))
        }
    }

    async function changeRole(u, newRole) {
        setErr('')
        try {
            await api.updateRole(u.id, newRole)
            load()
        } catch (ex) {
            setErr(ex.message || String(ex))
        }
    }

    async function remove(id) {
        setErr('')
        if (!window.confirm('Delete this user?')) return
        try {
            await api.deleteUser(id)

            if (editingUser === id) {
                cancelEdit()
            }

            load()
        } catch (ex) {
            setErr(ex.message || String(ex))
        }
    }

    return (
        <div className="container">
            <h2>Users Management</h2>
            {err && <div style={{ color: '#ff8e8e', whiteSpace: 'pre-wrap', marginBottom: 12 }}>{err}</div>}

            <div className="card">
                <h3>{editingUser ? 'Edit User' : 'Add New User'}</h3>
                {editingUser && (
                    <div style={{
                        background: '#2a3a6b',
                        padding: 8,
                        borderRadius: 6,
                        marginBottom: 12,
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center'
                    }}>
                        <span>Editing user: <strong>{form.username}</strong></span>
                        <button className="btn" onClick={cancelEdit} style={{ background: '#3d2a2a' }}>
                            Cancel Edit
                        </button>
                    </div>
                )}
                <div className="grid" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 12 }}>
                    <div>
                        <label>Username *</label>
                        <input
                            className="input"
                            value={form.username}
                            onChange={onChange('username')}
                            placeholder="e.g. john_doe"
                        />
                        {editingUser && (
                            <small style={{ opacity: 0.6, fontSize: 12 }}>
                                ⚠️ Changing username will affect login credentials
                            </small>
                        )}
                    </div>
                    <div>
                        <label>Email *</label>
                        <input className="input" type="email" value={form.email} onChange={onChange('email')} placeholder="john@example.com" />
                    </div>
                    <div>
                        <label>Password {editingUser ? '(leave empty to keep current)' : '*'}</label>
                        <input
                            className="input"
                            type="password"
                            value={form.password}
                            onChange={onChange('password')}
                            placeholder={editingUser ? 'Leave empty to keep current' : 'Enter password'}
                        />
                    </div>
                    <div>
                        <label>Full Name</label>
                        <input className="input" value={form.name} onChange={onChange('name')} placeholder="John Doe" />
                    </div>
                    <div>
                        <label>City</label>
                        <input className="input" value={form.city} onChange={onChange('city')} placeholder="Cluj-Napoca" />
                    </div>
                    <div>
                        <label>Date of Birth</label>
                        <input className="input" type="date" value={form.dateOfBirth} onChange={onChange('dateOfBirth')} />
                    </div>
                    <div>
                        <label>Role</label>
                        <select className="input" value={form.role} onChange={onChange('role')}>
                            <option>CLIENT</option>
                            <option>ADMIN</option>
                        </select>
                    </div>
                </div>
                <div style={{ marginTop: 12, display: 'flex', gap: 12 }}>
                    {editingUser ? (
                        <>
                            <button className="btn" onClick={update}>Update User</button>
                            <button className="btn" onClick={cancelEdit} style={{ background: '#3d2a2a' }}>
                                Cancel
                            </button>
                        </>
                    ) : (
                        <button className="btn" onClick={create}>Add User</button>
                    )}
                </div>
            </div>

            <div className="card">
                <h3>Existing Users</h3>
                {loading ? (
                    <div>Loading users...</div>
                ) : (
                    <table className="table">
                        <thead>
                        <tr>
                            <th>Username</th>
                            <th>Email</th>
                            <th>Name</th>
                            <th>City</th>
                            <th>Role</th>
                            <th>Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        {users.length === 0 ? (
                            <tr><td colSpan="6">No users found</td></tr>
                        ) : users.map(u => (
                            <tr key={u.id} style={{
                                background: editingUser === u.id ? '#1a2847' : 'transparent'
                            }}>
                                <td>{u.username}</td>
                                <td>{u.email}</td>
                                <td>{u.name || '-'}</td>
                                <td>{u.city || '-'}</td>
                                <td>
                                    <select
                                        className="input"
                                        value={u.role || 'CLIENT'}
                                        onChange={e => changeRole(u, e.target.value)}
                                        disabled={editingUser === u.id}
                                    >
                                        <option>CLIENT</option>
                                        <option>ADMIN</option>
                                    </select>
                                </td>
                                <td>
                                    <div className="row">
                                        <button
                                            className="btn"
                                            onClick={() => startEdit(u)}
                                            disabled={editingUser && editingUser !== u.id}
                                        >
                                            Edit
                                        </button>
                                        <button
                                            className="btn"
                                            onClick={() => remove(u.id)}
                                            style={{ background: '#5a2a2a' }}
                                            disabled={editingUser === u.id}
                                        >
                                            Delete
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    )
}