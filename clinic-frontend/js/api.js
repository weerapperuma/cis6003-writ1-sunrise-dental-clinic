let detectedBase = null;

// Resolve API base URL dynamically
function getApiBase() {
    if (detectedBase !== null) return detectedBase;
    // If accessed through Tomcat on port 8080
    if (window.location.origin.includes(':8080')) {
        if (window.location.pathname.startsWith('/clinic-service')) {
            return '/clinic-service';
        }
        return '';
    }
    // Target active Tomcat server at root http://localhost:8080
    return 'http://localhost:8080';
}

async function api(path, options = {}) {
    const base = getApiBase();
    const cleanPath = path.startsWith('/') ? path : '/' + path;
    const url = path.startsWith('http') ? path : `${base}${cleanPath}`;
    try {
        let res = await fetch(url, {
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include', // Ensures session cookie (JSESSIONID) is stored and sent
            ...options
        });

        // Auto-fallback: if 404, check alternate base URL (/clinic-service vs root)
        if (res.status === 404 && !path.startsWith('http')) {
            const altBase = (base === 'http://localhost:8080') ? 'http://localhost:8080/clinic-service' : 'http://localhost:8080';
            const altUrl = `${altBase}${cleanPath}`;
            try {
                const altRes = await fetch(altUrl, {
                    headers: { 'Content-Type': 'application/json' },
                    credentials: 'include',
                    ...options
                });
                if (altRes.status !== 404) {
                    detectedBase = altBase;
                    res = altRes;
                }
            } catch (ignore) {}
        }

        let data = null;
        try { data = await res.json(); } catch (e) {}
        return { status: res.status, data };
    } catch (networkError) {
        console.error('API call failed:', networkError);
        return { status: 0, data: { success: false, message: 'Cannot connect to backend server at ' + base } };
    }
}

async function requireLogin() {
    const { status, data } = await api('/api/auth/me');
    if (status === 401 || !data || !data.success) {
        location.replace('login.html');
        return null;
    }
    return data;
}

function showMsg(el, text, ok) {
    el.textContent = text;
    el.className = 'msg ' + (ok ? 'ok' : 'err');
    el.style.display = 'block';
}