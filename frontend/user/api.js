const VaultCoreApi = (() => {
  const baseUrl = 'http://localhost:8080';
  const tokenKey = 'vbmsToken';
  const userKey = 'vbmsUser';
  const loginUrl = new URL('../login.html', document.currentScript.src).href;

  function getToken() {
    return sessionStorage.getItem(tokenKey);
  }

  function getUser() {
    const storedUser = sessionStorage.getItem(userKey);
    if (!storedUser) return null;

    try {
      return JSON.parse(storedUser);
    } catch {
      return null;
    }
  }

  function setSession(token, user) {
    sessionStorage.setItem(tokenKey, token);
    sessionStorage.setItem(userKey, JSON.stringify(user));
  }

  function logout() {
    sessionStorage.removeItem(tokenKey);
    sessionStorage.removeItem(userKey);
    window.location.assign(loginUrl);
  }

  function requireAuthentication() {
    if (getToken()) return true;
    window.location.replace(loginUrl);
    return false;
  }

  async function request(path, options = {}) {
    const headers = new Headers(options.headers || {});
    const hasBody = options.body !== undefined && options.body !== null;
    const isFormData = hasBody && options.body instanceof FormData;

    if (hasBody && !isFormData && !headers.has('Content-Type')) {
      headers.set('Content-Type', 'application/json');
    }

    const token = getToken();
    if (token) headers.set('Authorization', `Bearer ${token}`);

    const response = await fetch(`${baseUrl}${path}`, { ...options, headers });
    const responseText = await response.text();
    let body = null;

    if (responseText) {
      try {
        body = JSON.parse(responseText);
      } catch {
        body = responseText;
      }
    }

    if (!response.ok) {
      const message = body && typeof body === 'object'
        ? body.message || body.error
        : body;
      throw new Error(message || `Request failed (${response.status})`);
    }

    return body;
  }

  document.addEventListener('click', event => {
    if (!(event.target instanceof Element)) return;
    const logoutLink = event.target.closest('a');
    if (!logoutLink || !/\blogout\b/i.test(logoutLink.textContent)) return;
    event.preventDefault();
    logout();
  });

  return {
    getToken,
    getUser,
    setSession,
    isAuthenticated: () => Boolean(getToken()),
    hasRole: role => getUser()?.role === role,
    logout,
    requireAuthentication,
    request
  };
})();
