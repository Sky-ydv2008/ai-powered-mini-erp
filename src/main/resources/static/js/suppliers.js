/**
 * IntelliERP Suppliers & Supplier Loss Detector Controller
 */

let allSuppliers = [];
let editingSupplierId = null;

async function initSuppliersPage() {
    loadSuppliers();
    loadSupplierLossReport();
}

async function loadSuppliers() {
    try {
        allSuppliers = await Api.get('/suppliers');
        renderSuppliersTable(allSuppliers);
    } catch (e) {
        console.error('Error loading suppliers:', e);
        Utils.showToast('Failed to load suppliers: ' + e.message, 'danger');
    }
}

async function loadSupplierLossReport() {
    try {
        const report = await Api.get('/suppliers/loss-detector');
        const tbody = document.getElementById('loss-detector-tbody');
        if (!tbody) return;

        let grandTotalLoss = 0;

        tbody.innerHTML = report.map(r => {
            grandTotalLoss += parseFloat(r.estimatedLoss) || 0;
            const isHighLoss = (parseFloat(r.estimatedLoss) || 0) > 30000;
            return `
                <tr style="${isHighLoss ? 'background: rgba(239, 68, 68, 0.08);' : ''}">
                    <td><strong>${r.supplierName}</strong> ${isHighLoss ? '<span class="badge badge-danger">High Risk</span>' : ''}</td>
                    <td>${Utils.formatINR(r.totalPurchases)}</td>
                    <td><span style="color: var(--danger); font-weight: 600;">${Utils.formatINR(r.defectiveLoss)}</span> (${r.defectRate}%)</td>
                    <td><span style="color: var(--warning); font-weight: 600;">${Utils.formatINR(r.delayLoss)}</span> (${r.delayedOrders} delayed)</td>
                    <td><span style="color: var(--info); font-weight: 600;">${Utils.formatINR(r.returnLoss)}</span> (${r.returnRate}%)</td>
                    <td><strong style="color: var(--danger); font-size: 15px;">${Utils.formatINR(r.estimatedLoss)}</strong></td>
                    <td>${Utils.renderRatingStars(r.rating)}</td>
                </tr>
            `;
        }).join('');

        const lossSummaryEl = document.getElementById('grand-total-supplier-loss');
        if (lossSummaryEl) {
            lossSummaryEl.textContent = Utils.formatINR(grandTotalLoss);
        }
    } catch (e) {
        console.error('Error loading supplier loss report:', e);
    }
}

function renderSuppliersTable(suppliers) {
    const tbody = document.getElementById('suppliers-tbody');
    if (!tbody) return;

    if (suppliers.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" class="empty-state"><div class="empty-icon">🏭</div><div class="empty-text">No suppliers registered</div></td></tr>`;
        return;
    }

    tbody.innerHTML = suppliers.map(s => `
        <tr>
            <td>
                <strong>${s.name}</strong><br>
                <small class="text-muted">${s.contactPerson || '-'} • ${s.phone || s.email || ''}</small>
            </td>
            <td><strong>${s.leadTimeDays} days</strong></td>
            <td>${Utils.formatINR(s.totalPurchases)}</td>
            <td>
                <span class="badge ${s.onTimeDeliveryRate >= 95 ? 'badge-success' : s.onTimeDeliveryRate >= 80 ? 'badge-warning' : 'badge-danger'}">
                    ${s.onTimeDeliveryRate}% On-Time
                </span>
            </td>
            <td><span class="badge ${s.defectRate > 5 ? 'badge-danger' : 'badge-success'}">${s.defectRate}%</span></td>
            <td>
                <div style="display: flex; align-items: center; gap: 6px;">
                    <div style="width: 60px; height: 6px; background: rgba(255,255,255,0.1); border-radius: 3px; overflow: hidden;">
                        <div style="width: ${s.performanceScore}%; height: 100%; background: ${s.performanceScore >= 80 ? 'var(--success)' : s.performanceScore >= 60 ? 'var(--warning)' : 'var(--danger)'};"></div>
                    </div>
                    <strong>${s.performanceScore}/100</strong>
                </div>
            </td>
            <td>${Utils.renderRatingStars(s.rating)}</td>
            <td>
                <div class="table-actions">
                    <button class="action-btn" title="Edit" onclick="openEditSupplierModal(${s.id})">✎</button>
                    <button class="action-btn delete" title="Delete" onclick="deleteSupplier(${s.id}, '${s.name}')">✕</button>
                </div>
            </td>
        </tr>
    `).join('');
}

function openAddSupplierModal() {
    editingSupplierId = null;
    document.getElementById('supplier-modal-title').textContent = 'Add New Supplier';
    document.getElementById('supp-name').value = '';
    document.getElementById('supp-contact').value = '';
    document.getElementById('supp-email').value = '';
    document.getElementById('supp-phone').value = '';
    document.getElementById('supp-address').value = '';
    document.getElementById('supp-lead-time').value = '7';
    Utils.openModal('supplier-modal');
}

function openEditSupplierModal(id) {
    const s = allSuppliers.find(item => item.id === id);
    if (!s) return;

    editingSupplierId = s.id;
    document.getElementById('supplier-modal-title').textContent = 'Edit Supplier';
    document.getElementById('supp-name').value = s.name;
    document.getElementById('supp-contact').value = s.contactPerson || '';
    document.getElementById('supp-email').value = s.email || '';
    document.getElementById('supp-phone').value = s.phone || '';
    document.getElementById('supp-address').value = s.address || '';
    document.getElementById('supp-lead-time').value = s.leadTimeDays || 7;
    Utils.openModal('supplier-modal');
}

async function saveSupplier() {
    const name = document.getElementById('supp-name').value.trim();
    const contactPerson = document.getElementById('supp-contact').value.trim();
    const email = document.getElementById('supp-email').value.trim();
    const phone = document.getElementById('supp-phone').value.trim();
    const address = document.getElementById('supp-address').value.trim();
    const leadTimeDays = parseInt(document.getElementById('supp-lead-time').value) || 7;

    if (!name) {
        Utils.showToast('Please enter supplier name', 'warning');
        return;
    }

    const payload = {
        name,
        contactPerson,
        email,
        phone,
        address,
        leadTimeDays
    };

    try {
        if (editingSupplierId) {
            await Api.put(`/suppliers/${editingSupplierId}`, payload);
            Utils.showToast('Supplier updated successfully!', 'success');
        } else {
            await Api.post('/suppliers', payload);
            Utils.showToast('Supplier added successfully!', 'success');
        }
        Utils.closeModal('supplier-modal');
        loadSuppliers();
        loadSupplierLossReport();
    } catch (e) {
        Utils.showToast('Failed to save supplier: ' + e.message, 'danger');
    }
}

async function deleteSupplier(id, name) {
    if (!confirm(`Are you sure you want to delete supplier '${name}'?`)) return;
    try {
        await Api.delete(`/suppliers/${id}`);
        Utils.showToast(`Supplier '${name}' deleted`, 'success');
        loadSuppliers();
        loadSupplierLossReport();
    } catch (e) {
        Utils.showToast('Cannot delete supplier: ' + e.message, 'danger');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    initSuppliersPage();
});
