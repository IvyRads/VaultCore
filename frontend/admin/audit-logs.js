(() => {
    const api = VaultCoreApi;
    const filters = document.getElementById('filters');
    const rows = document.getElementById('rows');
    const count = document.getElementById('count');
    const modal = document.getElementById('modal');
    const detail = document.getElementById('detail');
    let logs = [];

    const escapeHtml = value => String(value ?? '').replace(/[&<>"']/g, char => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    })[char]);
    const formatDate = value => value ? new Date(value).toLocaleString() : '—';
    const errorMessage = error => {
        const message = error.message || '';
        if (/401|unauthori[sz]ed|authentication|required.*token/i.test(message)) return 'Your session is not authenticated. Please sign in again.';
        if (/403|forbidden|access denied/i.test(message)) return 'You are not authorized to view audit logs.';
        return `Unable to load audit logs: ${message || 'Please try again.'}`;
    };

    const render = () => {
        count.textContent = `${logs.length} log record${logs.length === 1 ? '' : 's'} found`;
        rows.innerHTML = logs.length ? logs.map(log => `<tr class="hover:bg-cream/60">
            <td class="px-4 py-3 font-semibold text-vault">${escapeHtml(log.logId)}</td>
            <td class="px-4 py-3">${escapeHtml(log.transactionId ?? '—')}</td>
            <td class="px-4 py-3">${escapeHtml(log.performedBy ?? '—')}</td>
            <td class="px-4 py-3">${escapeHtml(log.action || '—')}</td>
            <td class="px-4 py-3">${escapeHtml(formatDate(log.logTime))}</td>
            <td class="px-4 py-3">${escapeHtml(log.remarks || '—')}</td>
            <td class="px-4 py-3"><button data-log-id="${escapeHtml(log.logId)}" class="font-semibold text-vault">View</button></td>
        </tr>`).join('') : '<tr><td colspan="7" class="p-8 text-center text-slate">No audit logs match the selected filters.</td></tr>';
    };

    const load = async () => {
        const params = new URLSearchParams();
        const performedBy = document.getElementById('performedBy').value.trim();
        const action = document.getElementById('actionFilter').value.trim();
        const from = document.getElementById('from').value;
        const to = document.getElementById('to').value;
        if (performedBy) params.set('performedBy', performedBy);
        if (action) params.set('action', action);
        if (from) params.set('from', from);
        if (to) params.set('to', to);

        rows.innerHTML = '<tr><td colspan="7" class="p-8 text-center text-slate">Loading audit logs…</td></tr>';
        count.textContent = 'Loading…';
        try {
            const query = params.toString();
            logs = await api.request(`/api/admin/audit-logs${query ? `?${query}` : ''}`);
            render();
        } catch (error) {
            logs = [];
            count.textContent = '';
            rows.innerHTML = `<tr><td colspan="7" class="p-8 text-center text-red-700">${escapeHtml(errorMessage(error))}</td></tr>`;
        }
    };

    filters.addEventListener('submit', event => {
        event.preventDefault();
        load();
    });
    document.getElementById('resetFilters').addEventListener('click', () => {
        filters.reset();
        load();
    });
    rows.addEventListener('click', event => {
        const button = event.target.closest('[data-log-id]');
        if (button) showDetails(button.dataset.logId);
    });
    const close = () => modal.classList.replace('flex', 'hidden');
    document.getElementById('closeDetails').addEventListener('click', close);
    modal.addEventListener('click', event => {
        if (event.target === modal) close();
    });

    async function showDetails(id) {
        modal.classList.replace('hidden', 'flex');
        detail.innerHTML = '<p class="text-sm text-slate">Loading audit log details…</p>';
        try {
            const log = await api.request(`/api/admin/audit-logs/${encodeURIComponent(id)}`);
            const fields = [
                ['Log ID', log.logId],
                ['Transaction ID', log.transactionId],
                ['Performed By ID', log.performedBy],
                ['Action', log.action],
                ['Timestamp', formatDate(log.logTime)],
                ['Remarks', log.remarks]
            ];
            detail.innerHTML = fields.map(([label, value]) => `<div><span class="block text-xs text-slate">${label}</span><strong>${escapeHtml(value ?? '—')}</strong></div>`).join('');
        } catch (error) {
            detail.innerHTML = `<p class="text-sm text-red-700">${escapeHtml(errorMessage(error))}</p>`;
        }
    }

    load();
})();
