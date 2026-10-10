(() => {
    const api = VaultCoreApi;
    const escapeHtml = value => String(value ?? '').replace(/[&<>"']/g, char => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    })[char]);
    const formatDate = value => value ? new Date(value).toLocaleDateString() : '—';
    const money = value => `₹${Number(value || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;
    const errorText = error => `Unable to load data: ${error.message || 'Please try again.'}`;

    const listRows = document.getElementById('customerRows');
    if (listRows) {
        const filterForm = document.getElementById('filterForm');
        const searchInput = document.getElementById('searchInput');
        const branchFilter = document.getElementById('branchFilter');
        const resetButton = document.getElementById('resetButton');
        const resultCount = document.getElementById('resultCount');
        const pageSize = 10;
        let customers = [];
        let page = 1;

        const render = () => {
            const pages = Math.max(1, Math.ceil(customers.length / pageSize));
            page = Math.min(page, pages);
            const current = customers.slice((page - 1) * pageSize, page * pageSize);
            resultCount.textContent = `${customers.length} customer record${customers.length === 1 ? '' : 's'} found`;
            listRows.innerHTML = current.length ? current.map(customer => {
                const name = [customer.firstName, customer.lastName].filter(Boolean).join(' ');
                return `<tr class="hover:bg-cream/60">
                    <td class="px-5 py-4 font-semibold text-vault">${escapeHtml(customer.customerId)}</td>
                    <td class="px-5 py-4">${escapeHtml(customer.customerNumber)}</td>
                    <td class="px-5 py-4 font-medium">${escapeHtml(name)}</td>
                    <td class="px-5 py-4">${escapeHtml(customer.phone || '—')}</td>
                    <td class="px-5 py-4">${escapeHtml(customer.branchName || customer.branchId || '—')}</td>
                    <td class="px-5 py-4"><button data-customer-id="${escapeHtml(customer.customerId)}" class="font-semibold text-vault hover:underline">View</button></td>
                </tr>`;
            }).join('') : '<tr><td colspan="6" class="px-5 py-10 text-center text-slate">No customer records found.</td></tr>';
            const pagination = document.getElementById('pagination');
            pagination.innerHTML = pages > 1
                ? `<button data-page="${page - 1}" ${page === 1 ? 'disabled' : ''} class="rounded border border-line px-3 py-1.5 text-sm disabled:opacity-40">Previous</button>
                    <span class="px-3 py-1.5 text-sm text-slate">Page ${page} of ${pages}</span>
                    <button data-page="${page + 1}" ${page === pages ? 'disabled' : ''} class="rounded border border-line px-3 py-1.5 text-sm disabled:opacity-40">Next</button>`
                : '';
        };

        const load = async () => {
            const params = new URLSearchParams();
            if (searchInput.value.trim()) params.set('search', searchInput.value.trim());
            if (branchFilter.value) params.set('branchId', branchFilter.value);
            listRows.innerHTML = '<tr><td colspan="6" class="px-5 py-10 text-center text-slate">Loading customers…</td></tr>';
            resultCount.textContent = 'Loading…';
            document.getElementById('statistics').innerHTML = '';
            try {
                const query = params.toString();
                customers = await api.request(`/api/customers${query ? `?${query}` : ''}`);
                page = 1;
                document.getElementById('statistics').innerHTML = `<article class="rounded-xl border border-line bg-white p-4 shadow-sm"><p class="text-xs font-medium text-slate">Customers in results</p><p class="mt-2 text-2xl font-bold text-vault">${customers.length.toLocaleString()}</p></article>`;
                render();
            } catch (error) {
                customers = [];
                listRows.innerHTML = `<tr><td colspan="6" class="px-5 py-10 text-center text-red-700">${escapeHtml(errorText(error))}</td></tr>`;
                resultCount.textContent = '';
                document.getElementById('pagination').innerHTML = '';
            }
        };

        filterForm.addEventListener('submit', event => {
            event.preventDefault();
            load();
        });
        resetButton.addEventListener('click', () => {
            filterForm.reset();
            load();
        });
        listRows.addEventListener('click', event => {
            const button = event.target.closest('[data-customer-id]');
            if (button) openDetails(button.dataset.customerId);
        });
        document.getElementById('pagination').addEventListener('click', event => {
            const button = event.target.closest('[data-page]');
            if (button && !button.disabled) {
                page = Number(button.dataset.page);
                render();
            }
        });

        const branchParam = new URLSearchParams(location.search).get('branch');
        if (branchParam && /^\d+$/.test(branchParam)) branchFilter.value = branchParam;
        load();

        async function openDetails(id) {
            const modal = document.getElementById('detailModal');
            const detailContent = document.getElementById('detailContent');
            document.getElementById('modalCustomerId').textContent = `Customer ID ${id}`;
            document.getElementById('customerTransactionsLink').href = `customer_transactions.html?customer=${encodeURIComponent(id)}`;
            detailContent.innerHTML = '<p class="text-sm text-slate">Loading customer and account information…</p>';
            modal.classList.replace('hidden', 'flex');
            try {
                const [customer, accounts] = await Promise.all([
                    api.request(`/api/customers/${encodeURIComponent(id)}`),
                    api.request(`/api/customers/${encodeURIComponent(id)}/accounts`)
                ]);
                const name = [customer.firstName, customer.lastName].filter(Boolean).join(' ');
                document.getElementById('modalCustomerId').textContent = `${customer.customerNumber || ''} · ${name}`;
                const fields = [
                    ['Customer ID', customer.customerId],
                    ['Customer Number', customer.customerNumber],
                    ['Name', name],
                    ['Phone', customer.phone],
                    ['Date of Birth', customer.dob],
                    ['Gender', customer.gender],
                    ['Address', customer.address],
                    ['Branch', customer.branchName],
                    ['Created', formatDate(customer.createdAt)]
                ];
                const accountRows = accounts.length ? accounts.map(account => `<tr class="border-t border-line">
                    <td class="p-3">${escapeHtml(account.accountNumber)}</td>
                    <td class="p-3">${escapeHtml(account.accountType)}</td>
                    <td class="p-3">${money(account.balance)}</td>
                    <td class="p-3">${escapeHtml(account.status)}</td>
                    <td class="p-3"><a class="font-semibold text-vault" href="account_details.html?account=${encodeURIComponent(account.accountId)}">View Account</a></td>
                </tr>`).join('') : '<tr><td colspan="5" class="p-4 text-center text-slate">No accounts linked to this customer.</td></tr>';
                detailContent.innerHTML = `<h3 class="mb-3 font-bold">Personal Information</h3>
                    <div class="mb-6 grid gap-3 rounded-lg bg-cream p-4 text-sm sm:grid-cols-2">${fields.map(([label, value]) => `<div><span class="block text-xs text-slate">${label}</span><span class="font-medium">${escapeHtml(value || '—')}</span></div>`).join('')}</div>
                    <h3 class="mb-3 font-bold">Accounts</h3>
                    <div class="overflow-x-auto rounded-lg border border-line"><table class="w-full min-w-[600px] text-left text-sm">
                        <thead class="bg-cream"><tr><th class="p-3">Account Number</th><th class="p-3">Type</th><th class="p-3">Balance</th><th class="p-3">Status</th><th class="p-3">Action</th></tr></thead>
                        <tbody>${accountRows}</tbody>
                    </table></div>`;
            } catch (error) {
                detailContent.innerHTML = `<p class="text-sm text-red-700">${escapeHtml(errorText(error))}</p>`;
            }
        }
    }

    const customerContent = document.getElementById('content');
    if (customerContent && location.pathname.endsWith('customer_details.html')) {
        const id = new URLSearchParams(location.search).get('customer');
        if (!id) {
            customerContent.innerHTML = '<p class="rounded-lg border border-red-200 bg-white p-5 text-red-700">A customer ID is required.</p>';
        } else {
            customerContent.innerHTML = '<p class="text-sm text-slate">Loading customer details…</p>';
            Promise.all([
                api.request(`/api/customers/${encodeURIComponent(id)}`),
                api.request(`/api/customers/${encodeURIComponent(id)}/accounts`)
            ]).then(([customer, accounts]) => {
                const name = [customer.firstName, customer.lastName].filter(Boolean).join(' ');
                const profile = [
                    ['Customer ID', customer.customerId], ['Customer Number', customer.customerNumber],
                    ['Customer Name', name], ['Phone', customer.phone], ['Date of Birth', customer.dob],
                    ['Gender', customer.gender], ['Address', customer.address], ['Branch', customer.branchName],
                    ['Registration Date', formatDate(customer.createdAt)]
                ];
                const accountRows = accounts.length ? accounts.map(account => `<tr>
                    <td class="px-5 py-4 font-semibold text-vault">${escapeHtml(account.accountNumber)}</td>
                    <td class="px-5 py-4">${escapeHtml(account.accountType)}</td>
                    <td class="px-5 py-4">${money(account.balance)}</td>
                    <td class="px-5 py-4">${escapeHtml(account.status)}</td>
                    <td class="px-5 py-4">${formatDate(account.createdAt)}</td>
                    <td class="px-5 py-4"><a class="font-semibold text-vault" href="account_details.html?account=${encodeURIComponent(account.accountId)}">View Account</a></td>
                </tr>`).join('') : '<tr><td colspan="6" class="px-5 py-8 text-center text-slate">No accounts linked to this customer.</td></tr>';
                customerContent.innerHTML = `<section class="grid gap-5 lg:grid-cols-[1.4fr_1fr]">
                    <article class="rounded-xl border border-line bg-white p-5 shadow-sm"><h2 class="mb-4 font-bold">Customer Profile</h2><div class="grid gap-4 text-sm sm:grid-cols-2">${profile.map(([label, value]) => `<div><span class="block text-xs text-slate">${label}</span><span class="font-medium">${escapeHtml(value || '—')}</span></div>`).join('')}</div></article>
                    <article class="rounded-xl border border-line bg-white p-5 shadow-sm"><h2 class="mb-4 font-bold">Account Summary</h2><div class="grid grid-cols-2 gap-3 text-sm">
                        <div class="rounded-lg bg-cream p-3"><span class="block text-xs text-slate">Total Accounts</span><strong class="mt-1 block text-vault">${accounts.length}</strong></div>
                        <div class="rounded-lg bg-cream p-3"><span class="block text-xs text-slate">Total Balance</span><strong class="mt-1 block text-vault">${money(accounts.reduce((sum, account) => sum + Number(account.balance || 0), 0))}</strong></div>
                    </div></article>
                </section><section class="mt-5 overflow-hidden rounded-xl border border-line bg-white shadow-sm">
                    <div class="flex flex-wrap items-center justify-between gap-3 border-b border-line px-5 py-4"><h2 class="font-bold">Customer Accounts</h2><a class="rounded-lg border border-line px-3 py-2 text-sm font-semibold text-vault" href="customer_transactions.html?customer=${encodeURIComponent(customer.customerId)}">View Transactions</a></div>
                    <div class="overflow-x-auto"><table class="w-full min-w-[850px] text-left text-sm"><thead class="bg-cream text-xs uppercase text-slate"><tr><th class="px-5 py-3">Account Number</th><th class="px-5 py-3">Account Type</th><th class="px-5 py-3">Balance</th><th class="px-5 py-3">Status</th><th class="px-5 py-3">Created</th><th class="px-5 py-3">Action</th></tr></thead><tbody class="divide-y divide-line">${accountRows}</tbody></table></div>
                </section>`;
            }).catch(error => {
                customerContent.innerHTML = `<p class="rounded-lg border border-red-200 bg-white p-5 text-red-700">${escapeHtml(errorText(error))}</p>`;
            });
        }
    }

    const transactionRows = document.getElementById('rows');
    if (transactionRows && document.getElementById('filters')) {
        const params = new URLSearchParams(location.search);
        const customerId = params.get('customer');
        const customerLink = document.getElementById('customerLink');
        const summary = document.getElementById('summary');
        const filters = document.getElementById('filters');
        const search = document.getElementById('search');
        const type = document.getElementById('type');
        const min = document.getElementById('min');
        let transactions = [];
        customerLink.href = customerId ? `customer_details.html?customer=${encodeURIComponent(customerId)}` : 'customer_management.html';
        const render = () => {
            const query = search.value.trim().toLowerCase();
            const visible = transactions.filter(transaction => {
                const account = transaction.account || {};
                const matchesText = !query || `${transaction.transactionId} ${account.accountNumber || ''}`.toLowerCase().includes(query);
                const matchesType = !type.value || String(transaction.transactionType).toLowerCase() === type.value.toLowerCase();
                const matchesAmount = !min.value || Number(transaction.amount) >= Number(min.value);
                return matchesText && matchesType && matchesAmount;
            });
            document.getElementById('total').textContent = visible.length;
            transactionRows.innerHTML = visible.length ? visible.map(transaction => {
                const account = transaction.account || {};
                return `<tr>
                    <td class="px-5 py-4 font-semibold text-vault">${escapeHtml(transaction.transactionId)}</td>
                    <td class="px-5 py-4"><a class="text-vault hover:underline" href="account_details.html?account=${encodeURIComponent(account.accountId || '')}">${escapeHtml(account.accountNumber || '—')}</a></td>
                    <td class="px-5 py-4">${escapeHtml(transaction.transactionType)}</td>
                    <td class="px-5 py-4">${money(transaction.amount)}</td>
                    <td class="px-5 py-4">${escapeHtml(transaction.transactionDate ? new Date(transaction.transactionDate).toLocaleString() : '—')}</td>
                </tr>`;
            }).join('') : '<tr><td colspan="5" class="px-5 py-10 text-center text-slate">No customer transactions found.</td></tr>';
        };
        const loadTransactions = async () => {
            if (!customerId) {
                transactionRows.innerHTML = '<tr><td colspan="5" class="px-5 py-10 text-center text-red-700">A customer ID is required.</td></tr>';
                return;
            }
            summary.innerHTML = '<p class="text-sm text-slate">Loading customer transactions…</p>';
            transactionRows.innerHTML = '<tr><td colspan="5" class="px-5 py-10 text-center text-slate">Loading transactions…</td></tr>';
            try {
                const [customer, result] = await Promise.all([
                    api.request(`/api/customers/${encodeURIComponent(customerId)}`),
                    api.request(`/api/customers/${encodeURIComponent(customerId)}/transactions`)
                ]);
                transactions = result;
                const name = [customer.firstName, customer.lastName].filter(Boolean).join(' ');
                summary.innerHTML = `<section class="grid gap-3 sm:grid-cols-2"><div class="rounded-xl border border-line bg-white p-4"><span class="text-xs text-slate">Customer</span><strong class="mt-1 block text-vault">${escapeHtml(name)} (${escapeHtml(customer.customerNumber)})</strong></div><div class="rounded-xl border border-line bg-white p-4"><span class="text-xs text-slate">Transactions</span><strong id="total" class="mt-1 block text-vault"></strong></div></section>`;
                render();
            } catch (error) {
                summary.innerHTML = `<p class="text-sm text-red-700">${escapeHtml(errorText(error))}</p>`;
                transactionRows.innerHTML = `<tr><td colspan="5" class="px-5 py-10 text-center text-red-700">${escapeHtml(errorText(error))}</td></tr>`;
            }
        };
        filters.addEventListener('submit', event => { event.preventDefault(); render(); });
        document.getElementById('reset').addEventListener('click', () => { filters.reset(); render(); });
        loadTransactions();
    }
})();
