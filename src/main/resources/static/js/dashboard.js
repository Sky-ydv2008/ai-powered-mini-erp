/**
 * IntelliERP Dashboard Controller
 */

let revProfitChart = null;
let salesExpChart = null;

async function loadDashboard() {
    try {
        const data = await Api.get('/dashboard/overview');

        // 1. Render Top KPI Cards
        document.getElementById('kpi-today-sales').textContent = Utils.formatINR(data.todaySales);
        document.getElementById('kpi-today-profit').textContent = Utils.formatINR(data.todayNetProfit);
        document.getElementById('kpi-today-expenses').textContent = Utils.formatINR(data.todayExpenses);
        document.getElementById('kpi-inventory-value').textContent = Utils.formatINR(data.currentInventoryValue);
        document.getElementById('kpi-low-stock').textContent = data.lowStockCount;
        document.getElementById('kpi-total-customers').textContent = Utils.formatNumber(data.totalCustomers);

        // 2. Render Business Health Gauge & Pillars
        const health = data.businessHealthScore;
        if (health) {
            document.getElementById('health-overall-score').textContent = health.overallScore;
            document.getElementById('health-overall-rating').textContent = `Business Health: ${health.rating}`;
            document.getElementById('gauge-container').style.setProperty('--score-pct', health.overallScore);

            document.getElementById('pillar-profitability').textContent = `${health.profitabilityScore}/100`;
            document.getElementById('pillar-inventory').textContent = `${health.inventoryScore}/100`;
            document.getElementById('pillar-sales').textContent = `${health.salesGrowthScore}/100`;
            document.getElementById('pillar-supplier').textContent = `${health.supplierReliabilityScore}/100`;
            document.getElementById('pillar-customer').textContent = `${health.customerRetentionScore}/100`;
            document.getElementById('pillar-expense').textContent = `${health.expenseControlScore}/100`;
        }

        // 3. Render Today's Business Summary
        const summary = data.todaySummary;
        if (summary) {
            document.getElementById('sum-gross-sales').textContent = Utils.formatINR(summary.grossSales);
            document.getElementById('sum-discounts').textContent = Utils.formatINR(summary.discounts);
            document.getElementById('sum-net-sales').textContent = Utils.formatINR(summary.netSales);
            document.getElementById('sum-orders-count').textContent = summary.salesOrdersCount;
            document.getElementById('sum-units-sold').textContent = summary.productsSoldToday;

            document.getElementById('sum-cogs').textContent = Utils.formatINR(summary.costOfGoodsSold);
            document.getElementById('sum-operating-exp').textContent = Utils.formatINR(summary.operatingExpenses);
            document.getElementById('sum-net-profit').textContent = Utils.formatINR(summary.netProfit);
            document.getElementById('sum-profit-margin').textContent = `${summary.profitMargin}%`;

            document.getElementById('sum-po-count').textContent = summary.purchaseOrdersCount;
            document.getElementById('sum-units-added').textContent = summary.productsAddedToday;
            document.getElementById('sum-total-stock').textContent = summary.currentTotalStock;
        }

        // 4. Render Top Selling & Top Profitable Products
        renderTopProducts(data.topSellingProducts, data.topProfitableProducts);

        // 5. Load Financial Trend Charts
        loadFinancialCharts('this_month');

        // 6. Check High-Priority AI Alert Banner
        loadAiBanner();

    } catch (e) {
        console.error('Error loading dashboard:', e);
        Utils.showToast('Failed to load dashboard data: ' + e.message, 'danger');
    }
}

async function loadFinancialCharts(period) {
    try {
        const pl = await Api.get('/profit-loss', { period });
        if (pl && pl.trend) {
            if (revProfitChart) revProfitChart.destroy();
            if (salesExpChart) salesExpChart.destroy();

            revProfitChart = Charts.createRevenueProfitChart('revenueProfitChart', pl.trend);
            salesExpChart = Charts.createSalesVsExpensesChart('salesExpensesChart', pl.trend);
        }
    } catch (e) {
        console.error('Error loading charts:', e);
    }
}

function renderTopProducts(selling, profitable) {
    const sellTbody = document.getElementById('top-selling-tbody');
    if (sellTbody && selling) {
        sellTbody.innerHTML = selling.map((row, idx) => `
            <tr>
                <td><strong>#${idx + 1}</strong></td>
                <td><strong>${row[1]}</strong><br><small class="text-muted">${row[2]}</small></td>
                <td><span class="badge badge-info">${row[3]} units</span></td>
                <td>${Utils.formatINR(row[4])}</td>
                <td><span class="badge badge-success">${Utils.formatINR(row[5])}</span></td>
            </tr>
        `).join('');
    }

    const profTbody = document.getElementById('top-profitable-tbody');
    if (profTbody && profitable) {
        profTbody.innerHTML = profitable.map((row, idx) => `
            <tr>
                <td><strong>#${idx + 1}</strong></td>
                <td><strong>${row[1]}</strong><br><small class="text-muted">${row[2]}</small></td>
                <td><span class="badge badge-success">${Utils.formatINR(row[5])}</span></td>
                <td>${Utils.formatINR(row[4])}</td>
            </tr>
        `).join('');
    }
}

async function loadAiBanner() {
    try {
        const insights = await Api.get('/ai/insights');
        const banner = document.getElementById('dashboard-ai-banner');
        if (banner && insights && insights.length > 0) {
            const topRisk = insights[0];
            banner.style.display = 'flex';
            document.getElementById('ai-banner-title').textContent = topRisk.title;
            document.getElementById('ai-banner-desc').textContent = `${topRisk.metricSummary} (Estimated Impact: ${Utils.formatINR(topRisk.financialImpact)})`;
            document.getElementById('ai-banner-action-btn').onclick = () => window.location.href = '/ai-insights.html';
        }
    } catch (e) {
        // Ignore
    }
}

document.addEventListener('DOMContentLoaded', () => {
    loadDashboard();
});
