(() => {
    const api = VaultCoreApi;
    const escapeHtml = value => String(value ?? '').replace(/[&<>"']/g, char => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    })[char]);
    const formatType = value => String(value || '—').replaceAll('_', ' ');
    const formatDate = value => value ? new Date(value).toLocaleString() : '—';
    const money = value => `₹${Number(value || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;
    const requestError = error => error.message || 'Please try again.';

    const rows = document.getElementById('rows');
    if (rows) {
        const filters = document.getElementById('filters');
        const count = document.getElementById('count');
        const stats = document.getElementById('stats');
        const pageSize = 10;
        let accounts = [];
        let page = 1;
        const currentParams = new URLSearchParams(location.search);

        const render = () => {
            const pages = Math.max(1, Math.ceil(accounts.length / pageSize));
            page = Math.min(page, pages);
            const visible = accounts.slice((page - 1) * pageSize, page * pageSize);
            count.textContent = `${accounts.length} account record${accounts.length === 1 ? '' : 's'} found`;
            rows.innerHTML = visible.length ? visible.map(account => `<tr class="hover:bg-cream/60">
                <td class="px-5 py-4 font-semibold text-vault">${escapeHtml(account.accountNumber)}</td>
                <td class="px-5 py-4 font-medium">${escapeHtml(account.customerName || '—')}</td>
                <td class="px-5 py-4">${escapeHtml(account.customerId)}</td>
                <td class="px-5 py-4">${escapeHtml(formatType(account.accountType))}</td>
                <td class="px-5 py-4">${escapeHtml(account.branchName || '—')}</td>
                <td class="px-5 py-4 font-medium">${money(account.balance)}</td>
                <td class="px-5 py-4">${escapeHtml(account.status)}</td>
                <td class="px-5 py-4">${formatDate(account.createdAt)}</td>
                <td class="px-5 py-4"><a href="account_details.html?account=${encodeURIComponent(account.accountId)}" class="font-semibold text-vault hover:underline">View</a></td>
            </tr>`).join('') : '<tr><td colspan="9" class="px-5 py-10 text-center text-slate">No accounts found.</td></tr>';
            document.getElementById('pages').innerHTML = pages > 1
                ? `<button data-page="${page - 1}" ${page === 1 ? 'disabled' : ''} class="rounded border border-line px-3 py-1.5 text-sm disabled:opacity-40">Previous</button><span class="px-3 py-1.5 text-sm text-slate">Page ${page} of ${pages}</span><button data-page="${page + 1}" ${page === pages ? 'disabled' : ''} class="rounded border border-line px-3 py-1.5 text-sm disabled:opacity-40">Next</button>`
                : '';
            const byStatus = value => accounts.filter(account => String(account.status).toUpperCase() === value).length;
            stats.innerHTML = [
                ['Accounts in results', accounts.length, '#8A1538'],
                ['Active', byStatus('ACTIVE'), '#15803d'],
                ['Frozen', byStatus('FROZEN'), '#b7791f'],
                ['Closed', byStatus('CLOSED'), '#b91c1c']
            ].map(([label, value, color]) => `<article class="rounded-xl border border-line bg-white p-4 shadow-sm"><p class="text-xs font-medium text-slate">${label}</p><p class="mt-2 text-2xl font-bold" style="color:${color}">${value.toLocaleString()}</p></article>`).join('');
        };

        const loadAccounts = async () => {
            const params = new URLSearchParams();
            const queryFields = [
                ['accountNumberFilter', 'accountNumber'],
                ['type', 'accountType'],
                ['status', 'status'],
                ['customerIdFilter', 'customerId'],
                ['branchIdFilter', 'branchId']
            ];
            queryFields.forEach(([elementId, key]) => {
                const value = document.getElementById(elementId).value.trim();
                if (value) params.set(key, value);
            });
            const branchFromUrl = currentParams.get('branch');
            const accountFromUrl = currentParams.get('account');
            if (!params.has('branchId') && branchFromUrl && /^\d+$/.test(branchFromUrl)) {
                params.set('branchId', branchFromUrl);
                document.getElementById('branchIdFilter').value = branchFromUrl;
            }
            if (!params.has('accountNumber') && accountFromUrl) {
                params.set('accountNumber', accountFromUrl);
                document.getElementById('accountNumberFilter').value = accountFromUrl;
            }
            rows.innerHTML = '<tr><td colspan="9" class="px-5 py-10 text-center text-slate">Loading accounts…</td></tr>';
            count.textContent = 'Loading…';
            stats.innerHTML = '';
            try {
                const query = params.toString();
                accounts = await api.request(`/api/reference/accounts${query ? `?${query}` : ''}`);
                page = 1;
                render();
            } catch (error) {
                accounts = [];
                rows.innerHTML = `<tr><td colspan="9" class="px-5 py-10 text-center text-red-700">Unable to load accounts: ${escapeHtml(requestError(error))}</td></tr>`;
                count.textContent = '';
                document.getElementById('pages').innerHTML = '';
            }
        };
        filters.addEventListener('submit', event => { event.preventDefault(); loadAccounts(); });
        document.getElementById('resetFilters').addEventListener('click', () => { filters.reset(); loadAccounts(); });
        document.getElementById('pages').addEventListener('click', event => {
            const button = event.target.closest('[data-page]');
            if (button && !button.disabled) {
                page = Number(button.dataset.page);
                render();
            }
        });
        loadAccounts();
    }

    const accountContent = document.getElementById('content');
    if (accountContent && location.pathname.endsWith('account_details.html')) {
        const accountKey = new URLSearchParams(location.search).get('account');
        if (!accountKey) {
            accountContent.innerHTML = '<p class="rounded-lg border border-red-200 bg-white p-5 text-red-700">An account ID is required.</p>';
            return;
        }
        accountContent.innerHTML = '<p class="text-sm text-slate">Loading account, transaction, and transfer details…</p>';
        const loadAccount = async () => {
            let accountId = accountKey;
            if (!/^\d+$/.test(accountKey) || accountKey.length >= 9) {
                const match = await api.request(`/api/reference/accounts?accountNumber=${encodeURIComponent(accountKey)}`);
                const found = match.find(item => String(item.accountNumber) === String(accountKey));
                if (!found) throw new Error('Account not found.');
                accountId = found.accountId;
            }
            const [account, transactions, transfers] = await Promise.all([
                api.request(`/api/accounts/${encodeURIComponent(accountId)}`),
                api.request(`/api/accounts/${encodeURIComponent(accountId)}/transactions`),
                api.request(`/api/accounts/${encodeURIComponent(accountId)}/transfers`)
            ]);
            return { account, transactions, transfers };
        };
        loadAccount().then(({ account, transactions, transfers }) => {
            const transactionRows = transactions.length ? transactions.map(transaction => `<tr>
                <td class="p-3">${escapeHtml(transaction.transactionId)}</td>
                <td class="p-3">${escapeHtml(formatType(transaction.transactionType))}</td>
                <td class="p-3">${money(transaction.amount)}</td>
                <td class="p-3">${escapeHtml(formatDate(transaction.transactionDate))}</td>
                <td class="p-3">${escapeHtml(transaction.description || '—')}</td>
            </tr>`).join('') : '<tr><td colspan="5" class="p-4 text-center text-slate">No transactions for this account.</td></tr>';
            const transferRows = transfers.length ? transfers.map(transfer => `<tr>
                <td class="p-3">${escapeHtml(transfer.transferId)}</td>
                <td class="p-3">${escapeHtml(transfer.senderAccountId)}</td>
                <td class="p-3">${escapeHtml(transfer.receiverAccountId)}</td>
                <td class="p-3">${money(transfer.amount)}</td>
                <td class="p-3">${escapeHtml(formatDate(transfer.transferDate))}</td>
                <td class="p-3">${escapeHtml(transfer.status)}</td>
            </tr>`).join('') : '<tr><td colspan="6" class="p-4 text-center text-slate">No transfers for this account.</td></tr>';
            accountContent.innerHTML = `<section class="grid gap-5 lg:grid-cols-2">
                <article class="rounded-xl border border-line bg-white p-5 shadow-sm"><h2 class="mb-4 font-bold">Account Summary</h2><div class="grid gap-4 text-sm sm:grid-cols-2">
                    ${[['Account ID', account.accountId], ['Account Number', account.accountNumber], ['Account Type', formatType(account.accountType)], ['Current Balance', money(account.balance)], ['Status', account.status], ['Date Created', formatDate(account.createdAt)]].map(([label, value]) => `<div><span class="block text-xs text-slate">${label}</span><span class="font-medium">${escapeHtml(value)}</span></div>`).join('')}
                </div><button id="statusAction" class="mt-5 rounded-lg border border-gold bg-gold px-4 py-2 text-sm font-semibold text-ink"></button><p id="statusMessage" class="mt-3 text-sm"></p></article>
                <article class="rounded-xl border border-line bg-white p-5 shadow-sm"><h2 class="mb-4 font-bold">Customer Information</h2><div class="grid gap-4 text-sm sm:grid-cols-2">
                    ${[['Customer ID', account.customerId], ['Customer Number', account.customerNumber], ['Customer Name', account.customerName], ['Branch ID', account.branchId], ['Branch Name', account.branchName]].map(([label, value]) => `<div><span class="block text-xs text-slate">${label}</span><span class="font-medium">${escapeHtml(value || '—')}</span></div>`).join('')}
                </div><div class="mt-5 flex gap-3"><a class="text-sm font-semibold text-vault" href="customer_details.html?customer=${encodeURIComponent(account.customerId)}">View Customer</a><a class="text-sm font-semibold text-vault" href="branch_details.html?branch=${encodeURIComponent(account.branchId)}">View Branch</a></div></article>
            </section><section class="mt-5 overflow-hidden rounded-xl border border-line bg-white shadow-sm"><div class="border-b border-line px-5 py-4"><h2 class="font-bold">Account Transactions</h2></div>
                <div class="overflow-x-auto"><table class="w-full min-w-[700px] text-left text-sm"><thead class="bg-cream text-xs uppercase text-slate"><tr><th class="p-3">Transaction ID</th><th class="p-3">Type</th><th class="p-3">Amount</th><th class="p-3">Date &amp; Time</th><th class="p-3">Description</th></tr></thead><tbody class="divide-y divide-line">${transactionRows}</tbody></table></div>
            </section><section class="mt-5 overflow-hidden rounded-xl border border-line bg-white shadow-sm"><div class="border-b border-line px-5 py-4"><h2 class="font-bold">Account Transfers</h2></div>
                <div class="overflow-x-auto"><table class="w-full min-w-[700px] text-left text-sm"><thead class="bg-cream text-xs uppercase text-slate"><tr><th class="p-3">Transfer ID</th><th class="p-3">Sender Account ID</th><th class="p-3">Receiver Account ID</th><th class="p-3">Amount</th><th class="p-3">Date &amp; Time</th><th class="p-3">Status</th></tr></thead><tbody class="divide-y divide-line">${transferRows}</tbody></table></div>
            </section>`;
            const action = document.getElementById('statusAction');
            const statusMessage = document.getElementById('statusMessage');
            const frozen = String(account.status).toUpperCase() === 'FROZEN';
            action.textContent = frozen ? 'Unfreeze Account' : 'Freeze Account';
            action.disabled = !['ACTIVE', 'FROZEN'].includes(String(account.status).toUpperCase());
            action.classList.toggle('hidden', !['ACTIVE', 'FROZEN'].includes(String(account.status).toUpperCase()));
            action.addEventListener('click', async () => {
                const reason = window.prompt(`Reason to ${frozen ? 'unfreeze' : 'freeze'} this account:`);
                if (!reason || !reason.trim()) return;
                action.disabled = true;
                statusMessage.textContent = 'Submitting status change…';
                statusMessage.className = 'mt-3 text-sm text-slate';
                try {
                    await api.request(`/api/accounts/${encodeURIComponent(account.accountId)}/${frozen ? 'unfreeze' : 'freeze'}`, {
                        method: 'POST',
                        body: JSON.stringify({ reason: reason.trim(), remarks: '' })
                    });
                    location.reload();
                } catch (error) {
                    statusMessage.textContent = `Unable to change account status: ${requestError(error)}`;
                    statusMessage.className = 'mt-3 text-sm text-red-700';
                    action.disabled = false;
                }
            });
        }).catch(error => {
            accountContent.innerHTML = `<p class="rounded-lg border border-red-200 bg-white p-5 text-red-700">Unable to load account details: ${escapeHtml(requestError(error))}</p>`;
        });
    }
})();
