(() => {
    const api = VaultCoreApi;
    const escapeHtml = value => String(value ?? '').replace(/[&<>"']/g, char => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    })[char]);
    const formatDate = value => value ? new Date(value).toLocaleString() : '—';
    const money = value => `₹${Number(value || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;
    const errorMessage = error => {
        const message = error.message || '';
        if (/401|unauthori[sz]ed|authentication|required.*token/i.test(message)) return 'Your session is not authenticated. Please sign in again.';
        if (/403|forbidden|access denied/i.test(message)) return 'You are not authorized to view admin transaction monitoring.';
        return `Unable to load transactions: ${message || 'Please try again.'}`;
    };
    const filterForm = document.getElementById('filterForm');
    const tableBody = document.getElementById('tableBody');
    const recordCount = document.getElementById('recordCount');
    const modal = document.getElementById('transactionModal');
    const modalBody = document.getElementById('modalBody');
    const modalId = document.getElementById('modalTransactionId');
    let transactions = [];

    const setState = (message, className = 'text-slate') => {
        tableBody.innerHTML = `<tr><td colspan="9" class="px-4 py-12 text-center ${className}">${escapeHtml(message)}</td></tr>`;
    };

    const render = () => {
        recordCount.textContent = `${transactions.length} transaction record${transactions.length === 1 ? '' : 's'} found`;
        document.getElementById('summaryCards').innerHTML = `<article class="rounded-xl border border-line bg-white p-4 shadow-sm"><p class="text-xs font-medium text-slate">Transactions in results</p><p class="mt-2 text-2xl font-bold text-vault">${transactions.length.toLocaleString()}</p></article>`;
        if (!transactions.length) {
            setState('No transactions match the selected filters.');
            return;
        }
        tableBody.innerHTML = transactions.map(transaction => `<tr class="border-t border-line hover:bg-cream/70">
            <td class="px-4 py-3 font-semibold text-vault">${escapeHtml(transaction.transactionId)}</td>
            <td class="px-4 py-3">${escapeHtml(transaction.accountNumber || '—')} <span class="block text-xs text-slate">ID ${escapeHtml(transaction.accountId)}</span></td>
            <td class="px-4 py-3">${escapeHtml(transaction.customerId)}<span class="block text-xs text-slate">${escapeHtml(transaction.customerNumber || '—')}</span></td>
            <td class="px-4 py-3">${escapeHtml(transaction.transactionType || '—')}</td>
            <td class="px-4 py-3 font-semibold">${money(transaction.amount)}</td>
            <td class="px-4 py-3">${escapeHtml(transaction.receiverAccount || '—')}</td>
            <td class="px-4 py-3">${escapeHtml(formatDate(transaction.transactionDate))}</td>
            <td class="px-4 py-3">${escapeHtml(transaction.description || '—')}</td>
            <td class="px-4 py-3"><button class="font-semibold text-vault hover:underline" data-transaction-id="${escapeHtml(transaction.transactionId)}">View</button></td>
        </tr>`).join('');
    };

    const loadTransactions = async () => {
        const params = new URLSearchParams();
        const type = document.getElementById('typeFilter').value.trim();
        const accountId = document.getElementById('accountIdFilter').value.trim();
        const fromDate = document.getElementById('fromDate').value;
        const toDate = document.getElementById('toDate').value;
        if (type) params.set('transactionType', type);
        if (accountId) params.set('accountId', accountId);
        if (fromDate) params.set('from', `${fromDate}T00:00:00`);
        if (toDate) params.set('to', `${toDate}T23:59:59`);
        tableBody.innerHTML = '<tr><td colspan="9" class="px-4 py-12 text-center text-slate">Loading transactions…</td></tr>';
        recordCount.textContent = 'Loading…';
        document.getElementById('summaryCards').innerHTML = '';
        try {
            const query = params.toString();
            transactions = await api.request(`/api/admin/transactions${query ? `?${query}` : ''}`);
            render();
        } catch (error) {
            transactions = [];
            recordCount.textContent = '';
            document.getElementById('summaryCards').innerHTML = '';
            setState(errorMessage(error), 'text-red-700');
        }
    };

    filterForm.addEventListener('submit', event => {
        event.preventDefault();
        loadTransactions();
    });
    document.getElementById('resetFilters').addEventListener('click', () => {
        filterForm.reset();
        loadTransactions();
    });
    tableBody.addEventListener('click', event => {
        const button = event.target.closest('[data-transaction-id]');
        if (button) showTransaction(button.dataset.transactionId);
    });
    document.getElementById('closeModal').addEventListener('click', () => modal.classList.replace('flex', 'hidden'));
    modal.addEventListener('click', event => {
        if (event.target === modal) modal.classList.replace('flex', 'hidden');
    });

    async function showTransaction(id) {
        modal.classList.replace('hidden', 'flex');
        modalId.textContent = `Transaction ${id}`;
        modalBody.innerHTML = '<p class="text-sm text-slate">Loading transaction details…</p>';
        try {
            const transaction = await api.request(`/api/admin/transactions/${encodeURIComponent(id)}`);
            const fields = [
                ['Transaction ID', transaction.transactionId],
                ['Account ID', transaction.accountId],
                ['Account Number', transaction.accountNumber],
                ['Customer ID', transaction.customerId],
                ['Customer Number', transaction.customerNumber],
                ['Transaction Type', transaction.transactionType],
                ['Amount', money(transaction.amount)],
                ['Receiver Account', transaction.receiverAccount],
                ['Timestamp', formatDate(transaction.transactionDate)],
                ['Description', transaction.description]
            ];
            modalId.textContent = `Transaction ${transaction.transactionId}`;
            document.getElementById('modalAccountLink').href = `account_details.html?account=${encodeURIComponent(transaction.accountId)}`;
            document.getElementById('modalCustomerLink').href = `customer_details.html?customer=${encodeURIComponent(transaction.customerId)}`;
            modalBody.innerHTML = `<div class="grid gap-4 rounded-lg bg-cream p-4 text-sm sm:grid-cols-2">
                ${fields.map(([label, value]) => `<div><span class="block text-xs text-slate">${label}</span><span class="font-medium">${escapeHtml(value || '—')}</span></div>`).join('')}
            </div>`;
        } catch (error) {
            modalBody.innerHTML = `<p class="text-sm text-red-700">${escapeHtml(errorMessage(error))}</p>`;
        }
    }

    loadTransactions();
})();
