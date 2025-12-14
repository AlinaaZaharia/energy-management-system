import React, { useEffect, useState } from 'react'
import { api } from '../api'
import { useAuth } from '../auth'

export default function ClientDevices() {
    const { userId } = useAuth()
    const [devices, setDevices] = useState([])
    const [err, setErr] = useState('')
    const [loading, setLoading] = useState(false)

    const [selectedDeviceId, setSelectedDeviceId] = useState('')
    const [selectedDate, setSelectedDate] = useState('')
    const [consumptionData, setConsumptionData] = useState([]) // [{hour, consumption}]
    const [loadingConsumption, setLoadingConsumption] = useState(false)
    const [consumptionError, setConsumptionError] = useState('')

    async function load() {
        if (!userId) return
        setLoading(true)
        try {
            setDevices(await api.myDevices(userId))
        } catch (ex) {
            setErr(ex.message)
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => { load() }, [userId])

    async function loadConsumption() {
        setConsumptionError('')
        setConsumptionData([])

        if (!selectedDeviceId) {
            setConsumptionError('Please select a device.')
            return
        }
        if (!selectedDate) {
            setConsumptionError('Please select a date.')
            return
        }

        try {
            setLoadingConsumption(true)
            const data = await api.getDailyConsumption(selectedDeviceId, selectedDate)
            setConsumptionData(data || [])
        } catch (ex) {
            setConsumptionError(ex.message)
        } finally {
            setLoadingConsumption(false)
        }
    }

    const maxConsumption = consumptionData.length
        ? Math.max(...consumptionData.map(p => p.consumption || 0))
        : 0

    return (
        <div className="container">
            <h2>My Devices</h2>
            {err && <div style={{ color: '#ff8e8e', marginBottom: 12 }}>{err}</div>}

            {}
            {loading ? (
                <div className="card">Loading your devices...</div>
            ) : devices.length === 0 ? (
                <div className="card" style={{ textAlign: 'center', padding: 40 }}>
                    <p style={{ fontSize: 18, opacity: 0.7 }}>No devices assigned to you yet</p>
                    <p style={{ opacity: 0.5 }}>Contact your administrator to get devices assigned</p>
                </div>
            ) : (
                <>
                    <div className="card">
                        <h3>Your Devices ({devices.length})</h3>
                        <table className="table">
                            <thead>
                            <tr>
                                <th>Device Name</th>
                                <th>Description</th>
                                <th>Max Power (kW/h)</th>
                            </tr>
                            </thead>
                            <tbody>
                            {devices.map(d => (
                                <tr key={d.id}>
                                    <td style={{ fontWeight: 600 }}>{d.name}</td>
                                    <td style={{ opacity: 0.8 }}>{d.description || 'No description'}</td>
                                    <td>
                                        <span style={{
                                            background: '#2a3a6b',
                                            padding: '4px 12px',
                                            borderRadius: 6,
                                            fontWeight: 500
                                        }}>
                                            {d.maxConsumption || d.maxHourlyConsumption || 'N/A'} kW/h
                                        </span>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>

                    {}
                    <div className="card">
                        <h3>Daily Energy Consumption</h3>
                        <p style={{ opacity: 0.7, marginBottom: 12 }}>
                            Select one of your devices and a day to see the hourly consumption chart.
                        </p>

                        <div
                            className="grid"
                            style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 12 }}
                        >
                            <div>
                                <label>Device</label>
                                <select
                                    className="input"
                                    value={selectedDeviceId}
                                    onChange={e => setSelectedDeviceId(e.target.value)}
                                >
                                    <option value="">Select device...</option>
                                    {devices.map(d => (
                                        <option key={d.id} value={d.id}>{d.name}</option>
                                    ))}
                                </select>
                            </div>
                            <div>
                                <label>Date</label>
                                <input
                                    type="date"
                                    className="input"
                                    value={selectedDate}
                                    onChange={e => setSelectedDate(e.target.value)}
                                />
                            </div>
                            <div style={{ alignSelf: 'end' }}>
                                <button
                                    className="btn"
                                    type="button"
                                    onClick={loadConsumption}
                                    disabled={loadingConsumption}
                                >
                                    {loadingConsumption ? 'Loading chart...' : 'View Consumption'}
                                </button>
                            </div>
                        </div>

                        {consumptionError && (
                            <div style={{ color: '#ff8e8e', marginTop: 12 }}>{consumptionError}</div>
                        )}

                        {}
                        <div style={{ marginTop: 24 }}>
                            {loadingConsumption && (
                                <div>Loading consumption data...</div>
                            )}

                            {!loadingConsumption && consumptionData.length === 0 && !consumptionError && (
                                <div style={{ opacity: 0.7 }}>
                                    No data found for this day. Try another date or device.
                                </div>
                            )}

                            {!loadingConsumption && consumptionData.length > 0 && (
                                <div>
                                    <h4 style={{ marginBottom: 12 }}>
                                        {`Device: ${
                                            devices.find(d => d.id === selectedDeviceId)?.name || ''
                                        } – Date: ${selectedDate}`}
                                    </h4>

                                    <div className="chart-container">
                                        {}
                                        {Array.from({ length: 24 }).map((_, hour) => {
                                            const point = consumptionData.find(p => p.hour === hour)
                                            const value = point ? point.consumption : 0
                                            const height = maxConsumption > 0
                                                ? (value / maxConsumption) * 180
                                                : 0

                                            return (
                                                <div key={hour} className="chart-bar-wrapper">
                                                    <div
                                                        className="chart-bar"
                                                        style={{ height: `${height}px` }}
                                                        title={`${hour}:00 – ${value.toFixed(3)} kWh`}
                                                    />
                                                    <span className="chart-hour-label">
                                                        {hour}
                                                    </span>
                                                </div>
                                            )
                                        })}
                                    </div>

                                    <div style={{ marginTop: 8, fontSize: 12, opacity: 0.7 }}>
                                        Each bar represents the total energy consumption for that hour (kWh).
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>
                </>
            )}
        </div>
    )
}
