// Common JavaScript functionality for DSR Mock Frontend

// Global state
window.DSR = {
    user: null,
    token: null,
    apiBaseUrl: 'http://localhost:3000/api'
};

// Utility functions
function showLoading() {
    const spinner = document.getElementById('loading-spinner');
    if (spinner) {
        spinner.classList.remove('hidden');
    }
}

function hideLoading() {
    const spinner = document.getElementById('loading-spinner');
    if (spinner) {
        spinner.classList.add('hidden');
    }
}

function showMessage(type, message) {
    const messageEl = document.getElementById(`${type}-message`);
    if (messageEl) {
        const textEl = messageEl.querySelector('.message-text');
        if (textEl) {
            textEl.textContent = message;
        }
        messageEl.classList.remove('hidden');
        
        // Auto-hide after 5 seconds
        setTimeout(() => {
            hideMessage(`${type}-message`);
        }, 5000);
    }
}

function hideMessage(messageId) {
    const messageEl = document.getElementById(messageId);
    if (messageEl) {
        messageEl.classList.add('hidden');
    }
}

function showSuccessMessage(message) {
    showMessage('success', message);
}

function showErrorMessage(message) {
    showMessage('error', message);
}

// API helper functions
async function apiRequest(endpoint, options = {}) {
    const url = `${window.DSR.apiBaseUrl}${endpoint}`;
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    if (window.DSR.token) {
        defaultOptions.headers.Authorization = `Bearer ${window.DSR.token}`;
    }

    const finalOptions = {
        ...defaultOptions,
        ...options,
        headers: {
            ...defaultOptions.headers,
            ...options.headers,
        },
    };

    try {
        const response = await fetch(url, finalOptions);
        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.error || `HTTP error! status: ${response.status}`);
        }

        return data;
    } catch (error) {
        console.error('API request failed:', error);
        throw error;
    }
}

// Authentication helpers
function setAuthToken(token) {
    window.DSR.token = token;
    localStorage.setItem('dsr_token', token);
}

function getAuthToken() {
    if (!window.DSR.token) {
        window.DSR.token = localStorage.getItem('dsr_token');
    }
    return window.DSR.token;
}

function clearAuthToken() {
    window.DSR.token = null;
    window.DSR.user = null;
    localStorage.removeItem('dsr_token');
    localStorage.removeItem('dsr_user');
}

function setUser(user) {
    window.DSR.user = user;
    localStorage.setItem('dsr_user', JSON.stringify(user));
}

function getUser() {
    if (!window.DSR.user) {
        const userData = localStorage.getItem('dsr_user');
        if (userData) {
            window.DSR.user = JSON.parse(userData);
        }
    }
    return window.DSR.user;
}

function isAuthenticated() {
    return !!getAuthToken() && !!getUser();
}

// Navigation helpers
function redirectToLogin() {
    window.location.href = '/login';
}

function redirectToDashboard() {
    window.location.href = '/dashboard';
}

// Form validation helpers
function validateEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

function validatePassword(password) {
    // At least 8 characters, 1 uppercase, 1 lowercase, 1 number, 1 special character
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
    return passwordRegex.test(password);
}

function validatePSN(psn) {
    // PhilSys Number should be 16 digits
    const psnRegex = /^\d{16}$/;
    return psnRegex.test(psn.replace(/[-\s]/g, ''));
}

function validatePhoneNumber(phone) {
    // Philippine mobile number format
    const phoneRegex = /^(\+63|0)[0-9]{10}$/;
    return phoneRegex.test(phone.replace(/[-\s]/g, ''));
}

// User menu functionality
function initializeUserMenu() {
    const userMenuButton = document.querySelector('[data-testid="user-menu-button"]');
    const userMenuDropdown = document.querySelector('[data-testid="user-menu-dropdown"]');
    const logoutButton = document.querySelector('[data-testid="logout-button"]');

    if (userMenuButton && userMenuDropdown) {
        userMenuButton.addEventListener('click', (e) => {
            e.stopPropagation();
            userMenuDropdown.classList.toggle('hidden');
        });

        // Close dropdown when clicking outside
        document.addEventListener('click', () => {
            userMenuDropdown.classList.add('hidden');
        });
    }

    if (logoutButton) {
        logoutButton.addEventListener('click', async (e) => {
            e.preventDefault();
            await logout();
        });
    }

    // Update user name in menu
    const user = getUser();
    if (user) {
        const userNameEl = document.getElementById('user-name');
        if (userNameEl) {
            userNameEl.textContent = user.name || user.email || 'User';
        }
    }
}

// Logout functionality
async function logout() {
    try {
        showLoading();
        
        // Call logout API
        await apiRequest('/auth/logout', {
            method: 'POST'
        });
        
        clearAuthToken();
        showSuccessMessage('Logged out successfully');
        
        setTimeout(() => {
            redirectToLogin();
        }, 1000);
    } catch (error) {
        console.error('Logout failed:', error);
        // Clear tokens anyway
        clearAuthToken();
        redirectToLogin();
    } finally {
        hideLoading();
    }
}

// Modal functionality
function showModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.remove('hidden');
    }
}

function hideModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.add('hidden');
    }
}

// Initialize modal close buttons
function initializeModals() {
    const modalCloseButtons = document.querySelectorAll('.modal-close');
    modalCloseButtons.forEach(button => {
        button.addEventListener('click', (e) => {
            const modal = e.target.closest('.modal');
            if (modal) {
                modal.classList.add('hidden');
            }
        });
    });

    // Close modal when clicking outside
    const modals = document.querySelectorAll('.modal');
    modals.forEach(modal => {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                modal.classList.add('hidden');
            }
        });
    });
}

// Page protection for authenticated routes
function requireAuth() {
    if (!isAuthenticated()) {
        redirectToLogin();
        return false;
    }
    return true;
}

// Initialize common functionality when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    initializeUserMenu();
    initializeModals();
    
    // Check authentication for protected pages
    const protectedPages = ['/dashboard', '/profile', '/registration', '/eligibility', '/payments', '/grievances', '/analytics'];
    const currentPath = window.location.pathname;
    
    if (protectedPages.includes(currentPath)) {
        requireAuth();
    }
    
    // Redirect to dashboard if already authenticated and on login/register page
    if ((currentPath === '/login' || currentPath === '/register') && isAuthenticated()) {
        redirectToDashboard();
    }
});

// Export functions for use in other scripts
window.DSRCommon = {
    showLoading,
    hideLoading,
    showSuccessMessage,
    showErrorMessage,
    apiRequest,
    setAuthToken,
    getAuthToken,
    clearAuthToken,
    setUser,
    getUser,
    isAuthenticated,
    redirectToLogin,
    redirectToDashboard,
    validateEmail,
    validatePassword,
    validatePSN,
    validatePhoneNumber,
    logout,
    showModal,
    hideModal,
    requireAuth
};
