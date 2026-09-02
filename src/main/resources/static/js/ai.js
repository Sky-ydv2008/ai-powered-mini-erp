/**
 * IntelliERP AI Insights & Decision Intelligence Controller
 */

let allInsights = [];
let activeInsight = null;

async function initAiPage() {
    loadAiInsights();
}

async function loadAiInsights() {
    try {
        allInsights = await Api.get('/ai/insights');
        renderInsightsDeck(allInsights);
        updateCounts(allInsights);
    } catch (e) {
        console.error('Error loading AI insights:', e);
        Utils.showToast('Failed to load AI insights: ' + e.message, 'danger');
    }
}

function updateCounts(insights) {
    document.getElementById('count-all').textContent = insights.length;
    document.getElementById('count-critical').textContent = insights.filter(i => i.severity === 'CRITICAL').length;
    document.getElementById('count-warning').textContent = insights.filter(i => i.severity === 'WARNING').length;
    document.getElementById('count-opportunity').textContent = insights.filter(i => i.severity === 'OPPORTUNITY').length;
}

function renderInsightsDeck(insights) {
    const container = document.getElementById('ai-insights-deck');
    if (!container) return;

    if (insights.length === 0) {
        container.innerHTML = `
            <div class="empty-state" style="grid-column: 1 / -1;">
                <div class="empty-icon">✨</div>
                <div class="empty-text">No active anomalies or risks detected</div>
                <button class="btn btn-primary btn-sm" onclick="runAiAnalysis()">Run Engine Now</button>
            </div>
        `;
        return;
    }

    container.innerHTML = insights.map(item => `
        <div class="card" style="border-left: 4px solid ${getSeverityColor(item.severity)}; cursor: pointer;" onclick="openInsightDetail(${item.id})">
            <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px;">
                ${Utils.renderSeverityBadge(item.severity)}
                <span style="font-size: 11.5px; color: var(--text-muted); font-weight: 600;">
                    ${item.confidenceScore ? `Confidence: ${item.confidenceScore}%` : ''}
                </span>
            </div>
            <h3 style="font-size: 15.5px; font-weight: 700; color: #fff; margin-bottom: 8px;">${item.title}</h3>
            <p style="font-size: 13px; color: var(--text-secondary); margin-bottom: 16px; line-height: 1.5;">${item.metricSummary}</p>
            
            <div style="background: rgba(255, 255, 255, 0.03); border-radius: var(--radius-sm); padding: 12px; margin-bottom: 16px; display: flex; justify-content: space-between; align-items: center;">
                <span style="font-size: 12px; color: var(--text-muted);">Projected Financial Impact:</span>
                <strong style="color: ${item.severity === 'OPPORTUNITY' ? 'var(--success)' : 'var(--danger)'}; font-size: 15px;">
                    ${item.severity === 'OPPORTUNITY' ? '+' : '-'}${Utils.formatINR(item.financialImpact)}
                </strong>
            </div>

            <div style="display: flex; align-items: center; justify-content: space-between; border-top: 1px solid var(--border-color); padding-top: 12px;">
                <span style="font-size: 12px; color: var(--primary); font-weight: 600;">View Root Cause & Action →</span>
                <small class="text-muted">${Utils.formatDate(item.createdAt)}</small>
            </div>
        </div>
    `).join('');
}

function getSeverityColor(sev) {
    const map = {
        'CRITICAL': '#ef4444',
        'WARNING': '#f59e0b',
        'ATTENTION': '#f59e0b',
        'OPPORTUNITY': '#10b981',
        'INFO': '#0ea5e9'
    };
    return map[sev] || '#6366f1';
}

function filterInsightsBySeverity(sev) {
    document.querySelectorAll('.ai-tab-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.severity === sev);
    });

    if (sev === 'ALL') {
        renderInsightsDeck(allInsights);
    } else {
        renderInsightsDeck(allInsights.filter(i => i.severity === sev));
    }
}

// Deep Explainable AI Modal
function openInsightDetail(id) {
    const insight = allInsights.find(i => i.id === id);
    if (!insight) return;
    activeInsight = insight;

    document.getElementById('xai-modal-badge').innerHTML = Utils.renderSeverityBadge(insight.severity);
    document.getElementById('xai-modal-title').textContent = insight.title;
    document.getElementById('xai-modal-confidence').textContent = `Confidence: ${insight.confidenceScore}%`;

    // 1. Problem
    document.getElementById('xai-problem-text').textContent = insight.metricSummary;

    // 2. Evidence
    const evidenceEl = document.getElementById('xai-evidence-box');
    try {
        const parsed = JSON.parse(insight.evidenceJson);
        evidenceEl.innerHTML = Object.entries(parsed).map(([k, v]) => `
            <div style="background: rgba(255,255,255,0.05); padding: 8px 12px; border-radius: 6px;">
                <div style="font-size: 11px; text-transform: uppercase; color: var(--text-muted);">${formatKeyName(k)}</div>
                <div style="font-size: 14px; font-weight: 700; color: #fff;">${v}</div>
            </div>
        `).join('');
    } catch (e) {
        evidenceEl.textContent = insight.evidenceJson || 'Statistical deviation from 30-day baseline.';
    }

    // 3. Root Cause
    document.getElementById('xai-rootcause-text').textContent = insight.rootCause || 'Underlying operational variable shift.';

    // 4. Financial Impact
    document.getElementById('xai-impact-val').textContent = Utils.formatINR(insight.financialImpact);
    document.getElementById('xai-impact-desc').textContent = insight.impactDescription || 'Cumulative bottom-line variance.';

    // 5. Actionable Recommendation
    document.getElementById('xai-rec-text').textContent = insight.recommendation;

    Utils.openModal('xai-modal');
}

function formatKeyName(camelCase) {
    return camelCase.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase());
}

async function markActiveInsightRead() {
    if (!activeInsight) return;
    try {
        await Api.post(`/ai/insights/${activeInsight.id}/read`);
        Utils.closeModal('xai-modal');
        loadAiInsights();
    } catch (e) {
        Utils.showToast('Error marking as read', 'danger');
    }
}

async function runAiAnalysis() {
    Utils.showToast('AI Decision Intelligence engine running across all transactions...', 'info');
    try {
        const insights = await Api.post('/ai/generate');
        Utils.showToast(`AI Engine completed! Generated ${insights.length} fresh insights.`, 'success');
        loadAiInsights();
    } catch (e) {
        Utils.showToast('AI analysis failed: ' + e.message, 'danger');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    initAiPage();
});
