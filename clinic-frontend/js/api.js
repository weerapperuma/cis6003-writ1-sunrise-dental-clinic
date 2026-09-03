// Global API helper
function getBase() {
    if (window.location.pathname.startsWith('/clinic-service')) {
        return '/clinic-service';
    }
    if (window.location.origin.includes(':8080')) {
        return '';
    }
    // If running from Live Server (port 5500, etc.), direct to backend port 8080
    if (window.location.port && window.location.port !== '8080') {
        return 'http://localhost:8080';
    }
    return '';
}

const API_ROOT = getBase();

async function api(path, options = {}) {
    const cleanPath = path.startsWith('/') ? path : '/' + path;
    const url = path.startsWith('http') ? path : `${API_ROOT}${cleanPath}`;

    const defaultHeaders = { 'Content-Type': 'application/json' };
    const opts = {
        headers: { ...defaultHeaders, ...(options.headers || {}) },
        credentials: 'include', // sends session cookie (JSESSIONID)
        ...options
    };

    try {
        let res = await fetch(url, opts);

        // Fallback for context path /clinic-service if running directly under /clinic-service
        if (res.status === 404 && !path.startsWith('http')) {
            const altUrl = (API_ROOT === '' || API_ROOT === 'http://localhost:8080')
                ? `http://localhost:8080/clinic-service${cleanPath}`
                : cleanPath;
            try {
                const altRes = await fetch(altUrl, opts);
                if (altRes.status !== 404) {
                    res = altRes;
                }
            } catch (e) {}
        }

        let data = null;
        try {
            data = await res.json();
        } catch (e) {}
        return { status: res.status, data };
    } catch (err) {
        console.error('API call failed for ' + url, err);
        return { status: 0, data: { success: false, message: 'Server connection error.' } };
    }
}

// Global Auth Check for Every Page
async function initAuth(navName) {
    const { status, data } = await api('/api/auth/me');
    if (status === 401 || !data || !data.success) {
        location.replace('login.html');
        return null;
    }

    const userEl = document.getElementById('headerUsername');
    const roleEl = document.getElementById('headerRole');
    if (userEl) userEl.textContent = data.username || 'Staff';
    if (roleEl) roleEl.textContent = (data.role || 'USER').toUpperCase();

    if (navName) {
        const navLink = document.getElementById('nav-' + navName);
        if (navLink) navLink.classList.add('active');
    }

    return data;
}

// Global Logout Handler
async function handleLogout() {
    try {
        await api('/api/auth/logout', { method: 'POST' });
    } catch (e) {}
    location.replace('login.html');
}

// Utility message display
function showMsg(el, text, isSuccess) {
    if (!el) return;
    el.innerHTML = text;
    el.className = 'msg ' + (isSuccess ? 'ok' : 'err');
    el.style.display = 'block';
}

function hideMsg(el) {
    if (el) el.style.display = 'none';
}