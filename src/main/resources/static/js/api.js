/**
 * IntelliERP API Client
 */

const API_BASE = '/api';

const Api = {
    getToken() {
        return localStorage.getItem('intellierp_token');
    },

    setToken(token) {
        localStorage.setItem('intellierp_token', token);
    },

    removeToken() {
        localStorage.removeItem('intellierp_token');
        localStorage.removeItem('intellierp_user');
    },

    getUser() {
        const u = localStorage.getItem('intellierp_user');
        return u ? JSON.parse(u) : null;
    },

    setUser(user) {
        localStorage.setItem('intellierp_user', JSON.stringify(user));
    },

    async request(endpoint, options = {}) {
        const url = `${API_BASE}${endpoint}`;
        const headers = {
            'Content-Type': 'application/json',
            ...(options.headers || {})
        };

        const token = this.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        try {
            const response = await fetch(url, {
                ...options,
                headers
            });

            if (response.status === 401) {
                // Unauthorized - redirect to login
                this.removeToken();
                if (!window.location.pathname.includes('login.html')) {
                    window.location.href = '/login.html';
                }
                throw new Error('Session expired. Please login again.');
            }

            if (response.status === 204) {
                return null;
            }

            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('text/csv')) {
                return await response.text();
            }

            const data = await response.json();

            if (!response.ok) {
                const message = data.message || data.error || 'An error occurred';
                throw new Error(message);
            }

            return data;
        } catch (error) {
            console.error(`API Error on ${endpoint}:`, error);
            throw error;
        }
    },

    get(endpoint, params = {}) {
        const queryString = new URLSearchParams(
            Object.entries(params).filter(([_, v]) => v !== undefined && v !== null && v !== '')
        ).toString();
        const url = queryString ? `${endpoint}?${queryString}` : endpoint;
        return this.request(url, { method: 'GET' });
    },

    post(endpoint, body = {}) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(body)
        });
    },

    put(endpoint, body = {}) {
        return this.request(endpoint, {
            method: 'PUT',
            body: JSON.stringify(body)
        });
    },

    patch(endpoint, params = {}) {
        const queryString = new URLSearchParams(params).toString();
        const url = queryString ? `${endpoint}?${queryString}` : endpoint;
        return this.request(url, { method: 'PATCH' });
    },

    delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    }
};
