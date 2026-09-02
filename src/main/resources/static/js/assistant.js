/**
 * IntelliERP Natural Language Business Assistant ("Ask Your Business Data")
 */

async function sendAssistantQuery(customQuery = null) {
    const input = document.getElementById('assistant-input');
    const query = customQuery || (input ? input.value.trim() : '');

    if (!query) return;
    if (input && !customQuery) input.value = '';

    appendUserMessage(query);
    showTypingIndicator();

    try {
        const response = await Api.post('/ai/ask', { query });
        removeTypingIndicator();
        appendAssistantResponse(response);
    } catch (e) {
        removeTypingIndicator();
        appendErrorMessage('Sorry, I encountered an issue analyzing your data: ' + e.message);
    }
}

function appendUserMessage(text) {
    const chatBox = document.getElementById('chat-messages');
    if (!chatBox) return;

    const div = document.createElement('div');
    div.className = 'chat-bubble user-bubble';
    div.style.cssText = 'align-self: flex-end; background: var(--primary-gradient); color: #fff; padding: 12px 18px; border-radius: 16px 16px 4px 16px; max-width: 80%; margin-bottom: 16px; font-size: 14px; box-shadow: var(--shadow-md);';
    div.textContent = text;

    chatBox.appendChild(div);
    chatBox.scrollTop = chatBox.scrollHeight;
}

function showTypingIndicator() {
    const chatBox = document.getElementById('chat-messages');
    if (!chatBox) return;

    const div = document.createElement('div');
    div.id = 'typing-indicator';
    div.style.cssText = 'align-self: flex-start; background: var(--bg-card); color: var(--text-muted); padding: 10px 16px; border-radius: 16px 16px 16px 4px; font-size: 13px; margin-bottom: 16px; display: flex; align-items: center; gap: 8px; border: 1px solid var(--border-color);';
    div.innerHTML = `<span>⚙ Analyzing database ledger and running analytics...</span>`;

    chatBox.appendChild(div);
    chatBox.scrollTop = chatBox.scrollHeight;
}

function removeTypingIndicator() {
    const el = document.getElementById('typing-indicator');
    if (el) el.remove();
}

function appendAssistantResponse(data) {
    const chatBox = document.getElementById('chat-messages');
    if (!chatBox) return;

    const container = document.createElement('div');
    container.style.cssText = 'align-self: flex-start; background: var(--bg-card); border: 1px solid var(--border-color); padding: 20px; border-radius: 16px 16px 16px 4px; max-width: 85%; margin-bottom: 20px; box-shadow: var(--shadow-sm);';

    let calcHtml = '';
    if (data.calculatedData) {
        if (Array.isArray(data.calculatedData)) {
            calcHtml = `
                <div style="margin: 12px 0; background: rgba(0,0,0,0.25); border-radius: 8px; overflow: hidden; border: 1px solid var(--border-color);">
                    <table class="data-table" style="font-size: 12.5px;">
                        <tbody>
                            ${data.calculatedData.map(item => `
                                <tr>
                                    ${Object.entries(item).map(([k, v]) => `<td><strong style="color: var(--text-muted);">${k}:</strong> ${typeof v === 'number' ? Utils.formatINR(v) : v}</td>`).join('')}
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            `;
        } else if (typeof data.calculatedData === 'object') {
            calcHtml = `
                <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(140px, 1fr)); gap: 10px; margin: 12px 0;">
                    ${Object.entries(data.calculatedData).map(([k, v]) => `
                        <div style="background: rgba(255,255,255,0.04); padding: 10px; border-radius: 6px; border: 1px solid var(--border-color);">
                            <div style="font-size: 11px; text-transform: uppercase; color: var(--text-muted);">${k}</div>
                            <div style="font-size: 15px; font-weight: 700; color: #fff; margin-top: 2px;">
                                ${typeof v === 'number' ? Utils.formatINR(v) : v}
                            </div>
                        </div>
                    `).join('')}
                </div>
            `;
        }
    }

    let recsHtml = '';
    if (data.recommendations && data.recommendations.length > 0) {
        recsHtml = `
            <div style="margin-top: 14px; padding-top: 12px; border-top: 1px solid var(--border-color);">
                <div style="font-size: 12px; font-weight: 700; text-transform: uppercase; color: var(--success); margin-bottom: 6px;">Recommended Actions:</div>
                <ul style="padding-left: 20px; font-size: 13px; color: var(--text-secondary); line-height: 1.6;">
                    ${data.recommendations.map(r => `<li>${r}</li>`).join('')}
                </ul>
            </div>
        `;
    }

    container.innerHTML = `
        <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px;">
            <span style="font-size: 18px;">💡</span>
            <strong style="font-size: 15px; color: #fff;">${data.title || 'AI Decision Advisor'}</strong>
        </div>
        <div style="font-size: 13.5px; color: var(--text-primary); line-height: 1.6;">
            ${data.explanation}
        </div>
        ${calcHtml}
        ${recsHtml}
    `;

    chatBox.appendChild(container);
    chatBox.scrollTop = chatBox.scrollHeight;
}

function appendErrorMessage(msg) {
    const chatBox = document.getElementById('chat-messages');
    if (!chatBox) return;

    const div = document.createElement('div');
    div.style.cssText = 'align-self: flex-start; background: rgba(239, 68, 68, 0.15); border: 1px solid var(--danger); color: #fff; padding: 12px 18px; border-radius: 16px 16px 16px 4px; max-width: 80%; margin-bottom: 16px; font-size: 13.5px;';
    div.textContent = msg;

    chatBox.appendChild(div);
    chatBox.scrollTop = chatBox.scrollHeight;
}

document.addEventListener('DOMContentLoaded', () => {
    const input = document.getElementById('assistant-input');
    if (input) {
        input.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') sendAssistantQuery();
        });
    }
});
