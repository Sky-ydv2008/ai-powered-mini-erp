/**
 * IntelliERP Utilities & Helpers
 */

const Utils = {
    formatINR(value) {
        if (value === null || value === undefined) return '₹0.00';
        const num = typeof value === 'string' ? parseFloat(value) : value;
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
            maximumFractionDigits: 2
        }).format(num);
    },

    formatNumber(value) {
        if (value === null || value === undefined) return '0';
        return new Intl.NumberFormat('en-IN').format(value);
    },

    formatDate(dateStr) {
        if (!dateStr) return '-';
        const d = new Date(dateStr);
        return d.toLocaleDateString('en-IN', {
            day: '2-digit',
            month: 'short',
            year: 'numeric'
        });
    },

    formatDateTime(dateStr) {
        if (!dateStr) return '-';
        const d = new Date(dateStr);
        return d.toLocaleDateString('en-IN', {
            day: '2-digit',
            month: 'short',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    },

    showToast(message, type = 'info') {
        let container = document.getElementById('toast-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toast-container';
            document.body.appendChild(container);
        }

        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;

        const iconMap = {
            success: '✓',
            danger: '✕',
            warning: '⚠',
            info: 'ℹ'
        };

        toast.innerHTML = `
            <span style="font-weight: bold; font-size: 16px;">${iconMap[type] || 'ℹ'}</span>
            <span>${message}</span>
        `;

        container.appendChild(toast);

        setTimeout(() => {
            toast.style.animation = 'slideIn 0.3s ease reverse';
            setTimeout(() => toast.remove(), 300);
        }, 4000);
    },

    openModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.add('active');
        }
    },

    closeModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.remove('active');
        }
    },

    renderStatusBadge(status) {
        if (!status) return '';
        const map = {
            'HEALTHY': 'badge-success',
            'LOW_STOCK': 'badge-warning',
            'CRITICAL': 'badge-danger',
            'OUT_OF_STOCK': 'badge-danger',
            'COMPLETED': 'badge-success',
            'ORDERED': 'badge-info',
            'RECEIVED': 'badge-success',
            'PENDING': 'badge-warning',
            'CANCELLED': 'badge-danger',
            'REFUNDED': 'badge-info'
        };
        const cls = map[status] || 'badge-primary';
        return `<span class="badge ${cls}">${status.replace(/_/g, ' ')}</span>`;
    },

    renderBcgBadge(bcg) {
        if (!bcg) return '';
        const map = {
            'STAR': { cls: 'badge-success', text: '★ STAR' },
            'CASH_COW': { cls: 'badge-info', text: '⛁ CASH COW' },
            'QUESTION_MARK': { cls: 'badge-warning', text: '? QUESTION MARK' },
            'LOSS_MAKER': { cls: 'badge-danger', text: '▼ LOSS MAKER' },
            'DEAD_STOCK': { cls: 'badge-danger', text: '✕ DEAD STOCK' }
        };
        const item = map[bcg] || { cls: 'badge-primary', text: bcg };
        return `<span class="badge ${item.cls}">${item.text}</span>`;
    },

    renderSeverityBadge(severity) {
        if (!severity) return '';
        const map = {
            'CRITICAL': { cls: 'badge-danger', icon: '🔴' },
            'WARNING': { cls: 'badge-warning', icon: '🟠' },
            'ATTENTION': { cls: 'badge-warning', icon: '🟡' },
            'OPPORTUNITY': { cls: 'badge-success', icon: '🟢' },
            'INFO': { cls: 'badge-info', icon: '🔵' }
        };
        const item = map[severity] || { cls: 'badge-primary', icon: '⚪' };
        return `<span class="badge ${item.cls}">${item.icon} ${severity}</span>`;
    },

    renderRatingStars(rating) {
        const r = parseFloat(rating) || 0;
        const fullStars = Math.floor(r);
        const halfStar = r % 1 >= 0.5 ? 1 : 0;
        const emptyStars = 5 - fullStars - halfStar;

        return `<span style="color: #f59e0b; font-size: 14px; letter-spacing: 2px;">` +
            '★'.repeat(fullStars) +
            (halfStar ? '½' : '') +
            '☆'.repeat(emptyStars) +
            `</span> <span style="font-weight: 700; color: #fff; font-size: 12px;">(${r.toFixed(1)})</span>`;
    },

    exportCsvFile(filename, csvContent) {
        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        const url = URL.createObjectURL(blob);
        link.setAttribute('href', url);
        link.setAttribute('download', filename);
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }
};
