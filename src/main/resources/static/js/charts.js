/**
 * IntelliERP Chart.js Configuration & Visualizations
 */

const Charts = {
    commonOptions: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                labels: {
                    color: '#9ca3af',
                    font: { family: 'Plus Jakarta Sans', size: 12, weight: '500' }
                }
            },
            tooltip: {
                backgroundColor: '#1f2937',
                titleColor: '#f9fafb',
                bodyColor: '#e5e7eb',
                borderColor: 'rgba(255, 255, 255, 0.1)',
                borderWidth: 1,
                padding: 12,
                cornerRadius: 8,
                titleFont: { family: 'Plus Jakarta Sans', weight: '600' },
                bodyFont: { family: 'Plus Jakarta Sans' }
            }
        },
        scales: {
            x: {
                grid: { color: 'rgba(255, 255, 255, 0.05)' },
                ticks: { color: '#9ca3af', font: { family: 'Plus Jakarta Sans', size: 11 } }
            },
            y: {
                grid: { color: 'rgba(255, 255, 255, 0.05)' },
                ticks: {
                    color: '#9ca3af',
                    font: { family: 'Plus Jakarta Sans', size: 11 },
                    callback: function(value) {
                        if (value >= 100000) return '₹' + (value / 100000).toFixed(1) + 'L';
                        if (value >= 1000) return '₹' + (value / 1000).toFixed(0) + 'k';
                        return '₹' + value;
                    }
                }
            }
        }
    },

    createRevenueProfitChart(canvasId, trendData) {
        const ctx = document.getElementById(canvasId);
        if (!ctx) return null;

        const labels = trendData.map(d => d.date);
        const revenues = trendData.map(d => d.revenue);
        const profits = trendData.map(d => d.netProfit);

        const gradientRev = ctx.getContext('2d').createLinearGradient(0, 0, 0, 300);
        gradientRev.addColorStop(0, 'rgba(99, 102, 241, 0.4)');
        gradientRev.addColorStop(1, 'rgba(99, 102, 241, 0.0)');

        const gradientProf = ctx.getContext('2d').createLinearGradient(0, 0, 0, 300);
        gradientProf.addColorStop(0, 'rgba(16, 185, 129, 0.4)');
        gradientProf.addColorStop(1, 'rgba(16, 185, 129, 0.0)');

        return new Chart(ctx, {
            type: 'line',
            data: {
                labels,
                datasets: [
                    {
                        label: 'Revenue (₹)',
                        data: revenues,
                        borderColor: '#6366f1',
                        backgroundColor: gradientRev,
                        fill: true,
                        tension: 0.35,
                        borderWidth: 2,
                        pointRadius: 3,
                        pointHoverRadius: 6
                    },
                    {
                        label: 'Net Profit (₹)',
                        data: profits,
                        borderColor: '#10b981',
                        backgroundColor: gradientProf,
                        fill: true,
                        tension: 0.35,
                        borderWidth: 2,
                        pointRadius: 3,
                        pointHoverRadius: 6
                    }
                ]
            },
            options: this.commonOptions
        });
    },

    createSalesVsExpensesChart(canvasId, trendData) {
        const ctx = document.getElementById(canvasId);
        if (!ctx) return null;

        const labels = trendData.map(d => d.date);
        const revenues = trendData.map(d => d.revenue);
        const expenses = trendData.map(d => d.expenses);

        return new Chart(ctx, {
            type: 'bar',
            data: {
                labels,
                datasets: [
                    {
                        label: 'Sales Revenue',
                        data: revenues,
                        backgroundColor: '#6366f1',
                        borderRadius: 4
                    },
                    {
                        label: 'Operating Expenses',
                        data: expenses,
                        backgroundColor: '#f59e0b',
                        borderRadius: 4
                    }
                ]
            },
            options: this.commonOptions
        });
    },

    createDoughnutChart(canvasId, labels, data, colors) {
        const ctx = document.getElementById(canvasId);
        if (!ctx) return null;

        return new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels,
                datasets: [{
                    data,
                    backgroundColor: colors || ['#6366f1', '#10b981', '#f59e0b', '#ef4444', '#0ea5e9', '#8b5cf6'],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '70%',
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: { color: '#9ca3af', font: { family: 'Plus Jakarta Sans', size: 11 }, padding: 12 }
                    }
                }
            }
        });
    }
};
