const CACHE_NAME = 'intellierp-pwa-v1';
const STATIC_ASSETS = [
  '/',
  '/login.html',
  '/dashboard.html',
  '/sales.html',
  '/products.html',
  '/inventory.html',
  '/suppliers.html',
  '/purchases.html',
  '/customers.html',
  '/expenses.html',
  '/reports.html',
  '/ai-insights.html',
  '/assistant.html',
  '/settings.html',
  '/manifest.json',
  '/icons/icon.svg',
  '/icons/icon-192.png',
  '/icons/icon-512.png',
  '/css/style.css',
  '/css/forms.css',
  '/css/dashboard.css',
  '/css/tables.css',
  '/css/modal.css',
  '/css/pos.css',
  '/css/insights.css',
  '/css/assistant.css',
  '/js/auth.js',
  '/js/api.js',
  '/js/utils.js',
  '/js/pwa.js',
  '/js/charts.js',
  '/js/dashboard.js',
  '/js/sales.js',
  '/js/products.js',
  '/js/inventory.js',
  '/js/suppliers.js',
  '/js/purchases.js',
  '/js/customers.js',
  '/js/expenses.js',
  '/js/reports.js',
  '/js/ai.js',
  '/js/assistant.js'
];

// Install Event - Pre-cache core shell
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      return cache.addAll(STATIC_ASSETS).catch((err) => {
        console.warn('Some assets failed to cache during install:', err);
      });
    }).then(() => self.skipWaiting())
  );
});

// Activate Event - Clean up old caches
self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames.map((name) => {
          if (name !== CACHE_NAME) {
            return caches.delete(name);
          }
        })
      );
    }).then(() => self.clients.claim())
  );
});

// Fetch Event - Strategy: Network-first for API, Cache-first for static assets
self.addEventListener('fetch', (event) => {
  const url = new URL(event.request.url);

  // Do not intercept non-GET requests or browser-extensions
  if (event.request.method !== 'GET' || !url.protocol.startsWith('http')) {
    return;
  }

  // API Requests: Network First, no cache
  if (url.pathname.startsWith('/api/')) {
    event.respondWith(
      fetch(event.request).catch(() => {
        return new Response(JSON.stringify({
          success: false,
          message: 'You are currently offline. Please check your network connection.'
        }), {
          headers: { 'Content-Type': 'application/json' }
        });
      })
    );
    return;
  }

  // Static Assets / Pages: Stale-While-Revalidate or Cache-First with fallback
  event.respondWith(
    caches.match(event.request).then((cachedResponse) => {
      const fetchPromise = fetch(event.request).then((networkResponse) => {
        if (networkResponse && networkResponse.status === 200 && networkResponse.type === 'basic') {
          const responseToCache = networkResponse.clone();
          caches.open(CACHE_NAME).then((cache) => {
            cache.put(event.request, responseToCache);
          });
        }
        return networkResponse;
      }).catch(() => {
        return cachedResponse;
      });

      return cachedResponse || fetchPromise;
    })
  );
});
