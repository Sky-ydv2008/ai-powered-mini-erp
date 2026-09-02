/**
 * IntelliERP Reports & Profit/Loss Center Controller
 */

let reportTrendChart = null;

async function initReportsPage() {
    loadProfitLoss('this_month');
}

async function loadProfitLoss(period = 'this_month') {
    const customStart = document.getElementById('report-start-date')?.value || '';
    const customEnd = document.getElementById('report-end-date')?.value || '';

    try {
        const data = await Api.get('/profit-loss', {
            period,
            startDate: period === 'custom' ? customStart : undefined,
            endDate: period === 'custom' ? customEnd : undefined
        });

        // 1. Render Metrics
        document.getElementById('pl-revenue').textContent = Utils.formatINR(data.revenue);
        document.getElementById('pl-cogs').textContent = Utils.formatINR(data.costOfGoodsSold);
        document.getElementById('pl-gross-profit').textContent = Utils.formatINR(data.grossProfit);
        document.getElementById('pl-gross-margin').textContent = `${data.grossMargin}%`;
        document.getElementById('pl-expenses').textContent = Utils.formatINR(data.operatingExpenses);
        document.getElementById('pl-net-profit').textContent = Utils.formatINR(data.netProfit);
        document.getElementById('pl-profit-margin').textContent = `${data.profitMargin}%`;
        document.getElementById('pl-sales-count').textContent = data.salesCount;
        document.getElementById('pl-discounts').textContent = Utils.formatINR(data.discounts);
        document.getElementById('pl-refunds').textContent = Utils.formatINR(data.refunds);

        // 2. Render Financial Performance Trend Chart
        if (data.trend) {
            if (reportTrendChart) reportTrendChart.destroy();
            reportTrendChart = Charts.createRevenueProfitChart('reportTrendChart', data.trend);
        }

        // Highlight active period button
        document.querySelectorAll('.period-btn').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.period === period);
        });

    } catch (e) {
        console.error('Error loading report:', e);
        Utils.showToast('Failed to load report: ' + e.message, 'danger');
    }
}

function onPeriodChange(period) {
    const customRow = document.getElementById('custom-date-row');
    if (period === 'custom') {
        customRow.style.display = 'flex';
    } else {
        customRow.style.display = 'none';
        loadProfitLoss(period);
    }
}

// CSV Exports
async function exportReportCsv(type) {
    try {
        let endpoint = '';
        let filename = '';

        if (type === 'sales') {
            endpoint = '/reports/sales/csv';
            filename = 'sales_report.csv';
        } else if (type === 'inventory') {
            endpoint = '/reports/inventory/csv';
            filename = 'inventory_report.csv';
        } else if (type === 'suppliers') {
            endpoint = '/reports/suppliers/csv';
            filename = 'supplier_performance_report.csv';
        } else if (type === 'profit-loss') {
            const activePeriod = document.querySelector('.period-btn.active')?.dataset.period || 'this_month';
            endpoint = `/reports/profit-loss/csv?period=${activePeriod}`;
            filename = `profit_loss_${activePeriod}.csv`;
        }

        const csv = await Api.request(endpoint);
        Utils.exportCsvFile(filename, csv);
        Utils.showToast(`Exported ${filename} successfully!`, 'success');
    } catch (e) {
        Utils.showToast('CSV export failed: ' + e.message, 'danger');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    initReportsPage();
});
