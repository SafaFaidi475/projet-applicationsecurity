import { LitElement, html, css } from 'lit';
import { Router } from '@vaadin/router';
import { AuthService } from './auth-service.js';

export class SentinelApp extends LitElement {
    static properties = {
        isAuthenticated: { type: Boolean }
    };

    constructor() {
        super();
        this.authService = new AuthService();
        this.isAuthenticated = false;
    }

    static styles = css`
    :host {
      display: block;
      height: 100vh;
      font-family: 'Inter', sans-serif;
      background-color: #f4f4f9;
      color: #333;
    }
    header {
      background: #2a2d34;
      color: white;
      padding: 1rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    main {
      padding: 2rem;
    }
    .login-box {
      background: white;
      padding: 2rem;
      border-radius: 8px;
      box-shadow: 0 4px 6px rgba(0,0,0,0.1);
      max-width: 400px;
      margin: 2rem auto;
    }
    input {
      width: 100%;
      padding: 0.8rem;
      margin: 0.5rem 0;
      border: 1px solid #ddd;
      border-radius: 4px;
    }
    button {
      width: 100%;
      padding: 0.8rem;
      background: #007bff;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-weight: bold;
    }
    button:hover {
      background: #0056b3;
    }
  `;

    async firstUpdated() {
        const router = new Router(this.shadowRoot.querySelector('main'));
        router.setRoutes([
            { path: '/', component: 'sentinel-dashboard' },
            { path: '/login', component: 'sentinel-login' }
        ]);

        // Check auth status
        this.isAuthenticated = await this.authService.checkAuth();
        if (!this.isAuthenticated) {
            // Simple redirect simulation
            // Router.go('/login');
        }
    }

    render() {
        if (!this.isAuthenticated) {
            return html`
            <header>
                <h1>SentinelKey</h1>
            </header>
            <div class="login-box">
                <h2>Secure Login</h2>
                <input type="text" placeholder="Username" id="user">
                <input type="password" placeholder="Password" id="pass">
                <button @click="${this._login}">Login (OAuth 2.1)</button>
            </div>
        `;
        }

        return html`
      <header>
        <h1>SentinelKey Dashboard</h1>
        <button @click="${this._logout}">Logout</button>
      </header>
      <main></main>
    `;
    }

    async _login() {
        const user = this.shadowRoot.getElementById('user').value;
        const pass = this.shadowRoot.getElementById('pass').value;
        try {
            await this.authService.login(user, pass);
            this.isAuthenticated = true;
        } catch (e) {
            alert('Login failed: ' + e.message);
        }
    }

    async _logout() {
        await this.authService.logout();
        this.isAuthenticated = false;
    }
}

customElements.define('sentinel-app', SentinelApp);
