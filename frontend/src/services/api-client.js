const API_BASE_URL = 'http://localhost:8080/secureteam-backend/api';

class ApiClient {
    constructor() {
        this.token = localStorage.getItem('secureteam_token');
    }

    setToken(token) {
        this.token = token;
        localStorage.setItem('secureteam_token', token);
    }

    async request(endpoint, options = {}) {
        const url = `${API_BASE_URL}${endpoint}`;
        const headers = {
            'Content-Type': 'application/json',
            'X-Device-ID': 'browser-001', // Should be dynamic
            ...options.headers,
        };

        if (this.token) {
            headers['Authorization'] = `Bearer ${this.token}`;
        }

        const response = await fetch(url, {
            ...options,
            headers,
        });

        if (response.status === 401) {
            // Handle unauthorized (Logout or refresh)
            console.error('Session expired');
        }

        return response.json();
    }

    // Auth
    login(username, code) {
        return this.request('/auth/mfa/verify', {
            method: 'POST',
            body: JSON.stringify({ username, code })
        });
    }

    register(user) {
        return this.request('/auth/register', {
            method: 'POST',
            body: JSON.stringify(user)
        });
    }

    // Projects
    getProjects() {
        return this.request('/projects');
    }

    // Steganography
    hideData(coverImage, message) {
        return this.request('/stego/hide', {
            method: 'POST',
            body: JSON.stringify({ coverImage, message })
        });
    }

    extractData(stegoImage) {
        return this.request('/stego/extract', {
            method: 'POST',
            body: JSON.stringify({ coverImage: stegoImage })
        });
    }

    // Audit
    getAuditLogs() {
        return this.request('/audit');
    }
}

export const apiClient = new ApiClient();
