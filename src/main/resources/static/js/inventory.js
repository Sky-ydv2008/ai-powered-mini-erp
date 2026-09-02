/**
 * IntelliERP Inventory & Stock Ledger Controller
 */

let allProducts = [];

async function initInventoryPage() {
    await loadProducts();
    loadInventoryMetrics();
    loadStockLedger(0);
}

async function loadProducts() {
    try {
        allProducts = await Api.get('/products');
        renderInventoryTable(allProducts);

        const select = document.getElementById('adjust-product-select');
        if (select) {
            select.innerHTML = '<option value="">Select Product</option>' +
                allProducts.map(p => `<option value="${p.id}">${p.name} (${p.sku}) - Current Stock: ${p.currentStock}</option>`).join('');
        }
    } catch (e) {
        console.error('Error loading products:', e);
    }
}

async function loadInventoryMetrics() {
    try {
        const metrics = await Api.get('/inventory/overview');
        document.getElementById('inv-total-units').textContent = Utils.formatNumber(metrics.totalStockUnits);
        document.getElementById('inv-total-value').textContent = Utils.formatINR(metrics.totalStockValue);
        document.getElementById('inv-healthy-count').textContent = metrics.healthyProducts;
        document.getElementById('inv-low-count').textContent = metrics.lowStockProducts;
        document.getElementById('inv-critical-count').textContent = metrics.criticalStockProducts;
        document.getElementById('inv-out-count').textContent = metrics.outOfStockProducts;
    } catch (e) {
        console.error('Error loading inventory metrics:', e);
    }
}

function renderInventoryTable(products) {
    const tbody = document.getElementById('inventory-tbody');
    if (!tbody) return;

    if (products.length === 0) {
        tbody.innerHTML = `<tr><td colspan="9" class="empty-state"><div class="empty-icon">📦</div><div class="empty-text">No products in inventory</div></td></tr>`;
        return;
    }

    tbody.innerHTML = products.map(p => `
        <tr>
            <td><strong>${p.name}</strong></td>
            <td><code>${p.sku}</code></td>
            <td><strong>${p.currentStock}</strong> ${p.unit || 'pcs'}</td>
            <td>${p.reorderLevel}</td>
            <td>${p.safetyStock}</td>
            <td>${Utils.formatINR(p.purchasePrice)}</td>
            <td>${Utils.formatINR(p.sellingPrice)}</td>
            <td><strong>${Utils.formatINR(p.stockValue)}</strong></td>
            <td>${Utils.renderStatusBadge(p.status)}</td>
            <td>
                <button class="btn btn-secondary btn-sm" onclick="openStockAdjustModal(${p.id})">Adjust Stock</button>
            </td>
        </tr>
    `).join('');
}

function filterInventory(query) {
    const q = query.toLowerCase();
    const filtered = allProducts.filter(p => p.name.toLowerCase().includes(q) || p.sku.toLowerCase().includes(q));
    renderInventoryTable(filtered);
}

function filterByStatus(status) {
    if (!status) {
        renderInventoryTable(allProducts);
    } else {
        renderInventoryTable(allProducts.filter(p => p.status === status));
    }
}

// Stock Adjustment Modal
function openStockAdjustModal(productId = null) {
    if (productId) {
        document.getElementById('adjust-product-select').value = productId;
    }
    document.getElementById('adjust-quantity').value = '10';
    document.getElementById('adjust-reason').value = 'Physical cycle count adjustment';
    Utils.openModal('adjust-modal');
}

async function submitStockAdjustment() {
    const productId = document.getElementById('adjust-product-select')?.value;
    const type = document.getElementById('adjust-type')?.value || 'ADJUSTMENT';
    const qtyInput = document.getElementById('adjust-quantity')?.value;
    const reason = document.getElementById('adjust-reason')?.value || 'Manual adjustment';

    if (!productId || !qtyInput) {
        Utils.showToast('Please select product and quantity', 'warning');
        return;
    }

    let qty = parseInt(qtyInput);
    if (type === 'DAMAGE' && qty > 0) {
        qty = -qty; // Reduction
    }

    try {
        await Api.post('/inventory/adjust', null, {
            productId,
            quantityAdjustment: qty,
            type,
            reason
        });
        // POST to /inventory/adjust takes query params
        const url = `/inventory/adjust?productId=${productId}&quantityAdjustment=${qty}&type=${type}&reason=${encodeURIComponent(reason)}`;
        await Api.request(url, { method: 'POST' });

        Utils.showToast('Stock adjustment successfully logged!', 'success');
        Utils.closeModal('adjust-modal');
        await loadProducts();
        loadInventoryMetrics();
        loadStockLedger(0);
    } catch (e) {
        Utils.showToast('Adjustment failed: ' + e.message, 'danger');
    }
}

// Stock Movement Ledger
async function loadStockLedger(page = 0) {
    const type = document.getElementById('filter-tx-type')?.value || '';
    try {
        const data = await Api.get('/inventory/ledger', {
            type,
            page,
            size: 15
        });

        const tbody = document.getElementById('ledger-tbody');
        if (!tbody) return;

        if (!data.content || data.content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" class="empty-state"><div class="empty-icon">📜</div><div class="empty-text">No ledger transactions found</div></td></tr>`;
            return;
        }

        tbody.innerHTML = data.content.map(tx => `
            <tr>
                <td>${Utils.formatDateTime(tx.timestamp)}</td>
                <td><strong>${tx.product.name}</strong><br><small class="text-muted">${tx.product.sku}</small></td>
                <td>
                    <span class="badge ${tx.type === 'PURCHASE' ? 'badge-success' : tx.type === 'SALE' ? 'badge-info' : tx.type === 'RETURN' ? 'badge-warning' : 'badge-danger'}">
                        ${tx.type}
                    </span>
                </td>
                <td style="font-weight: 700; color: ${tx.quantity > 0 ? 'var(--success)' : 'var(--danger)'}">
                    ${tx.quantity > 0 ? '+' + tx.quantity : tx.quantity}
                </td>
                <td><strong>${tx.balanceAfter}</strong></td>
                <td><code>${tx.referenceId || '-'}</code></td>
                <td><small>${tx.reason || '-'}</small></td>
            </tr>
        `).join('');

        renderLedgerPagination(data.number, data.totalPages);
    } catch (e) {
        console.error('Error loading stock ledger:', e);
    }
}

function renderLedgerPagination(current, total) {
    const el = document.getElementById('ledger-pagination-ctrls');
    if (!el) return;
    el.innerHTML = `
        <button class="page-btn" ${current === 0 ? 'disabled' : ''} onclick="loadStockLedger(${current - 1})">Previous</button>
        <span style="font-size: 12px; color: var(--text-muted);">Page ${current + 1} of ${total || 1}</span>
        <button class="page-btn" ${current >= total - 1 ? 'disabled' : ''} onclick="loadStockLedger(${current + 1})">Next</button>
    `;
}

document.addEventListener('DOMContentLoaded', () => {
    initInventoryPage();
});
