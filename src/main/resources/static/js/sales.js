/**
 * IntelliERP Sales & POS Controller
 */

let allProductsList = [];
let allCustomersList = [];
let posCart = [];

async function initSalesPage() {
    await loadProductsAndCustomers();
    loadSales(0);
}

async function loadProductsAndCustomers() {
    try {
        allProductsList = await Api.get('/products');
        allCustomersList = await Api.get('/customers');

        // Populate customer select in modal
        const custSelect = document.getElementById('pos-customer-select');
        if (custSelect) {
            custSelect.innerHTML = '<option value="">Walk-in Customer</option>' +
                allCustomersList.map(c => `<option value="${c.id}">${c.name} (${c.tier}) - ${c.phone || c.email || ''}</option>`).join('');
        }
    } catch (e) {
        console.error('Error loading metadata:', e);
    }
}

async function loadSales(page = 0) {
    try {
        const customerId = document.getElementById('filter-customer')?.value || '';
        const status = document.getElementById('filter-status')?.value || '';

        const data = await Api.get('/sales/paged', {
            customerId,
            status,
            page,
            size: 15
        });

        const tbody = document.getElementById('sales-tbody');
        if (!tbody) return;

        if (!data.content || data.content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" class="empty-state"><div class="empty-icon">🧾</div><div class="empty-text">No sales records found</div></td></tr>`;
            return;
        }

        tbody.innerHTML = data.content.map(sale => `
            <tr>
                <td><strong>${sale.invoiceNumber}</strong></td>
                <td>${Utils.formatDateTime(sale.saleDate)}</td>
                <td>${sale.customer ? `<strong>${sale.customer.name}</strong>` : 'Walk-in Customer'}</td>
                <td><span class="badge badge-primary">${sale.paymentMethod}</span></td>
                <td>${Utils.renderStatusBadge(sale.status)}</td>
                <td><strong>${Utils.formatINR(sale.totalAmount)}</strong></td>
                <td><span class="badge badge-success">${Utils.formatINR(sale.profit)}</span></td>
                <td>
                    <div class="table-actions">
                        <button class="action-btn" title="View Invoice" onclick="viewInvoice('${sale.invoiceNumber}')">👁</button>
                        ${sale.status === 'COMPLETED' ? `
                            <button class="action-btn delete" title="Cancel Sale" onclick="openCancelModal(${sale.id}, '${sale.invoiceNumber}')">✕</button>
                        ` : ''}
                    </div>
                </td>
            </tr>
        `).join('');

        renderPagination(data.number, data.totalPages);
    } catch (e) {
        console.error('Error loading sales:', e);
        Utils.showToast('Failed to load sales: ' + e.message, 'danger');
    }
}

function renderPagination(current, total) {
    const el = document.getElementById('pagination-ctrls');
    if (!el) return;
    el.innerHTML = `
        <button class="page-btn" ${current === 0 ? 'disabled' : ''} onclick="loadSales(${current - 1})">Previous</button>
        <span style="font-size: 12px; color: var(--text-muted);">Page ${current + 1} of ${total || 1}</span>
        <button class="page-btn" ${current >= total - 1 ? 'disabled' : ''} onclick="loadSales(${current + 1})">Next</button>
    `;
}

// POS Modal Logic
function openPosModal() {
    posCart = [];
    renderCart();
    renderPosProductPicker();
    Utils.openModal('pos-modal');
}

function renderPosProductPicker() {
    const picker = document.getElementById('pos-product-picker');
    if (!picker) return;

    picker.innerHTML = allProductsList.map(p => `
        <div class="card" style="padding: 12px; cursor: pointer; border: 1px solid var(--border-color); ${p.currentStock <= 0 ? 'opacity: 0.5; pointer-events: none;' : ''}" onclick="addToCart(${p.id})">
            <div style="font-weight: 700; font-size: 13.5px;">${p.name}</div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 6px;">
                <span style="color: var(--primary); font-weight: 800;">${Utils.formatINR(p.sellingPrice)}</span>
                <span class="badge ${p.currentStock <= p.reorderLevel ? 'badge-warning' : 'badge-success'}">${p.currentStock} in stock</span>
            </div>
        </div>
    `).join('');
}

function filterPosProducts(query) {
    const q = query.toLowerCase();
    const filtered = allProductsList.filter(p => p.name.toLowerCase().includes(q) || p.sku.toLowerCase().includes(q));
    const picker = document.getElementById('pos-product-picker');
    if (picker) {
        picker.innerHTML = filtered.map(p => `
            <div class="card" style="padding: 12px; cursor: pointer; border: 1px solid var(--border-color); ${p.currentStock <= 0 ? 'opacity: 0.5; pointer-events: none;' : ''}" onclick="addToCart(${p.id})">
                <div style="font-weight: 700; font-size: 13.5px;">${p.name}</div>
                <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 6px;">
                    <span style="color: var(--primary); font-weight: 800;">${Utils.formatINR(p.sellingPrice)}</span>
                    <span class="badge ${p.currentStock <= p.reorderLevel ? 'badge-warning' : 'badge-success'}">${p.currentStock} in stock</span>
                </div>
            </div>
        `).join('');
    }
}

function addToCart(productId) {
    const product = allProductsList.find(p => p.id === productId);
    if (!product) return;

    const existing = posCart.find(item => item.productId === productId);
    if (existing) {
        if (existing.quantity >= product.currentStock) {
            Utils.showToast(`Cannot add more than ${product.currentStock} available units`, 'warning');
            return;
        }
        existing.quantity += 1;
    } else {
        if (product.currentStock <= 0) {
            Utils.showToast('Product is out of stock', 'danger');
            return;
        }
        posCart.push({
            productId: product.id,
            name: product.name,
            sellingPrice: parseFloat(product.sellingPrice),
            quantity: 1,
            discount: 0,
            maxStock: product.currentStock
        });
    }
    renderCart();
}

function updateCartQty(productId, qty) {
    const item = posCart.find(i => i.productId === productId);
    if (!item) return;
    const q = parseInt(qty);
    if (q <= 0) {
        posCart = posCart.filter(i => i.productId !== productId);
    } else if (q > item.maxStock) {
        item.quantity = item.maxStock;
        Utils.showToast(`Max available stock is ${item.maxStock}`, 'warning');
    } else {
        item.quantity = q;
    }
    renderCart();
}

function renderCart() {
    const tbody = document.getElementById('cart-tbody');
    if (!tbody) return;

    if (posCart.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="text-muted" style="text-align: center; padding: 20px;">Cart is empty. Select products on the left.</td></tr>`;
        updateCartTotals();
        return;
    }

    tbody.innerHTML = posCart.map(item => `
        <tr>
            <td><strong>${item.name}</strong></td>
            <td>${Utils.formatINR(item.sellingPrice)}</td>
            <td>
                <input type="number" class="form-control" style="width: 70px; padding: 4px 8px;" value="${item.quantity}" min="1" max="${item.maxStock}" onchange="updateCartQty(${item.productId}, this.value)">
            </td>
            <td><strong>${Utils.formatINR(item.sellingPrice * item.quantity)}</strong></td>
            <td><button class="action-btn delete" onclick="updateCartQty(${item.productId}, 0)">✕</button></td>
        </tr>
    `).join('');

    updateCartTotals();
}

function updateCartTotals() {
    let subtotal = 0;
    posCart.forEach(item => {
        subtotal += item.sellingPrice * item.quantity;
    });

    const discInput = document.getElementById('pos-order-discount');
    const discount = discInput ? parseFloat(discInput.value) || 0 : 0;
    const tax = subtotal * 0.18; // 18% standard GST
    const total = Math.max(0, subtotal + tax - discount);

    document.getElementById('pos-subtotal').textContent = Utils.formatINR(subtotal);
    document.getElementById('pos-tax').textContent = Utils.formatINR(tax);
    document.getElementById('pos-total').textContent = Utils.formatINR(total);
}

async function submitPosSale() {
    if (posCart.length === 0) {
        Utils.showToast('Cart is empty', 'warning');
        return;
    }

    const custId = document.getElementById('pos-customer-select')?.value || null;
    const paymentMethod = document.getElementById('pos-payment-method')?.value || 'CASH';
    const discInput = document.getElementById('pos-order-discount');
    const discount = discInput ? parseFloat(discInput.value) || 0 : 0;

    let subtotal = 0;
    posCart.forEach(item => { subtotal += item.sellingPrice * item.quantity; });
    const tax = subtotal * 0.18;

    const payload = {
        customerId: custId ? parseInt(custId) : null,
        paymentMethod,
        discount,
        tax,
        items: posCart.map(i => ({
            productId: i.productId,
            quantity: i.quantity,
            sellingPrice: i.sellingPrice,
            discount: 0
        }))
    };

    try {
        const sale = await Api.post('/sales', payload);
        Utils.showToast(`Sale Invoice #${sale.invoiceNumber} completed successfully!`, 'success');
        Utils.closeModal('pos-modal');
        await loadProductsAndCustomers(); // Refresh stock
        loadSales(0);
        viewInvoice(sale.invoiceNumber);
    } catch (e) {
        console.error('Sale error:', e);
        Utils.showToast('Sale failed: ' + e.message, 'danger');
    }
}

// Invoice Modal
async function viewInvoice(invoiceNumber) {
    try {
        const sale = await Api.get(`/sales/invoice/${invoiceNumber}`);
        document.getElementById('inv-number').textContent = sale.invoiceNumber;
        document.getElementById('inv-date').textContent = Utils.formatDateTime(sale.saleDate);
        document.getElementById('inv-customer').textContent = sale.customer ? `${sale.customer.name} (${sale.customer.phone || sale.customer.email || ''})` : 'Walk-in Customer';
        document.getElementById('inv-status').innerHTML = Utils.renderStatusBadge(sale.status);
        document.getElementById('inv-payment').textContent = sale.paymentMethod;

        const tbody = document.getElementById('inv-items-tbody');
        tbody.innerHTML = sale.items.map(item => `
            <tr>
                <td><strong>${item.product.name}</strong><br><small class="text-muted">${item.product.sku}</small></td>
                <td>${item.quantity}</td>
                <td>${Utils.formatINR(item.sellingPrice)}</td>
                <td>${Utils.formatINR(item.totalPrice)}</td>
            </tr>
        `).join('');

        document.getElementById('inv-subtotal').textContent = Utils.formatINR(sale.subtotal);
        document.getElementById('inv-discount').textContent = Utils.formatINR(sale.discount);
        document.getElementById('inv-tax').textContent = Utils.formatINR(sale.tax);
        document.getElementById('inv-total').textContent = Utils.formatINR(sale.totalAmount);
        document.getElementById('inv-profit').textContent = Utils.formatINR(sale.profit);

        Utils.openModal('invoice-modal');
    } catch (e) {
        Utils.showToast('Failed to load invoice: ' + e.message, 'danger');
    }
}

// Cancel Modal
let activeCancelId = null;
function openCancelModal(id, inv) {
    activeCancelId = id;
    document.getElementById('cancel-sale-inv').textContent = inv;
    Utils.openModal('cancel-modal');
}

async function confirmCancelSale() {
    if (!activeCancelId) return;
    const reason = document.getElementById('cancel-reason').value || 'Customer requested cancellation';
    try {
        await Api.post(`/sales/${activeCancelId}/cancel`, { reason });
        Utils.showToast('Sale cancelled and inventory restored!', 'success');
        Utils.closeModal('cancel-modal');
        loadSales(0);
    } catch (e) {
        Utils.showToast('Cancel failed: ' + e.message, 'danger');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    initSalesPage();
});
