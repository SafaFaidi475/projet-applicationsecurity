import { LitElement, html, css } from 'lit';
import { AuthService } from './auth-service.js';

export class SecureTeamApp extends LitElement {
  static properties = {
    isAuthenticated: { type: Boolean },
    loginStep: { type: Number }, // 1: Credentials, 2: MFA
    userRole: { type: String }, // 'external_collaborator' or 'security_admin'
    remainingTime: { type: String },
    projects: { type: Array },
    activeView: { type: String },
    qrImage: { type: String },
    pendingUser: { type: String },
    backendStatus: { type: String } // 'checking', 'online', 'offline'
  };

  constructor() {
    super();
    this.authService = new AuthService();
    this.isAuthenticated = false;
    this.loginStep = 1;
    this.userRole = 'external_collaborator';
    this.remainingTime = "1h 45m";
    this.projects = ["Project Alpha", "Project Beta"];
    this.activeView = "dashboard";
    this.qrImage = "";
    this.pendingUser = "";
    this.backendStatus = "checking";
  }

  static styles = css`
    :host {
      display: block;
      min-height: 100vh;
      font-family: 'Outfit', sans-serif;
      background: radial-gradient(circle at top left, #0f172a, #1e1b4b);
      color: #e2e8f0;
    }
    header {
      background: rgba(15, 23, 42, 0.8);
      backdrop-filter: blur(12px);
      padding: 1rem 2rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
      position: sticky;
      top: 0;
      z-index: 100;
    }
    .brand {
      font-size: 1.4rem;
      font-weight: 800;
      letter-spacing: -0.5px;
      background: linear-gradient(90deg, #38bdf8, #818cf8);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
    main {
      padding: 2rem;
      max-width: 1200px;
      margin: 0 auto;
    }
    .card {
      background: rgba(30, 41, 59, 0.5);
      border-radius: 20px;
      padding: 2rem;
      border: 1px solid rgba(255, 255, 255, 0.08);
      backdrop-filter: blur(8px);
      transition: all 0.3s ease;
    }
    .card:hover {
      border-color: rgba(56, 189, 248, 0.3);
      background: rgba(30, 41, 59, 0.7);
    }
    .mfa-box {
        text-align: center;
        padding: 1rem;
    }
    .qr-container {
        margin: 1rem auto;
        padding: 1.5rem;
        background: white;
        border-radius: 15px;
        display: inline-block;
        box-shadow: 0 10px 30px rgba(0,0,0,0.5);
    }
    .qr-container img {
        display: block;
        width: 180px;
        height: 180px;
    }
    .otp-input {
        letter-spacing: 1rem;
        font-size: 2rem;
        text-align: center;
        width: 250px;
        margin: 1.5rem auto;
        font-family: monospace;
        background: #0f172a;
        border: 2px solid #38bdf8;
        color: white;
        border-radius: 12px;
        padding: 1rem;
    }
    .status-badge {
      padding: 0.4rem 0.8rem;
      border-radius: 8px;
      font-size: 0.75rem;
      font-weight: 600;
      text-transform: uppercase;
    }
    .badge-admin { background: rgba(244, 63, 94, 0.2); color: #fb7185; }
    .badge-collab { background: rgba(56, 189, 248, 0.2); color: #7dd3fc; }

    .timer {
      font-size: 3rem;
      font-weight: 800;
      color: #38bdf8;
      text-shadow: 0 0 20px rgba(56, 189, 248, 0.3);
    }
    .login-container {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 90vh;
    }
    .login-card {
      width: 100%;
      max-width: 450px;
      padding: 3rem;
    }
    input, select, textarea {
      width: 100%;
      padding: 1rem;
      margin: 0.8rem 0;
      background: #0f172a;
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 12px;
      color: white;
      box-sizing: border-box;
      outline: none;
    }
    input:focus { border-color: #38bdf8; }
    
    button {
      padding: 1rem 1.5rem;
      border-radius: 12px;
      border: none;
      cursor: pointer;
      font-weight: 700;
      transition: all 0.2s ease;
    }
    .btn-primary {
      background: #38bdf8;
      color: #0f172a;
      width: 100%;
    }
    .btn-primary:hover { transform: translateY(-1px); box-shadow: 0 5px 15px rgba(56, 189, 248, 0.4); }
    
    .btn-ghost {
      background: transparent;
      color: #94a3b8;
      border: 1px solid rgba(255, 255, 255, 0.1);
    }
    .btn-ghost:hover { background: rgba(255, 255, 255, 0.05); }

    .grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
      gap: 1.5rem;
    }
    .tag {
      background: rgba(255, 255, 255, 0.05);
      padding: 0.4rem 0.8rem;
      border-radius: 6px;
      margin: 0.3rem;
      display: inline-block;
      font-size: 0.85rem;
    }
  `;

  async firstUpdated() {
    this.isAuthenticated = await this.authService.checkAuth();
    this._checkBackendHealth();
    setInterval(() => this._checkBackendHealth(), 5000);
  }

  async _checkBackendHealth() {
    try {
      const resp = await fetch('/secureteam-access/api/auth/health');
      if (resp.ok) {
        this.backendStatus = 'online';
      } else {
        this.backendStatus = 'offline';
      }
    } catch (e) {
      this.backendStatus = 'offline';
    }
  }

  render() {
    if (!this.isAuthenticated) {
      return html`
            <div class="login-container">
                <div class="card login-card">
                    <div class="brand" style="margin-bottom: 0.5rem; font-size: 1.8rem;">SecureTeam Access</div>
                    ${this.backendStatus === 'offline' ? html`<div style="padding: 0.5rem; background: rgba(244, 63, 94, 0.1); color: #fb7185; border-radius: 8px; font-size: 0.8rem; margin-bottom: 1.5rem; text-align: center; border: 1px solid rgba(244, 63, 94, 0.3);">⚠️ Security Engine Offline - Please wait for backend to start...</div>` : ''}
                    ${this.backendStatus === 'checking' ? html`<div style="padding: 0.5rem; background: rgba(56, 189, 248, 0.1); color: #38bdf8; border-radius: 8px; font-size: 0.8rem; margin-bottom: 1.5rem; text-align: center;">Checking Security Engine...</div>` : ''}
                    
                    ${this.loginStep === 1 ? this._renderLoginForm() : this._renderMFAForm()}
                </div>
            </div>
            `;
    }

    return html`
      <header>
        <div class="brand">SecureTeam Access</div>
        <div style="display: flex; gap: 0.8rem; align-items: center;">
            <span class="status-badge ${this.userRole === 'security_admin' ? 'badge-admin' : 'badge-collab'}">
                ${this.userRole === 'security_admin' ? 'Admin' : 'Consultant'} Mode
            </span>
            <button class="btn-ghost" @click="${() => this.activeView = 'dashboard'}">Dashboard</button>
            <button class="btn-ghost" @click="${() => this.activeView = 'request'}">Requests</button>
            <button class="btn-ghost" style="color: #fb7185;" @click="${this._logout}">Logout</button>
        </div>
      </header>
      
      <main>
        <h2 style="margin-bottom: 2rem;">Welcome, <span style="color: #38bdf8;">${this.userRole === 'security_admin' ? 'Administrator' : 'Freelance Partner'}</span></h2>
        ${this.activeView === 'dashboard' ? this._renderDashboard() : this._renderRequestForm()}
      </main>
    `;
  }

  _renderLoginForm() {
    return html`
            <label style="font-size: 0.8rem; color: #94a3b8;">User Identity</label>
            <input type="text" placeholder="Username" id="user" value="admin">
            <input type="password" placeholder="Password" id="pass" value="password">
            <button class="btn-primary" ?disabled="${this.backendStatus !== 'online'}" @click="${this._toMFAStep}">Next Step: Verify MFA</button>
            <p style="font-size: 0.75rem; color: #64748b; margin-top: 1rem; text-align: center;">
                Zero Trust Verification: Step 1 of 2
            </p>
        `;
  }

  _renderMFAForm() {
    return html`
            <div class="mfa-box">
                <h3 style="margin-bottom: 0.5rem;">Two-Factor Authentication</h3>
                <p style="color: #94a3b8; font-size: 0.85rem;">Scan this code with Google Authenticator to setup your device.</p>
                
                <div class="qr-container">
                    ${this.qrImage ? html`<img src="${this.qrImage}" alt="MFA QR Code">` : html`<div style="width:180px;height:180px;background:#eee;display:flex;align-items:center;justify-content:center;color:#666">Generating...</div>`}
                </div>

                <p style="color: #94a3b8; font-size: 0.8rem; margin: 1rem 0;">Enter the 6-digit code from your app</p>
                <input type="text" maxlength="6" class="otp-input" id="otp" placeholder="000 000" autofocus>
                
                <button class="btn-primary" @click="${this._login}">Establish Secure Session</button>
                <button class="btn-ghost" style="margin-top: 1rem; width: 100%;" @click="${() => this.loginStep = 1}">Back</button>
            </div>
        `;
  }

  _renderDashboard() {
    if (this.userRole === 'security_admin') {
      return this._renderAdminDashboard();
    }
    return this._renderCollabDashboard();
  }

  _renderCollabDashboard() {
    return html`
            <div class="grid">
                <div class="card">
                    <span class="status-badge badge-collab">Mission Pulse</span>
                    <h3 style="margin: 1rem 0;">JIT Session TTL</h3>
                    <div class="timer">${this.remainingTime}</div>
                    <p style="font-size: 0.85rem; color: #94a3b8; line-height: 1.5;">
                        Your access is temporary. Session will terminate and PASETO token will be revoked automatically at the end of the window.
                    </p>
                </div>

                <div class="card">
                    <h3 style="margin-bottom: 1.5rem;">Access Scope (ABAC)</h3>
                    <p style="font-size: 0.85rem; color: #94a3b8; margin-bottom: 1rem;">Authorized Projects:</p>
                    <div style="margin-bottom: 1.5rem;">
                        ${this.projects.map(p => html`<span class="tag">${p}</span>`)}
                    </div>
                    <div style="border-top: 1px solid rgba(255,255,255,0.05); padding-top: 1rem;">
                        <p style="font-size: 0.75rem; color: #64748b;">Device ID: <span style="color: #e2e8f0;">SEC-WS-442</span></p>
                        <p style="font-size: 0.75rem; color: #64748b;">IP Address: <span style="color: #e2e8f0;">10.0.5.12 (VPN)</span></p>
                    </div>
                </div>
            </div>

            <div class="card" style="margin-top: 1.5rem;">
                <h3 style="margin-bottom: 1rem;">Recent Activity</h3>
                <div style="font-size: 0.85rem;">
                    <div style="padding: 0.8rem 0; border-bottom: 1px solid rgba(255,255,255,0.05); display: flex; justify-content: space-between;">
                        <span>Decrypted sensitive key for Project Alpha</span>
                        <span style="color: #64748b;">2 mins ago</span>
                    </div>
                    <div style="padding: 0.8rem 0; display: flex; justify-content: space-between;">
                        <span>Session extended via JIT request #882</span>
                        <span style="color: #64748b;">1 hour ago</span>
                    </div>
                </div>
            </div>
        `;
  }

  _renderAdminDashboard() {
    return html`
            <div class="grid">
                <div class="card">
                    <h3 style="margin-bottom: 1rem;">System Health</h3>
                    <div style="display: flex; gap: 2rem;">
                        <div>
                            <div style="font-size: 2rem; font-weight: 700;">12</div>
                            <div style="font-size: 0.7rem; color: #94a3b8; text-transform: uppercase;">Active Sessions</div>
                        </div>
                        <div>
                            <div style="font-size: 2rem; font-weight: 700; color: #facc15;">3</div>
                            <div style="font-size: 0.7rem; color: #94a3b8; text-transform: uppercase;">Pending Approvals</div>
                        </div>
                    </div>
                </div>

                <div class="card">
                    <h3 style="margin-bottom: 1rem;">Threat Prevention</h3>
                    <div style="padding: 1rem; background: rgba(244, 63, 94, 0.1); border-radius: 12px; border: 1px solid rgba(244, 63, 94, 0.2);">
                        <p style="color: #fb7185; font-size: 0.85rem; font-weight: 600;">⚠️ Abnormal Activity Detected</p>
                        <p style="font-size: 0.75rem; color: #e2e8f0; margin-top: 0.3rem;">JTI Replay attempt from 192.168.1.100 blocked by Redis.</p>
                    </div>
                </div>
            </div>

            <div class="card" style="margin-top: 1.5rem;">
                <h3>Global Access Audit Logs</h3>
                <table style="width: 100%; border-collapse: collapse; margin-top: 1rem; font-size: 0.85rem; text-align: left;">
                    <thead>
                        <tr style="color: #64748b; border-bottom: 1px solid rgba(255,255,255,0.05);">
                            <th style="padding: 0.8rem;">Timestamp</th>
                            <th style="padding: 0.8rem;">Subject</th>
                            <th style="padding: 0.8rem;">Action</th>
                            <th style="padding: 0.8rem;">Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr style="border-bottom: 1px solid rgba(255,255,255,0.05);">
                            <td style="padding: 0.8rem;">14:12:05</td>
                            <td style="padding: 0.8rem;">user_alpha</td>
                            <td style="padding: 0.8rem;">JIT Request (2h)</td>
                            <td style="padding: 0.8rem; color: #fb7185;">PENDING</td>
                        </tr>
                        <tr>
                            <td style="padding: 0.8rem;">14:05:33</td>
                            <td style="padding: 0.8rem;">user_beta</td>
                            <td style="padding: 0.8rem;">Vault Decryption</td>
                            <td style="padding: 0.8rem; color: #4ade80;">ALLOWED</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        `;
  }

  _renderRequestForm() {
    return html`
            <div class="card" style="max-width: 600px; margin: 0 auto;">
                <span class="status-badge" style="background: rgba(245, 158, 11, 0.2); color: #fbbf24; border: 1px solid rgba(245, 158, 11, 0.3);">
                    Just-In-Time Escalation
                </span>
                <h3 style="margin-top: 1.5rem;">Request Temporary Elevated Privilege</h3>
                <p style="color: #94a3b8; margin-bottom: 2rem; font-size: 0.9rem;">
                    Requests are logged and require an explicit manager signature through the Approval Workflow API.
                </p>
                
                <label style="font-size: 0.8rem; color: #94a3b8;">Resource Zone</label>
                <select>
                    <option>Infrastucture - Cloud Console [R/W]</option>
                    <option>Database - Main Prod [READ ONLY]</option>
                    <option>CI/CD - Production Deployer</option>
                </select>

                <label style="font-size: 0.8rem; color: #94a3b8;">Window (TTL)</label>
                <select>
                    <option>1 Hour (Fast-track Fix)</option>
                    <option>4 Hours (Standard Maintenance)</option>
                    <option>8 Hours (Project Deadline)</option>
                </select>

                <label style="font-size: 0.8rem; color: #94a3b8;">Justification (Audit Log)</label>
                <textarea rows="3" placeholder="Explain the security requirement for this JIT request..."></textarea>

                <button class="btn-primary" style="margin-top: 1rem;" @click="${() => { alert('JIT Request Sent to Security Engine. Evaluation in progress...'); this.activeView = 'dashboard'; }}">
                    Send Request
                </button>
            </div>
        `;
  }

  async _toMFAStep() {
    const user = this.shadowRoot.getElementById('user').value;
    const pass = this.shadowRoot.getElementById('pass').value;

    if (user && pass) {
      this.pendingUser = user;
      this.userRole = user === 'admin' ? 'security_admin' : 'external_collaborator';

      // Fetch dynamic QR code from backend
      try {
        const resp = await fetch(`/secureteam-access/api/auth/mfa/setup?username=${user}`);
        const data = await resp.json();
        this.qrImage = data.qrImage;
        this.loginStep = 2;
      } catch (e) {
        console.error("MFA setup failed", e);
        alert("Connection to Security Engine failed. Check if backend is running.");
      }
    } else {
      alert('Invalid credentials');
    }
  }

  async _login() {
    const otp = this.shadowRoot.getElementById('otp').value;
    if (otp.length === 6) {
      try {
        // CALL REAL BACKEND MFA VERIFY
        const resp = await fetch(`/secureteam-access/api/auth/mfa/verify`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ username: this.pendingUser, code: otp })
        });

        if (resp.ok) {
          const data = await resp.json();
          localStorage.setItem('secureteam_token', data.token);
          this.isAuthenticated = true;
          this.loginStep = 1;
        } else {
          alert('MFA Verification failed. Incorrect code.');
        }
      } catch (e) {
        alert('MFA Verification error: ' + e.message);
      }
    } else {
      alert('Please enter a valid 6-digit code.');
    }
  }

  async _logout() {
    await this.authService.logout();
    this.isAuthenticated = false;
    this.loginStep = 1;
    this.activeView = "dashboard";
    this.qrImage = "";
  }
}

customElements.define('secureteam-app', SecureTeamApp);
