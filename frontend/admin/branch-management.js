(() => {
    const api = VaultCoreApi;
    const escapeHtml = value => String(value ?? '').replace(/[&<>"']/g, char => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    })[char]);
    const money = value => `₹${Number(value || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;
    const message = error => `Unable to load branch data: ${error.message || 'Please try again.'}`;

    const rows = document.getElementById('rows');
    if (rows) {
        const filters = document.getElementById('filters');
        const count = document.getElementById('count');
        const stats = document.getElementById('stats');
        const pageSize = 10;
        let branches = [];
        let page = 1;

        const render = () => {
            const pageCount = Math.max(1, Math.ceil(branches.length / pageSize));
            page = Math.min(page, pageCount);
            const pageItems = branches.slice((page - 1) * pageSize, page * pageSize);
            count.textContent = `${branches.length} branch record${branches.length === 1 ? '' : 's'} found`;
            rows.innerHTML = pageItems.length ? pageItems.map(branch => `<tr class="hover:bg-cream/60">
                <td class="px-5 py-4 font-semibold text-vault">${escapeHtml(branch.branchId)}</td>
                <td class="px-5 py-4 font-medium">${escapeHtml(branch.branchName)}</td>
                <td class="px-5 py-4">${escapeHtml(branch.ifscCode)}</td>
                <td class="px-5 py-4">${escapeHtml(branch.district || '—')}</td>
                <td class="px-5 py-4">${escapeHtml(branch.address || '—')}</td>
                <td class="px-5 py-4">${escapeHtml(branch.phone || '—')}</td>
                <td class="px-5 py-4"><a class="font-semibold text-vault hover:underline" href="branch_details.html?branch=${encodeURIComponent(branch.branchId)}">View</a></td>
            </tr>`).join('') : '<tr><td colspan="7" class="px-5 py-10 text-center text-slate">No branch records found.</td></tr>';
            document.getElementById('pages').innerHTML = pageCount > 1
                ? `<button data-page="${page - 1}" ${page === 1 ? 'disabled' : ''} class="rounded border border-line px-3 py-1.5 text-sm disabled:opacity-40">Previous</button><span class="px-3 py-1.5 text-sm text-slate">Page ${page} of ${pageCount}</span><button data-page="${page + 1}" ${page === pageCount ? 'disabled' : ''} class="rounded border border-line px-3 py-1.5 text-sm disabled:opacity-40">Next</button>`
                : '';
            stats.innerHTML = `<article class="rounded-xl border border-line bg-white p-4 shadow-sm"><p class="text-xs font-medium text-slate">Branches in results</p><p class="mt-2 text-2xl font-bold text-vault">${branches.length.toLocaleString()}</p></article>`;
        };

        const loadBranches = async () => {
            const params = new URLSearchParams();
            [['branchNameFilter', 'branchName'], ['districtFilter', 'district'], ['ifscFilter', 'ifscCode']].forEach(([id, name]) => {
                const value = document.getElementById(id).value.trim();
                if (value) params.set(name, value);
            });
            rows.innerHTML = '<tr><td colspan="7" class="px-5 py-10 text-center text-slate">Loading branches…</td></tr>';
            count.textContent = 'Loading…';
            stats.innerHTML = '';
            try {
                const query = params.toString();
                branches = await api.request(`/api/admin/branches${query ? `?${query}` : ''}`);
                page = 1;
                render();
            } catch (error) {
                branches = [];
                rows.innerHTML = `<tr><td colspan="7" class="px-5 py-10 text-center text-red-700">${escapeHtml(message(error))}</td></tr>`;
                count.textContent = '';
                document.getElementById('pages').innerHTML = '';
            }
        };

        filters.addEventListener('submit', event => { event.preventDefault(); loadBranches(); });
        document.getElementById('resetFilters').addEventListener('click', () => { filters.reset(); loadBranches(); });
        document.getElementById('pages').addEventListener('click', event => {
            const button = event.target.closest('[data-page]');
            if (button && !button.disabled) {
                page = Number(button.dataset.page);
                render();
            }
        });
        loadBranches();
    }

    const branchContent = document.getElementById('content');
    if (branchContent && location.pathname.endsWith('branch_details.html')) {
        const id = new URLSearchParams(location.search).get('branch');
        if (!id) {
            branchContent.innerHTML = '<p class="rounded-lg border border-red-200 bg-white p-5 text-red-700">A branch ID is required.</p>';
            return;
        }
        branchContent.innerHTML = '<p class="text-sm text-slate">Loading branch, customer, account, and statistics data…</p>';
        Promise.all([
            api.request('/api/admin/branches'),
            api.request(`/api/admin/branches/${encodeURIComponent(id)}/customers`),
            api.request(`/api/admin/branches/${encodeURIComponent(id)}/accounts`),
            api.request(`/api/admin/branches/${encodeURIComponent(id)}/stats`)
        ]).then(([branches, customers, accounts, stats]) => {
            const branch = branches.find(item => String(item.branchId) === String(id));
            if (!branch) {
                branchContent.innerHTML = '<p class="rounded-lg border border-line bg-white p-5 text-slate">Branch not found.</p>';
                return;
            }
            const name = value => [value.firstName, value.lastName].filter(Boolean).join(' ');
            const customerRows = customers.length ? customers.map(customer => `<tr>
                <td class="px-5 py-4">${escapeHtml(customer.customerId)}</td>
                <td class="px-5 py-4">${escapeHtml(customer.customerNumber)}</td>
                <td class="px-5 py-4">${escapeHtml(name(customer))}</td>
                <td class="px-5 py-4">${escapeHtml(customer.phone || '—')}</td>
                <td class="px-5 py-4"><a class="font-semibold text-vault" href="customer_details.html?customer=${encodeURIComponent(customer.customerId)}">View Customer</a></td>
            </tr>`).join('') : '<tr><td colspan="5" class="px-5 py-8 text-center text-slate">No customers are associated with this branch.</td></tr>';
            const accountRows = accounts.length ? accounts.map(account => `<tr>
                <td class="px-5 py-4">${escapeHtml(account.accountNumber)}</td>
                <td class="px-5 py-4">${escapeHtml(account.accountType)}</td>
                <td class="px-5 py-4">${money(account.balance)}</td>
                <td class="px-5 py-4">${escapeHtml(account.status)}</td>
                <td class="px-5 py-4">${new Date(account.createdAt).toLocaleDateString()}</td>
                <td class="px-5 py-4"><a class="font-semibold text-vault" href="account_details.html?account=${encodeURIComponent(account.accountId)}">View Account</a></td>
            </tr>`).join('') : '<tr><td colspan="6" class="px-5 py-8 text-center text-slate">No accounts are associated with this branch.</td></tr>';
            branchContent.innerHTML = `<section class="grid gap-5 lg:grid-cols-[1.3fr_1fr]">
                <article class="rounded-xl border border-line bg-white p-5 shadow-sm"><h2 class="mb-4 font-bold">Branch Information</h2><div class="grid gap-4 text-sm sm:grid-cols-2">
                    ${[['Branch ID', branch.branchId], ['Branch Name', branch.branchName], ['IFSC Code', branch.ifscCode], ['District', branch.district], ['Address', branch.address], ['Phone', branch.phone]].map(([label, value]) => `<div><span class="block text-xs text-slate">${label}</span><span class="font-medium">${escapeHtml(value || '—')}</span></div>`).join('')}
                </div><button id="editBranchButton" class="mt-5 rounded-lg border border-line px-4 py-2 text-sm font-semibold text-vault">Edit Branch</button></article>
                <article class="rounded-xl border border-line bg-white p-5 shadow-sm"><h2 class="mb-4 font-bold">Branch Statistics</h2><div class="grid grid-cols-2 gap-3 text-sm">
                    ${[['Customers', stats.customerCount], ['Accounts', stats.accountCount], ['Total Balance', money(stats.totalBalance)]].map(([label, value]) => `<div class="rounded-lg bg-cream p-3"><span class="block text-xs text-slate">${label}</span><strong class="mt-1 block text-vault">${escapeHtml(value)}</strong></div>`).join('')}
                </div></article>
            </section><section class="mt-5 overflow-hidden rounded-xl border border-line bg-white shadow-sm">
                <div class="flex items-center justify-between border-b border-line px-5 py-4"><h2 class="font-bold">Branch Customers</h2><a class="text-sm font-semibold text-vault" href="customer_management.html?branch=${encodeURIComponent(branch.branchId)}">View All Customers</a></div>
                <div class="overflow-x-auto"><table class="w-full min-w-[650px] text-left text-sm"><thead class="bg-cream text-xs uppercase text-slate"><tr><th class="px-5 py-3">Customer ID</th><th class="px-5 py-3">Customer Number</th><th class="px-5 py-3">Name</th><th class="px-5 py-3">Phone</th><th class="px-5 py-3">Action</th></tr></thead><tbody class="divide-y divide-line">${customerRows}</tbody></table></div>
            </section><section class="mt-5 overflow-hidden rounded-xl border border-line bg-white shadow-sm">
                <div class="flex items-center justify-between border-b border-line px-5 py-4"><h2 class="font-bold">Branch Accounts</h2><a class="text-sm font-semibold text-vault" href="accounts.html?branch=${encodeURIComponent(branch.branchId)}">View All Accounts</a></div>
                <div class="overflow-x-auto"><table class="w-full min-w-[700px] text-left text-sm"><thead class="bg-cream text-xs uppercase text-slate"><tr><th class="px-5 py-3">Account Number</th><th class="px-5 py-3">Type</th><th class="px-5 py-3">Balance</th><th class="px-5 py-3">Status</th><th class="px-5 py-3">Created</th><th class="px-5 py-3">Action</th></tr></thead><tbody class="divide-y divide-line">${accountRows}</tbody></table></div>
            </section>`;
            document.getElementById('editBranchButton').addEventListener('click', () => openBranchEdit(branch));
        }).catch(error => {
            branchContent.innerHTML = `<p class="rounded-lg border border-red-200 bg-white p-5 text-red-700">${escapeHtml(message(error))}</p>`;
        });
    }

    function openBranchEdit(branch) {
        const modal = document.getElementById('branchForm');
        if (!modal) {
            location.href = `branches.html?edit=${encodeURIComponent(branch.branchId)}`;
            return;
        }
        const form = document.getElementById('branchEditForm');
        document.getElementById('editBranchName').value = branch.branchName || '';
        document.getElementById('editDistrict').value = branch.district || '';
        document.getElementById('editAddress').value = branch.address || '';
        document.getElementById('editPhone').value = branch.phone || '';
        modal.classList.replace('hidden', 'flex');
        form.onsubmit = async event => {
            event.preventDefault();
            const submit = form.querySelector('button[type="submit"], button:not([type])');
            submit.disabled = true;
            try {
                await api.request(`/api/branches/${encodeURIComponent(branch.branchId)}`, {
                    method: 'PUT',
                    body: JSON.stringify({
                        branchName: document.getElementById('editBranchName').value.trim(),
                        district: document.getElementById('editDistrict').value.trim(),
                        address: document.getElementById('editAddress').value.trim(),
                        phone: document.getElementById('editPhone').value.trim()
                    })
                });
                modal.classList.replace('flex', 'hidden');
                location.reload();
            } catch (error) {
                let notice = form.querySelector('[data-error]');
                if (!notice) {
                    notice = document.createElement('p');
                    notice.dataset.error = 'true';
                    notice.className = 'text-sm text-red-700 sm:col-span-2';
                    form.prepend(notice);
                }
                notice.textContent = `Unable to update branch: ${error.message || 'Please try again.'}`;
            } finally {
                submit.disabled = false;
            }
        };
        ['closeBranchForm', 'cancelBranchEdit'].forEach(id => document.getElementById(id).addEventListener('click', () => modal.classList.replace('flex', 'hidden'), { once: true }));
    }
})();
