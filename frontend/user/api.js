const VaultCoreApi = (() => {
  const baseUrl = 'http://localhost:8080';

  async function request(path, options = {}) {
    const token = sessionStorage.getItem('vbmsToken');
    const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
    if (token) headers.Authorization = `Bearer ${token}`;
    const response = await fetch(`${baseUrl}${path}`, { ...options, headers });
    if (!response.ok) {
      const body = await response.json().catch(() => ({}));
      throw new Error(body.message || `Request failed (${response.status})`);
    }
    return response.status === 204 ? null : response.json();
  }

  return {
    isAuthenticated: () => Boolean(sessionStorage.getItem('vbmsToken')),
    request
  };
})();
