/**
 * IntelliERP Expenses Controller
 */

let expDoughnutChart = null;

async function initExpensesPage() {
    loadExpenses(0);
    loadExpenseBreakdown();
}

async function loadExpenses(page = 0) {
    const category = document.getElementById('filter-category')?.value || '';
    try {
        const data = await Api.get('/expenses/paged', {
            category,
            page,
            size: 15
        });

        const tbody = document.getElementById('expenses-tbody');
        if (!tbody) return;

        if (!data.content || data.content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="empty-state"><div class="empty-icon">💸</div><div class="empty-text">No expense records found</div></td></tr>`;
            return;
        }

        tbody.innerHTML = data.content.map(exp => `
            <tr>
                <td>${Utils.formatDate(exp.expenseDate)}</td>
                <td><span class="badge badge-warning">${exp.category}</span></td>
                <td><strong>${Utils.formatINR(exp.amount)}</strong></td>
                <td>${exp.description || '-'}</td>
                <td><span class="badge badge-primary">${exp.paymentMethod || 'BANK_TRANSFER'}</span></td>
                <td>
                    <button class="action-btn delete" title="Delete Expense" onclick="deleteExpense(${exp.id})">✕</button>
                </td>
            </tr>
        `).join('');

        renderPagination(data.number, data.totalPages);
    } catch (e) {
        console.error('Error loading expenses:', e);
        Utils.showToast('Failed to load expenses: ' + e.message, 'danger');
    }
}

async function loadExpenseBreakdown() {
    try {
        const breakdown = await Api.get('/expenses/breakdown');
        if (breakdown && breakdown.length > 0) {
            const labels = breakdown.map(b => b.category);
            const data = breakdown.map(b => b.amount);

            if (expDoughnutChart) expDoughnutChart.destroy();
            expDoughnutChart = Charts.createDoughnutChart('expenseBreakdownChart', labels, data);

            let total = 0;
            breakdown.forEach(b => { total += parseFloat(b.amount) || 0; });
            document.getElementById('total-monthly-expense').textContent = Utils.formatINR(total);
        }
    } catch (e) {
        console.error('Error loading expense breakdown:', e);
    }
}

function renderPagination(current, total) {
    const el = document.getElementById('pagination-ctrls');
    if (!el) return;
    el.innerHTML = `
        <button class="page-btn" ${current === 0 ? 'disabled' : ''} onclick="loadExpenses(${current - 1})">Previous</button>
        <span style="font-size: 12px; color: var(--text-muted);">Page ${current + 1} of ${total || 1}</span>
        <button class="page-btn" ${current >= total - 1 ? 'disabled' : ''} onclick="loadExpenses(${current + 1})">Next</button>
    `;
}

function openAddExpenseModal() {
    document.getElementById('exp-category').value = 'SALARY';
    document.getElementById('exp-amount').value = '';
    document.getElementById('exp-date').value = new Date().toISOString().split('T')[0];
    document.getElementById('exp-desc').value = '';
    document.getElementById('exp-payment').value = 'BANK_TRANSFER';
    Utils.openModal('expense-modal');
}

async function submitExpense() {
    const category = document.getElementById('exp-category')?.value;
    const amount = parseFloat(document.getElementById('exp-amount')?.value);
    const expenseDate = document.getElementById('exp-date')?.value || new Date().toISOString().split('T')[0];
    const description = document.getElementById('exp-desc')?.value;
    const paymentMethod = document.getElementById('exp-payment')?.value;

    if (!category || isNaN(amount) || amount <= 0) {
        Utils.showToast('Please enter a valid expense amount and category', 'warning');
        return;
    }

    try {
        await Api.post('/expenses', {
            category,
            amount,
            expenseDate,
            description,
            paymentMethod
        });
        Utils.showToast('Expense recorded successfully!', 'success');
        Utils.closeModal('expense-modal');
        loadExpenses(0);
        loadExpenseBreakdown();
    } catch (e) {
        Utils.showToast('Failed to record expense: ' + e.message, 'danger');
    }
}

async function deleteExpense(id) {
    if (!confirm('Are you sure you want to delete this expense record?')) return;
    try {
        await Api.delete(`/expenses/${id}`);
        Utils.showToast('Expense deleted', 'success');
        loadExpenses(0);
        loadExpenseBreakdown();
    } catch (e) {
        Utils.showToast('Cannot delete expense: ' + e.message, 'danger');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    initExpensesPage();
});
