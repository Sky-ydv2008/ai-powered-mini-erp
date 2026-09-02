/**
 * IntelliERP Products & Catalog Controller
 */

let allProducts = [];
let allCategories = [];
let allSuppliers = [];
let editingProductId = null;

async function initProductsPage() {
    await loadMetadata();
    loadProducts();
}

async function loadMetadata() {
    try {
        allCategories = await Api.get('/categories');
        allSuppliers = await Api.get('/suppliers');

        const catFilter = document.getElementById('filter-category');
        const catSelect = document.getElementById('prod-category');
        const suppSelect = document.getElementById('prod-supplier');

        if (catFilter) {
            catFilter.innerHTML = '<option value="">All Categories</option>' +
                allCategories.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
        }
        if (catSelect) {
            catSelect.innerHTML = '<option value="">Select Category</option>' +
                allCategories.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
        }
        if (suppSelect) {
            suppSelect.innerHTML = '<option value="">Select Preferred Supplier</option>' +
                allSuppliers.map(s => `<option value="${s.id}">${s.name}</option>`).join('');
        }
    } catch (e) {
        console.error('Error loading product metadata:', e);
    }
}

async function loadProducts() {
    try {
        allProducts = await Api.get('/products');
        renderProductsTable(allProducts);
    } catch (e) {
        console.error('Error loading products:', e);
        Utils.showToast('Failed to load products: ' + e.message, 'danger');
    }
}

function renderProductsTable(products) {
    const tbody = document.getElementById('products-tbody');
    if (!tbody) return;

    if (products.length === 0) {
        tbody.innerHTML = `<tr><td colspan="9" class="empty-state"><div class="empty-icon">🏷️</div><div class="empty-text">No products match your criteria</div></td></tr>`;
        return;
    }

    tbody.innerHTML = products.map(p => `
        <tr>
            <td>
                <strong>${p.name}</strong><br>
                <small class="text-muted">${p.category ? p.category.name : 'Uncategorized'}</small>
            </td>
            <td><code>${p.sku}</code></td>
            <td><strong>${p.currentStock}</strong> ${p.unit || 'pcs'}</td>
            <td>${Utils.formatINR(p.purchasePrice)}</td>
            <td><strong>${Utils.formatINR(p.sellingPrice)}</strong></td>
            <td><span class="badge badge-success">${p.profitMargin}%</span></td>
            <td>${Utils.renderStatusBadge(p.status)}</td>
            <td>${Utils.renderBcgBadge(p.bcgClassification)}</td>
            <td>
                <div class="table-actions">
                    <button class="action-btn" title="Edit Product" onclick="openEditProductModal(${p.id})">✎</button>
                    <button class="action-btn delete" title="Delete Product" onclick="deleteProduct(${p.id}, '${p.name}')">✕</button>
                </div>
            </td>
        </tr>
    `).join('');
}

function filterProducts() {
    const q = document.getElementById('search-products')?.value.toLowerCase() || '';
    const catId = document.getElementById('filter-category')?.value || '';
    const bcg = document.getElementById('filter-bcg')?.value || '';

    const filtered = allProducts.filter(p => {
        const matchesQuery = p.name.toLowerCase().includes(q) || p.sku.toLowerCase().includes(q);
        const matchesCat = !catId || (p.category && p.category.id === parseInt(catId));
        const matchesBcg = !bcg || p.bcgClassification === bcg;
        return matchesQuery && matchesCat && matchesBcg;
    });

    renderProductsTable(filtered);
}

function openAddProductModal() {
    editingProductId = null;
    document.getElementById('product-modal-title').textContent = 'Add New Product';
    document.getElementById('prod-name').value = '';
    document.getElementById('prod-sku').value = 'SKU-' + (Math.floor(Math.random() * 90000) + 10000);
    document.getElementById('prod-purchase-price').value = '100';
    document.getElementById('prod-selling-price').value = '180';
    document.getElementById('prod-stock').value = '50';
    document.getElementById('prod-reorder').value = '20';
    document.getElementById('prod-safety').value = '10';
    document.getElementById('prod-unit').value = 'pcs';
    Utils.openModal('product-modal');
}

function openEditProductModal(id) {
    const p = allProducts.find(item => item.id === id);
    if (!p) return;

    editingProductId = p.id;
    document.getElementById('product-modal-title').textContent = 'Edit Product';
    document.getElementById('prod-name').value = p.name;
    document.getElementById('prod-sku').value = p.sku;
    document.getElementById('prod-category').value = p.category ? p.category.id : '';
    document.getElementById('prod-supplier').value = p.preferredSupplier ? p.preferredSupplier.id : '';
    document.getElementById('prod-purchase-price').value = p.purchasePrice;
    document.getElementById('prod-selling-price').value = p.sellingPrice;
    document.getElementById('prod-stock').value = p.currentStock;
    document.getElementById('prod-reorder').value = p.reorderLevel;
    document.getElementById('prod-safety').value = p.safetyStock;
    document.getElementById('prod-unit').value = p.unit || 'pcs';
    document.getElementById('prod-bcg').value = p.bcgClassification || 'CASH_COW';
    Utils.openModal('product-modal');
}

async function saveProduct() {
    const name = document.getElementById('prod-name').value.trim();
    const sku = document.getElementById('prod-sku').value.trim();
    const catId = document.getElementById('prod-category').value;
    const suppId = document.getElementById('prod-supplier').value;
    const purchasePrice = parseFloat(document.getElementById('prod-purchase-price').value);
    const sellingPrice = parseFloat(document.getElementById('prod-selling-price').value);
    const currentStock = parseInt(document.getElementById('prod-stock').value);
    const reorderLevel = parseInt(document.getElementById('prod-reorder').value);
    const safetyStock = parseInt(document.getElementById('prod-safety').value);
    const unit = document.getElementById('prod-unit').value;
    const bcg = document.getElementById('prod-bcg')?.value || 'CASH_COW';

    if (!name || !sku || isNaN(purchasePrice) || isNaN(sellingPrice)) {
        Utils.showToast('Please fill all required fields correctly', 'warning');
        return;
    }

    const payload = {
        name,
        sku,
        purchasePrice,
        sellingPrice,
        currentStock,
        reorderLevel,
        safetyStock,
        unit,
        bcgClassification: bcg,
        category: catId ? { id: parseInt(catId) } : null,
        preferredSupplier: suppId ? { id: parseInt(suppId) } : null
    };

    try {
        if (editingProductId) {
            await Api.put(`/products/${editingProductId}`, payload);
            Utils.showToast('Product updated successfully!', 'success');
        } else {
            await Api.post('/products', payload);
            Utils.showToast('Product added successfully!', 'success');
        }
        Utils.closeModal('product-modal');
        loadProducts();
    } catch (e) {
        Utils.showToast('Failed to save product: ' + e.message, 'danger');
    }
}

async function deleteProduct(id, name) {
    if (!confirm(`Are you sure you want to delete product '${name}'?`)) {
        return;
    }
    try {
        await Api.delete(`/products/${id}`);
        Utils.showToast(`Product '${name}' deleted`, 'success');
        loadProducts();
    } catch (e) {
        Utils.showToast('Cannot delete: ' + e.message, 'danger');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    initProductsPage();
});
