export class AuthService {

    constructor() {
        this.token = localStorage.getItem('secureteam_token');
        // NOTE: In production, use encrypted IndexedDB or opaque HTTP-only cookies if possible.
        // localStorage is used here for zero-dependency simplicity in this generated file.
    }

    async checkAuth() {
        return !!this.token;
    }

    async login(username, password) {
        // 1. Generate PKCE Verifier & Challenge
        const verifier = this.generateCodeVerifier();
        const challenge = await this.generateCodeChallenge(verifier);

        // 2. Mock API Call to Backend (SecureTeam IAM)
        // In real flow, this redirects to /authorize
        console.log(`SecureTeam Auth: Initiating OAuth 2.1 PKCE with Challenge: ${challenge}`);

        // Simulate successful token exchange
        if (username === 'admin' && password === 'password') {
            this.token = "mock_secureteam_paseto_v4_token";
            localStorage.setItem('secureteam_token', this.token);
            return true;
        } else {
            throw new Error("Invalid credentials");
        }
    }

    async logout() {
        this.token = null;
        localStorage.removeItem('sentinel_token');
    }

    generateCodeVerifier() {
        const array = new Uint32Array(56 / 2);
        window.crypto.getRandomValues(array);
        return Array.from(array, dec => ('0' + dec.toString(16)).substr(-2)).join('');
    }

    async generateCodeChallenge(verifier) {
        const encoder = new TextEncoder();
        const data = encoder.encode(verifier);
        const digest = await window.crypto.subtle.digest("SHA-256", data);

        return btoa(String.fromCharCode(...new Uint8Array(digest)))
            .replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
    }
}
