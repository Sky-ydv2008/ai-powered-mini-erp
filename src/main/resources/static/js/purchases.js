/**
 * IntelliERP Purchases & Procurement Controller
 */

let allSuppliers = [];
let allProducts = [];
let poLineItems = [];

async function initPurchasesPage() {
    await loadMetadata();
    loadPurchases(0);
}

async function loadMetadata() {
    try {
        allSuppliers = await Api.get('/suppliers');
        allProducts = await Api.get('/products');

        const suppSelect = document.getElementById('po-supplier-select');
        const filterSupp = document.getElementById('filter-supplier');

        const options = allSuppliers.map(s => `<option value="${s.id}">${s.name} (Lead: ${s.leadTimeDays}d)</option>`).join('');
        if (suppSelect) suppSelect.innerHTML = `<option value="">Select Supplier</option>` + options;
        if (filterSupp) filterSupp.innerHTML = `<option value="">All Suppliers</option>` + options;
    } catch (e) {
        console.error('Error loading metadata:', e);
    }
}

async function loadPurchases(page = 0) {
    try {
        const supplierId = document.getElementById('filter-supplier')?.value || '';
        const status = document.getElementById('filter-status')?.value || '';

        const data = await Api.get('/purchases/paged', {
            supplierId,
            status,
            page,
            size: 15
        });

        const tbody = document.getElementById('purchases-tbody');
        if (!tbody) return;

        if (!data.content || data.content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" class="empty-state"><div class="empty-icon">📦</div><div class="empty-text">No purchase orders found</div></td></tr>`;
            return;
        }

        tbody.innerHTML = data.content.map(po => `
            <tr>
                <td><strong>${po.orderNumber}</strong></td>
                <td>${Utils.formatDate(po.orderDate)}</td>
                <td><strong>${po.supplier.name}</strong></td>
                <td>${po.items.length} items</td>
                <td>${Utils.formatDate(po.expectedDeliveryDate)}</td>
                <td>${Utils.renderStatusBadge(po.status)}</td>
                <td><strong>${Utils.formatINR(po.totalCost)}</strong></td>
                <td>
                    <div class="table-actions">
                        ${po.status === 'ORDERED' || po.status === 'PENDING' ? `
                            <button class="btn btn-success btn-sm" onclick="receivePurchaseOrder(${po.id}, '${po.orderNumber}')">✓ Receive Stock</button>
                        ` : `<span class="badge badge-success">Received</span>`}
                    </div>
                </td>
            </tr>
        `).join('');

        renderPagination(data.number, data.totalPages);
    } catch (e) {
        console.error('Error loading purchases:', e);
        Utils.showToast('Failed to load purchases: ' + e.message, 'danger');
    }
}

function renderPagination(current, total) {
    const el = document.getElementById('pagination-ctrls');
    if (!el) return;
    el.innerHTML = `
        <button class="page-btn" ${current === 0 ? 'disabled' : ''} onclick="loadPurchases(${current - 1})">Previous</button>
        <span style="font-size: 12px; color: var(--text-muted);">Page ${current + 1} of ${total || 1}</span>
        <button class="page-btn" ${current >= total - 1 ? 'disabled' : ''} onclick="loadPurchases(${current + 1})">Next</button>
    `;
}

function openAddPoModal() {
    poLineItems = [{ productId: allProducts[0]?.id, quantity: 50, unitPrice: allProducts[0]?.purchasePrice || 0 }];
    renderPoItems();
    Utils.openModal('po-modal');
}

function addPoItemRow() {
    poLineItems.push({ productId: allProducts[0]?.id, quantity: 20, unitPrice: allProducts[0]?.purchasePrice || 0 });
    renderPoItems();
}

function removePoItemRow(index) {
    poLineItems.splice(index, 1);
    renderPoItems();
}

function renderPoItems() {
    const tbody = document.getElementById('po-items-tbody');
    if (!tbody) return;

    tbody.innerHTML = poLineItems.map((item, idx) => `
        <tr>
            <td>
                <select class="form-control" onchange="onPoProductSelect(${idx}, this.value)">
                    ${allProducts.map(p => `<option value="${p.id}" ${p.id === item.productId ? 'selected' : ''}>${p.name} (${p.sku})</option>`).join('')}
                </select>
            </td>
            <td>
                <input type="number" class="form-control" min="1" value="${item.quantity}" onchange="poLineItems[${idx}].quantity = parseInt(this.value); updatePoTotals();">
            </td>
            <td>
                <input type="number" step="0.01" class="form-control" value="${item.unitPrice}" onchange="poLineItems[${idx}].unitPrice = parseFloat(this.value); updatePoTotals();">
            </td>
            <td>
                <strong>${Utils.formatINR(item.quantity * item.unitPrice)}</strong>
            </td>
            <td>
                <button class="action-btn delete" onclick="removePoItemRow(${idx})">✕</button>
            </td>
        </tr>
    `).join('');

    updatePoTotals();
}

function onPoProductSelect(idx, productId) {
    const p = allProducts.find(prod => prod.id === parseInt(productId));
    if (p) {
        poLineItems[idx].productId = p.id;
        poLineItems[idx].unitPrice = parseFloat(p.purchasePrice);
        renderPoItems();
    }
}

function updatePoTotals() {
    let total = 0;
    poLineItems.forEach(i => { total += i.quantity * i.unitPrice; });
    document.getElementById('po-modal-total').textContent = Utils.formatINR(total);
}

async function submitPurchaseOrder() {
    const supplierId = document.getElementById('po-supplier-select')?.value;
    if (!supplierId) {
        Utils.showToast('Please select a supplier', 'warning');
        return;
    }

    if (poLineItems.length === 0) {
        Utils.showToast('Please add at least one line item', 'warning');
        return;
    }

    const payload = {
        supplierId: parseInt(supplierId),
        orderDate: document.getElementById('po-order-date')?.value || new Date().toISOString().split('T')[0],
        notes: document.getElementById('po-notes')?.value || '',
        items: poLineItems.map(i => ({
            productId: i.productId,
            quantity: i.quantity,
            unitPrice: i.unitPrice
        }))
    };

    try {
        const po = await Api.post('/purchases', payload);
        Utils.showToast(`Purchase Order ${po.orderNumber} created!`, 'success');
        Utils.closeModal('po-modal');
        loadPurchases(0);
    } catch (e) {
        Utils.showToast('Failed to create purchase order: ' + e.message, 'danger');
    }
}

async function receivePurchaseOrder(id, orderNumber) {
    if (!confirm(`Confirm receipt for ${orderNumber}? This will automatically increase warehouse inventory.`)) {
        return;
    }

    try {
        await Api.patch(`/purchases/${id}/status`, { status: 'RECEIVED' });
        Utils.showToast(`Purchase Order ${orderNumber} received! Inventory stock updated.`, 'success');
        loadPurchases(0);
    } catch (e) {
        Utils.showToast('Failed to receive purchase order: ' + e.message, 'danger');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    initPurchasesPage();
});
