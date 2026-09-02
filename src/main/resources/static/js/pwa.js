/**
 * IntelliERP Progressive Web App (PWA) Manager
 * Handles Service Worker registration and Chrome Install Prompt
 */

let deferredInstallPrompt = null;

// Register Service Worker
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js')
      .then((registration) => {
        console.log('[PWA] Service Worker registered successfully with scope:', registration.scope);
      })
      .catch((error) => {
        console.warn('[PWA] Service Worker registration failed:', error);
      });
  });
}

// Capture Chrome's Install Prompt Event
window.addEventListener('beforeinstallprompt', (e) => {
  // Prevent Chrome mini-infobar from appearing on mobile automatically
  e.preventDefault();
  // Stash the event so it can be triggered on user action
  deferredInstallPrompt = e;
  console.log('[PWA] beforeinstallprompt captured, ready to trigger');
  
  // Show UI install triggers
  showInstallPromo();
});

// App Installed Event
window.addEventListener('appinstalled', () => {
  console.log('[PWA] IntelliERP application was installed successfully');
  deferredInstallPrompt = null;
  hideInstallPromo();
  
  if (window.showToast) {
    showToast('IntelliERP App installed! You can now launch it from your Desktop/Start Menu.', 'success');
  }
});

/**
 * Triggers the Chrome Install Prompt
 */
async function triggerPwaInstall() {
  if (!deferredInstallPrompt) {
    // If not triggered via beforeinstallprompt, guide user
    alert('To install IntelliERP on Chrome:\n1. Click the Install icon (📥) in the address bar (top-right)\n2. Or click Chrome Menu (⋮) -> Cast, save, and share -> Install IntelliERP...');
    return;
  }

  // Show Chrome's native install dialog
  deferredInstallPrompt.prompt();
  
  // Wait for the user to respond to the prompt
  const { outcome } = await deferredInstallPrompt.userChoice;
  console.log(`[PWA] User response to install prompt: ${outcome}`);
  
  // Clear deferred prompt
  deferredInstallPrompt = null;
  hideInstallPromo();
}

/**
 * Shows the custom in-app install buttons / badges
 */
function showInstallPromo() {
  // Check if button already exists
  let installBtn = document.getElementById('pwaInstallBtn');
  if (installBtn) {
    installBtn.style.display = 'inline-flex';
    return;
  }

  // If sidebar exists, add to sidebar bottom
  const sidebar = document.querySelector('.sidebar');
  if (sidebar) {
    const promoDiv = document.createElement('div');
    promoDiv.id = 'pwaSidebarPromo';
    promoDiv.className = 'pwa-install-container';
    promoDiv.innerHTML = `
      <button id="pwaInstallBtn" class="pwa-install-btn" onclick="triggerPwaInstall()">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
          <polyline points="7 10 12 15 17 10"></polyline>
          <line x1="12" y1="15" x2="12" y2="3"></line>
        </svg>
        <span>Download Desktop App</span>
      </button>
    `;
    sidebar.appendChild(promoDiv);
    return;
  }

  // If login page or standalone page, create a floating badge
  const floatingBtn = document.createElement('div');
  floatingBtn.id = 'pwaFloatingBadge';
  floatingBtn.className = 'pwa-floating-badge';
  floatingBtn.innerHTML = `
    <button id="pwaInstallBtn" class="pwa-floating-btn" onclick="triggerPwaInstall()" title="Install IntelliERP as an App">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
        <polyline points="7 10 12 15 17 10"></polyline>
        <line x1="12" y1="15" x2="12" y2="3"></line>
      </svg>
      <span>Install App</span>
    </button>
  `;
  document.body.appendChild(floatingBtn);
}

/**
 * Hides install UI promo
 */
function hideInstallPromo() {
  const sidebarPromo = document.getElementById('pwaSidebarPromo');
  if (sidebarPromo) sidebarPromo.remove();

  const floatingBadge = document.getElementById('pwaFloatingBadge');
  if (floatingBadge) floatingBadge.remove();

  const installBtn = document.getElementById('pwaInstallBtn');
  if (installBtn) installBtn.style.display = 'none';
}
