function authHeaders() {
    const token = localStorage.getItem('token')
    const h = { 'Content-Type': 'application/json' }
    if (token) h['Authorization'] = `Bearer ${token}`
    return h
}

async function http(method, url, body) {
    const res = await fetch(url, {
        method,
        headers: authHeaders(),
        body: body ? JSON.stringify(body) : undefined,
    })
    if (!res.ok) {
        const txt = await res.text().catch(() => '')
        throw new Error(`${res.status} ${res.statusText} ${txt}`)
    }

    if (res.status === 204 || res.status === 201) {
        return null
    }

    const ct = res.headers.get('content-type') || ''
    return ct.includes('application/json') ? res.json() : res.text()
}

export const api = {
    login: (username, password) => http('POST', '/api/auth/login', { username, password }),
    register: (username, password, email, role = 'CLIENT') =>
        http('POST', '/api/auth/register', { username, password, email, role }),

    getUser: (id) => http('GET', `/api/users/${id}`),
    listUsers: () => http('GET', '/api/users/details'),
    createUser: (dto) => http('POST', '/api/users', dto),
    updateUser: (id, dto) => http('PUT', `/api/users/${id}`, dto),
    deleteUser: (id) => http('DELETE', `/api/users/${id}`),

    listDevices: () => http('GET', '/api/devices'),
    createDevice: (dto) => http('POST', '/api/devices', dto),
    getDevice: (id) => http('GET', `/api/devices/${id}`),
    updateDevice: (id, dto) => http('PUT', `/api/devices/${id}`, dto),
    deleteDevice: (id) => http('DELETE', `/api/devices/${id}`),
    assignDevice: (deviceId, userId) => http('PUT', `/api/devices/${deviceId}/assign/${userId}`),
    unassignDevice: (deviceId) => http('DELETE', `/api/devices/${deviceId}/assign`),
    myDevices: (userId) => http('GET', `/api/devices/user/${userId}`),

    createCredential: (dto) => http('POST', '/api/auth/credentials', dto),
    getRoleByUserId: (userId) => http('GET', `/api/auth/credentials/role?userId=${userId}`),
    updateRole: (userId, role) => http('PUT', '/api/auth/credentials/role', { userId, role }),
    updateCredential: (userId, dto) => http('PUT', `/api/auth/credentials/${userId}`, dto),

    getDailyConsumption: (deviceId, date) => http('GET', `/api/monitoring/${deviceId}?date=${date}`),
}