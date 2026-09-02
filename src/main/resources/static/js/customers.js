/**
 * IntelliERP Customers & CRM Controller
 */

let allCustomers = [];
let editingCustomerId = null;

async function initCustomersPage() {
    loadCustomerAnalytics();
    loadCustomers();
}

async function loadCustomerAnalytics() {
    try {
        const stats = await Api.get('/customers/analytics');
        document.getElementById('cust-total').textContent = Utils.formatNumber(stats.totalCustomers);
        document.getElementById('cust-vip').textContent = Utils.formatNumber(stats.vipCustomers);
        document.getElementById('cust-returning').textContent = Utils.formatNumber(stats.returningCustomers);
        document.getElementById('cust-regular').textContent = Utils.formatNumber(stats.regularCustomers);
        document.getElementById('cust-churn').textContent = Utils.formatNumber(stats.churnRiskCustomers);
        document.getElementById('cust-total-spend').textContent = Utils.formatINR(stats.totalCustomerSpend);
    } catch (e) {
        console.error('Error loading customer analytics:', e);
    }
}

async function loadCustomers() {
    try {
        allCustomers = await Api.get('/customers');
        renderCustomersTable(allCustomers);
    } catch (e) {
        console.error('Error loading customers:', e);
        Utils.showToast('Failed to load customers: ' + e.message, 'danger');
    }
}

function renderCustomersTable(customers) {
    const tbody = document.getElementById('customers-tbody');
    if (!tbody) return;

    if (customers.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="empty-state"><div class="empty-icon">👥</div><div class="empty-text">No customers found</div></td></tr>`;
        return;
    }

    tbody.innerHTML = customers.map(c => `
        <tr>
            <td>
                <strong>${c.name}</strong><br>
                <small class="text-muted">${c.email || c.phone || 'No direct contact'}</small>
            </td>
            <td>${renderTierBadge(c.tier)}</td>
            <td><strong>${c.totalOrders || 0} orders</strong></td>
            <td><strong>${Utils.formatINR(c.totalSpend)}</strong></td>
            <td>${c.totalOrders > 0 ? Utils.formatINR((c.totalSpend || 0) / c.totalOrders) : '₹0.00'}</td>
            <td>${Utils.formatDate(c.lastPurchaseDate)}</td>
            <td>
                <div class="table-actions">
                    <button class="action-btn" title="Edit Customer" onclick="openEditCustomerModal(${c.id})">✎</button>
                    <button class="action-btn delete" title="Delete Customer" onclick="deleteCustomer(${c.id}, '${c.name}')">✕</button>
                </div>
            </td>
        </tr>
    `).join('');
}

function renderTierBadge(tier) {
    const map = {
        'VIP': '<span class="badge badge-success">👑 VIP</span>',
        'RETURNING': '<span class="badge badge-info">🔄 Returning</span>',
        'REGULAR': '<span class="badge badge-primary">Regular</span>',
        'CHURN_RISK': '<span class="badge badge-danger">⚠️ Churn Risk</span>',
        'INACTIVE': '<span class="badge badge-warning">Inactive</span>'
    };
    return map[tier] || `<span class="badge badge-primary">${tier}</span>`;
}

function filterCustomers() {
    const q = document.getElementById('search-customers')?.value.toLowerCase() || '';
    const tier = document.getElementById('filter-tier')?.value || '';

    const filtered = allCustomers.filter(c => {
        const matchesQuery = c.name.toLowerCase().includes(q) || (c.email && c.email.toLowerCase().includes(q));
        const matchesTier = !tier || c.tier === tier;
        return matchesQuery && matchesTier;
    });

    renderCustomersTable(filtered);
}

function openAddCustomerModal() {
    editingCustomerId = null;
    document.getElementById('customer-modal-title').textContent = 'Add New Customer';
    document.getElementById('cust-name').value = '';
    document.getElementById('cust-email').value = '';
    document.getElementById('cust-phone').value = '';
    document.getElementById('cust-address').value = '';
    document.getElementById('cust-tier').value = 'REGULAR';
    Utils.openModal('customer-modal');
}

function openEditCustomerModal(id) {
    const c = allCustomers.find(item => item.id === id);
    if (!c) return;

    editingCustomerId = c.id;
    document.getElementById('customer-modal-title').textContent = 'Edit Customer';
    document.getElementById('cust-name').value = c.name;
    document.getElementById('cust-email').value = c.email || '';
    document.getElementById('cust-phone').value = c.phone || '';
    document.getElementById('cust-address').value = c.address || '';
    document.getElementById('cust-tier').value = c.tier || 'REGULAR';
    Utils.openModal('customer-modal');
}

async function saveCustomer() {
    const name = document.getElementById('cust-name').value.trim();
    const email = document.getElementById('cust-email').value.trim();
    const phone = document.getElementById('cust-phone').value.trim();
    const address = document.getElementById('cust-address').value.trim();
    const tier = document.getElementById('cust-tier').value;

    if (!name) {
        Utils.showToast('Please enter customer name', 'warning');
        return;
    }

    const payload = {
        name,
        email,
        phone,
        address,
        tier
    };

    try {
        if (editingCustomerId) {
            await Api.put(`/customers/${editingCustomerId}`, payload);
            Utils.showToast('Customer updated successfully!', 'success');
        } else {
            await Api.post('/customers', payload);
            Utils.showToast('Customer added successfully!', 'success');
        }
        Utils.closeModal('customer-modal');
        loadCustomers();
        loadCustomerAnalytics();
    } catch (e) {
        Utils.showToast('Failed to save customer: ' + e.message, 'danger');
    }
}

async function deleteCustomer(id, name) {
    if (!confirm(`Are you sure you want to delete customer '${name}'?`)) return;
    try {
        await Api.delete(`/customers/${id}`);
        Utils.showToast(`Customer '${name}' deleted`, 'success');
        loadCustomers();
        loadCustomerAnalytics();
    } catch (e) {
        Utils.showToast('Cannot delete customer: ' + e.message, 'danger');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    initCustomersPage();
});
