/**
 * IntelliERP Authentication Manager
 */

const Auth = {
    isAuthenticated() {
        return !!Api.getToken();
    },

    getCurrentUser() {
        return Api.getUser();
    },

    hasRole(role) {
        const user = this.getCurrentUser();
        return user && user.role === role;
    },

    isAdmin() {
        return this.hasRole('ROLE_ADMIN');
    },

    isManager() {
        return this.hasRole('ROLE_MANAGER') || this.isAdmin();
    },

    async login(username, password) {
        try {
            const data = await Api.post('/auth/login', { username, password });
            Api.setToken(data.token);
            Api.setUser({
                id: data.id,
                username: data.username,
                fullName: data.fullName,
                email: data.email,
                role: data.role
            });
            return data;
        } catch (error) {
            throw error;
        }
    },

    async register(userData) {
        return await Api.post('/auth/register', userData);
    },

    logout() {
        Api.removeToken();
        window.location.href = '/login.html';
    },

    initPageProtection() {
        const isLoginPage = window.location.pathname.includes('login.html');
        if (!this.isAuthenticated() && !isLoginPage) {
            window.location.href = '/login.html';
            return;
        }

        if (this.isAuthenticated() && isLoginPage) {
            window.location.href = '/dashboard.html';
            return;
        }

        if (this.isAuthenticated()) {
            this.renderUserProfile();
            this.initNotificationBadge();
        }
    },

    renderUserProfile() {
        const user = this.getCurrentUser();
        if (!user) return;

        const nameEls = document.querySelectorAll('.user-name-display');
        const roleEls = document.querySelectorAll('.user-role-display');
        const avatarEls = document.querySelectorAll('.user-avatar-display');

        nameEls.forEach(el => el.textContent = user.fullName || user.username);
        roleEls.forEach(el => el.textContent = user.role.replace('ROLE_', ''));
        avatarEls.forEach(el => {
            el.textContent = (user.fullName || user.username).charAt(0).toUpperCase();
        });

        // Hide manager/admin exclusive menus if employee
        if (user.role === 'ROLE_EMPLOYEE') {
            document.querySelectorAll('.admin-only, .manager-only').forEach(el => el.style.display = 'none');
        } else if (user.role === 'ROLE_MANAGER') {
            document.querySelectorAll('.admin-only').forEach(el => el.style.display = 'none');
        }
    },

    async initNotificationBadge() {
        try {
            const data = await Api.get('/notifications/unread-count');
            const badge = document.querySelector('.notification-badge-dot');
            if (badge) {
                badge.style.display = data.count > 0 ? 'block' : 'none';
            }
        } catch (e) {
            // Ignore
        }
    }
};

// Run protection check on script load
document.addEventListener('DOMContentLoaded', () => {
    Auth.initPageProtection();
});
