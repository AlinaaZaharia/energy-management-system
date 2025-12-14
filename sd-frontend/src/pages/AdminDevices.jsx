import React, { useEffect, useState } from 'react'
import { api } from '../api'

export default function AdminDevices() {
    const [devices, setDevices] = useState([])
    const [users, setUsers] = useState([])
    const [form, setForm] = useState({
        name: '',
        description: '',
        maxHourlyConsumption: ''
    })
    const [editingDevice, setEditingDevice] = useState(null)
    const [err, setErr] = useState('')
    const [loading, setLoading] = useState(false)

    async function load() {
        setLoading(true)
        try {
            const [d, u] = await Promise.all([api.listDevices(), api.listUsers()])
            setDevices(d)
            setUsers(u)
        } catch (ex) {
            setErr(ex.message)
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => { load() }, [])

    function onChange(field) {
        return (e) => setForm(v => ({ ...v, [field]: e.target.value }))
    }

    function startEdit(device) {
        setEditingDevice(device.id)
        setForm({
            name: device.name,
            description: device.description || '',
            maxHourlyConsumption: device.maxConsumption || ''
        })
        setErr('')
        window.scrollTo({ top: 0, behavior: 'smooth' })
    }

    function cancelEdit() {
        setEditingDevice(null)
        setForm({
            name: '',
            description: '',
            maxHourlyConsumption: ''
        })
        setErr('')
    }

    async function create() {
        setErr('')
        if (!form.name || !form.maxHourlyConsumption) {
            setErr('Name and max consumption are required')
            return
        }
        try {
            await api.createDevice({
                name: form.name,
                description: form.description || '',
                maxConsumption: parseFloat(form.maxHourlyConsumption)
            })
            setForm({ name: '', description: '', maxHourlyConsumption: '' })
            alert('Device created successfully!')
            load()
        } catch (ex) {
            setErr(ex.message)
        }
    }

    async function update() {
        setErr('')
        if (!form.name || !form.maxHourlyConsumption) {
            setErr('Name and max consumption are required')
            return
        }
        try {
            await api.updateDevice(editingDevice, {
                name: form.name,
                description: form.description || '',
                maxConsumption: parseFloat(form.maxHourlyConsumption)
            })

            alert('Device updated successfully!')
            cancelEdit()
            load()
        } catch (ex) {
            setErr(ex.message)
        }
    }

    async function remove(id) {
        setErr('')
        if (!window.confirm('Delete this device?')) return
        try {
            await api.deleteDevice(id)

            if (editingDevice === id) {
                cancelEdit()
            }

            load()
        } catch (ex) {
            setErr(ex.message)
        }
    }

    async function assign(deviceId, userId) {
        setErr('')
        if (!userId) return
        try {
            await api.assignDevice(deviceId, userId)
            load()
        } catch (ex) {
            setErr(ex.message)
        }
    }

    async function unassign(deviceId) {
        setErr('')
        try {
            await api.unassignDevice(deviceId)
            load()
        } catch (ex) {
            setErr(ex.message)
        }
    }

    function getUsernameById(userId) {
        const user = users.find(u => u.id === userId)
        return user ? user.username : '-'
    }

    return (
        <div className="container">
            <h2>Devices Management</h2>
            {err && <div style={{ color: '#ff8e8e', marginBottom: 12 }}>{err}</div>}

            <div className="card">
                <h3>{editingDevice ? 'Edit Device' : 'Add New Device'}</h3>
                {editingDevice && (
                    <div style={{
                        background: '#2a3a6b',
                        padding: 8,
                        borderRadius: 6,
                        marginBottom: 12,
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center'
                    }}>
                        <span>Editing device: <strong>{form.name}</strong></span>
                        <button className="btn" onClick={cancelEdit} style={{ background: '#3d2a2a' }}>
                            Cancel Edit
                        </button>
                    </div>
                )}
                <div className="grid" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 12 }}>
                    <div>
                        <label>Name *</label>
                        <input
                            className="input"
                            value={form.name}
                            onChange={onChange('name')}
                            placeholder="e.g. Smart Thermostat"
                        />
                    </div>
                    <div>
                        <label>Description</label>
                        <input
                            className="input"
                            value={form.description}
                            onChange={onChange('description')}
                            placeholder="Device description"
                        />
                    </div>
                    <div>
                        <label>Max Hourly Consumption (kW) *</label>
                        <input
                            className="input"
                            type="number"
                            step="0.1"
                            value={form.maxHourlyConsumption}
                            onChange={onChange('maxHourlyConsumption')}
                            placeholder="e.g. 2.5"
                        />
                    </div>
                </div>
                <div style={{ marginTop: 12, display: 'flex', gap: 12 }}>
                    {editingDevice ? (
                        <>
                            <button className="btn" onClick={update}>Update Device</button>
                            <button className="btn" onClick={cancelEdit} style={{ background: '#3d2a2a' }}>
                                Cancel
                            </button>
                        </>
                    ) : (
                        <button className="btn" onClick={create}>Add Device</button>
                    )}
                </div>
            </div>

            <div className="card">
                <h3>Existing Devices</h3>
                {loading ? (
                    <div>Loading devices...</div>
                ) : (
                    <table className="table">
                        <thead>
                        <tr>
                            <th>Name</th>
                            <th>Description</th>
                            <th>Max kW/h</th>
                            <th>Assigned To</th>
                            <th>Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        {devices.length === 0 ? (
                            <tr><td colSpan="5">No devices found</td></tr>
                        ) : devices.map(d => (
                            <tr key={d.id} style={{
                                background: editingDevice === d.id ? '#1a2847' : 'transparent'
                            }}>
                                <td>{d.name}</td>
                                <td>{d.description || '-'}</td>
                                <td>{d.maxConsumption || '-'}</td>
                                <td>{d.userId ? getUsernameById(d.userId) : '-'}</td>
                                <td>
                                    <div className="row">
                                        <button
                                            className="btn"
                                            onClick={() => startEdit(d)}
                                            disabled={editingDevice && editingDevice !== d.id}
                                        >
                                            Edit
                                        </button>
                                        <button
                                            className="btn"
                                            onClick={() => remove(d.id)}
                                            style={{ background: '#5a2a2a' }}
                                            disabled={editingDevice === d.id}
                                        >
                                            Delete
                                        </button>
                                        {d.userId ? (
                                            <button
                                                className="btn"
                                                onClick={() => unassign(d.id)}
                                                disabled={editingDevice}
                                            >
                                                Unassign
                                            </button>
                                        ) : (
                                            <select
                                                className="input"
                                                defaultValue=""
                                                onChange={e => assign(d.id, e.target.value)}
                                                disabled={editingDevice}
                                            >
                                                <option value="" disabled>Assign to…</option>
                                                {users.map(u => (
                                                    <option key={u.id} value={u.id}>{u.username}</option>
                                                ))}
                                            </select>
                                        )}
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