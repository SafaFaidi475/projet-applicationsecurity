import { LitElement, html, css } from 'lit';
import { AuthService } from './auth-service.js';

export class SecureTeamApp extends LitElement {
  static properties = {
    isAuthenticated: { type: Boolean },
    remainingTime: { type: String },
    projects: { type: Array },
    activeView: { type: String }
  };

  constructor() {
    super();
    this.authService = new AuthService();
    this.isAuthenticated = false;
    this.remainingTime = "1h 00m";
    this.projects = ["Project Alpha", "Project Beta"];
    this.activeView = "dashboard";
  }

  static styles = css`
    :host {
      display: block;
      min-height: 100vh;
      font-family: 'Outfit', sans-serif;
      background: radial-gradient(circle at top left, #1a1c2c, #4a192c);
      color: #e0e0e0;
    }
    header {
      background: rgba(255, 255, 255, 0.05);
      backdrop-filter: blur(10px);
      padding: 1.5rem 2rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    }
    .brand {
      font-size: 1.5rem;
      font-weight: 800;
      background: linear-gradient(90deg, #ff4d4d, #f9cb28);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
    main {
      padding: 3rem;
      max-width: 1200px;
      margin: 0 auto;
    }
    .card {
      background: rgba(255, 255, 255, 0.03);
      border-radius: 16px;
      padding: 2rem;
      border: 1px solid rgba(255, 255, 255, 0.05);
      box-shadow: 0 10px 30px rgba(0,0,0,0.3);
      margin-bottom: 2rem;
    }
    .status-badge {
      padding: 0.5rem 1rem;
      border-radius: 20px;
      font-size: 0.8rem;
      background: rgba(76, 175, 80, 0.2);
      color: #81c784;
      border: 1px solid rgba(76, 175, 80, 0.3);
    }
    .timer {
      font-size: 2.5rem;
      font-weight: 700;
      color: #f9cb28;
      margin: 1rem 0;
    }
    .login-container {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 80vh;
    }
    .login-card {
      width: 100%;
      max-width: 450px;
    }
    input, select, textarea {
      width: 100%;
      padding: 1rem;
      margin: 1rem 0;
      background: rgba(255, 255, 255, 0.05);
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 12px;
      color: white;
      box-sizing: border-box;
    }
    button {
      padding: 1rem 2rem;
      border-radius: 12px;
      border: none;
      cursor: pointer;
      font-weight: 700;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }
    .btn-primary {
      background: linear-gradient(135deg, #6366f1, #a855f7);
      color: white;
      width: 100%;
    }
    .btn-primary:hover {
      transform: translateY(-2px);
      box-shadow: 0 10px 20px rgba(99, 102, 241, 0.4);
    }
    .btn-ghost {
      background: transparent;
      color: #94a3b8;
      border: 1px solid rgba(255, 255, 255, 0.1);
    }
    .btn-ghost:hover {
      background: rgba(255, 255, 255, 0.05);
    }
    .grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 2rem;
    }
    .tag {
      background: rgba(255, 255, 255, 0.1);
      padding: 0.3rem 0.8rem;
      border-radius: 8px;
      margin-right: 0.5rem;
      font-size: 0.9rem;
    }
  `;

  async firstUpdated() {
    this.isAuthenticated = await this.authService.checkAuth();
  }

  render() {
    if (!this.isAuthenticated) {
      return html`
            <div class="login-container">
                <div class="card login-card">
                    <div class="brand" style="margin-bottom: 2rem; font-size: 2rem;">SecureTeam Access</div>
                    <p style="color: #94a3b8; margin-bottom: 2rem;">Temporary & Context-Aware IAM System</p>
                    <input type="text" placeholder="Username" id="user" value="admin">
                    <input type="password" placeholder="Password" id="pass" value="password">
                    <button class="btn-primary" @click="${this._login}">Establish Session (OAuth 2.1)</button>
                </div>
            </div>
            `;
    }

    return html`
      <header>
        <div class="brand">SecureTeam Access</div>
        <div style="display: flex; gap: 1rem; align-items: center;">
            <button class="btn-ghost" @click="${() => this.activeView = 'dashboard'}">Dashboard</button>
            <button class="btn-ghost" @click="${() => this.activeView = 'request'}">Request Access</button>
            <button class="btn-ghost" style="color: #ff4d4d;" @click="${this._logout}">Logout</button>
        </div>
      </header>
      
      <main>
        ${this.activeView === 'dashboard' ? this._renderDashboard() : this._renderRequestForm()}
      </main>
    `;
  }

  _renderDashboard() {
    return html`
            <div class="grid">
                <div class="card">
                    <span class="status-badge">Active Mission</span>
                    <h3>Current Session Expiration</h3>
                    <div class="timer">${this.remainingTime}</div>
                    <p style="font-size: 0.9rem; color: #94a3b8;">Manual revocation will trigger at end of mission.</p>
                </div>

                <div class="card">
                    <h3>Authorized Project Contexts</h3>
                    <div style="margin-top: 1.5rem;">
                        ${this.projects.map(p => html`<span class="tag">${p}</span>`)}
                    </div>
                    <p style="margin-top: 2rem; font-size: 0.9rem; color: #94a3b8;">
                        Your access is strictly limited to these contexts as per your temporary contract.
                    </p>
                </div>
            </div>

            <div class="card" style="margin-top: 2rem;">
                <h3>Real-Time Security Context</h3>
                <div style="display: flex; gap: 3rem; margin-top: 1.5rem;">
                    <div>
                        <div style="color: #94a3b8; font-size: 0.8rem; margin-bottom: 0.3rem;">Device ID</div>
                        <div style="font-family: monospace;">ST-WKSTN-01-SEC</div>
                    </div>
                    <div>
                        <div style="color: #94a3b8; font-size: 0.8rem; margin-bottom: 0.3rem;">Access Window</div>
                        <div>09:00 - 18:00 (UTC+1)</div>
                    </div>
                    <div>
                        <div style="color: #94a3b8; font-size: 0.8rem; margin-bottom: 0.3rem;">ABAC Status</div>
                        <div style="color: #81c784;">Verified</div>
                    </div>
                </div>
            </div>
        `;
  }

  _renderRequestForm() {
    return html`
            <div class="card" style="max-width: 600px; margin: 0 auto;">
                <span class="status-badge" style="background: rgba(255, 152, 0, 0.2); color: #ffb74d; border-color: rgba(255, 152, 0, 0.3);">
                    Just-In-Time Access Request
                </span>
                <h2 style="margin-top: 1.5rem;">Request Temporary Access</h2>
                <p style="color: #94a3b8; margin-bottom: 2rem;">Sensitive resources require manager approval and are limited in time.</p>
                
                <label>Target Project</label>
                <select>
                    <option>Project Gamma - Internal API</option>
                    <option>Infrastructure - Production Logs</option>
                    <option>Database - Sensitive PII</option>
                </select>

                <label>Requested Duration</label>
                <select>
                    <option>2 Hours (Standard task)</option>
                    <option>4 Hours (Extended debug)</option>
                    <option>1 Day (Critical maintenance)</option>
                </select>

                <label>Business Justification</label>
                <textarea rows="4" placeholder="Describe why this temporary access is required..."></textarea>

                <button class="btn-primary" @click="${() => { alert('Access Request Submitted. Waiting for Manager Approval.'); this.activeView = 'dashboard'; }}">
                    Submit JIT Request
                </button>
            </div>
        `;
  }

  async _login() {
    const user = this.shadowRoot.getElementById('user').value;
    const pass = this.shadowRoot.getElementById('pass').value;
    try {
      await this.authService.login(user, pass);
      this.isAuthenticated = true;
    } catch (e) {
      alert('Access establishment failed: ' + e.message);
    }
  }

  async _logout() {
    await this.authService.logout();
    this.isAuthenticated = false;
    this.activeView = "dashboard";
  }
}

customElements.define('secureteam-app', SecureTeamApp);
