// Authentication functionality for DSR Mock Frontend

document.addEventListener('DOMContentLoaded', () => {
    initializeAuthForms();
});

function initializeAuthForms() {
    // Login form
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }

    // Register form
    const registerForm = document.getElementById('register-form');
    if (registerForm) {
        registerForm.addEventListener('submit', handleRegister);
    }

    // Forgot password form
    const forgotPasswordForm = document.getElementById('forgot-password-form');
    if (forgotPasswordForm) {
        forgotPasswordForm.addEventListener('submit', handleForgotPassword);
    }

    // Forgot password link
    const forgotPasswordLink = document.getElementById('forgot-password-link');
    if (forgotPasswordLink) {
        forgotPasswordLink.addEventListener('click', (e) => {
            e.preventDefault();
            showModal('forgot-password-modal');
        });
    }

    // PhilSys login button
    const philsysLoginButton = document.querySelector('[data-testid="philsys-login-button"]');
    if (philsysLoginButton) {
        philsysLoginButton.addEventListener('click', handlePhilSysLogin);
    }
}

async function handleLogin(e) {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const email = formData.get('email');
    const password = formData.get('password');
    const rememberMe = formData.get('remember-me') === 'on';

    // Validate inputs
    if (!email || !password) {
        showErrorMessage('Please fill in all fields');
        return;
    }

    if (!validateEmail(email)) {
        showErrorMessage('Please enter a valid email address');
        return;
    }

    try {
        showLoading();

        const response = await apiRequest('/auth/login', {
            method: 'POST',
            body: JSON.stringify({
                email,
                password,
                rememberMe
            })
        });

        if (response.success) {
            // Store authentication data
            setAuthToken(response.accessToken);
            setUser(response.user);

            showSuccessMessage('Login successful! Redirecting...');
            
            // Redirect to dashboard after short delay
            setTimeout(() => {
                redirectToDashboard();
            }, 1000);
        } else {
            showErrorMessage(response.message || 'Login failed');
        }
    } catch (error) {
        console.error('Login error:', error);
        showErrorMessage(error.message || 'Login failed. Please try again.');
    } finally {
        hideLoading();
    }
}

async function handleRegister(e) {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const firstName = formData.get('firstName');
    const lastName = formData.get('lastName');
    const email = formData.get('email');
    const password = formData.get('password');
    const confirmPassword = formData.get('confirmPassword');
    const termsAccepted = formData.get('terms') === 'on';

    // Validate inputs
    if (!firstName || !lastName || !email || !password || !confirmPassword) {
        showErrorMessage('Please fill in all fields');
        return;
    }

    if (!validateEmail(email)) {
        showErrorMessage('Please enter a valid email address');
        return;
    }

    if (!validatePassword(password)) {
        showErrorMessage('Password must be at least 8 characters with uppercase, lowercase, number, and special character');
        return;
    }

    if (password !== confirmPassword) {
        showErrorMessage('Passwords do not match');
        return;
    }

    if (!termsAccepted) {
        showErrorMessage('Please accept the terms and conditions');
        return;
    }

    try {
        showLoading();

        const response = await apiRequest('/auth/register', {
            method: 'POST',
            body: JSON.stringify({
                firstName,
                lastName,
                email,
                password
            })
        });

        if (response.success) {
            showSuccessMessage('Registration successful! Please login to continue.');
            
            // Redirect to login page after short delay
            setTimeout(() => {
                window.location.href = '/login';
            }, 2000);
        } else {
            showErrorMessage(response.message || 'Registration failed');
        }
    } catch (error) {
        console.error('Registration error:', error);
        showErrorMessage(error.message || 'Registration failed. Please try again.');
    } finally {
        hideLoading();
    }
}

async function handleForgotPassword(e) {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const email = formData.get('email');

    // Validate input
    if (!email) {
        showErrorMessage('Please enter your email address');
        return;
    }

    if (!validateEmail(email)) {
        showErrorMessage('Please enter a valid email address');
        return;
    }

    try {
        showLoading();

        const response = await apiRequest('/auth/reset-password', {
            method: 'POST',
            body: JSON.stringify({ email })
        });

        if (response.success) {
            showSuccessMessage('Password reset email sent! Please check your inbox.');
            hideModal('forgot-password-modal');
            
            // Clear the form
            e.target.reset();
        } else {
            showErrorMessage(response.message || 'Failed to send reset email');
        }
    } catch (error) {
        console.error('Password reset error:', error);
        showErrorMessage(error.message || 'Failed to send reset email. Please try again.');
    } finally {
        hideLoading();
    }
}

async function handlePhilSysLogin(e) {
    e.preventDefault();
    
    try {
        showLoading();
        
        // Simulate PhilSys authentication flow
        showSuccessMessage('Redirecting to PhilSys authentication...');
        
        // In a real implementation, this would redirect to PhilSys OAuth
        setTimeout(() => {
            // Mock successful PhilSys authentication
            const mockUser = {
                id: 'philsys_123',
                email: 'philsys.user@test.dsr.gov.ph',
                name: 'PhilSys User',
                role: 'citizen',
                psn: '1234567890123456',
                authMethod: 'philsys'
            };
            
            const mockToken = 'mock_philsys_token_' + Date.now();
            
            setAuthToken(mockToken);
            setUser(mockUser);
            
            showSuccessMessage('PhilSys authentication successful! Redirecting...');
            
            setTimeout(() => {
                redirectToDashboard();
            }, 1000);
        }, 2000);
    } catch (error) {
        console.error('PhilSys login error:', error);
        showErrorMessage('PhilSys authentication failed. Please try again.');
    } finally {
        setTimeout(() => {
            hideLoading();
        }, 2000);
    }
}

// Password strength indicator
function updatePasswordStrength(password) {
    const strengthIndicator = document.getElementById('password-strength');
    if (!strengthIndicator) return;

    let strength = 0;
    let feedback = [];

    if (password.length >= 8) strength++;
    else feedback.push('At least 8 characters');

    if (/[a-z]/.test(password)) strength++;
    else feedback.push('Lowercase letter');

    if (/[A-Z]/.test(password)) strength++;
    else feedback.push('Uppercase letter');

    if (/\d/.test(password)) strength++;
    else feedback.push('Number');

    if (/[@$!%*?&]/.test(password)) strength++;
    else feedback.push('Special character');

    const strengthLevels = ['Very Weak', 'Weak', 'Fair', 'Good', 'Strong'];
    const strengthColors = ['#ef4444', '#f97316', '#eab308', '#22c55e', '#16a34a'];

    strengthIndicator.textContent = strengthLevels[strength] || 'Very Weak';
    strengthIndicator.style.color = strengthColors[strength] || '#ef4444';

    const feedbackEl = document.getElementById('password-feedback');
    if (feedbackEl) {
        if (feedback.length > 0) {
            feedbackEl.textContent = 'Missing: ' + feedback.join(', ');
            feedbackEl.style.display = 'block';
        } else {
            feedbackEl.style.display = 'none';
        }
    }
}

// Add password strength checking if password field exists
document.addEventListener('DOMContentLoaded', () => {
    const passwordInput = document.querySelector('input[name="password"]');
    if (passwordInput) {
        passwordInput.addEventListener('input', (e) => {
            updatePasswordStrength(e.target.value);
        });
    }
});

// Export functions for testing
window.DSRAuth = {
    handleLogin,
    handleRegister,
    handleForgotPassword,
    handlePhilSysLogin,
    updatePasswordStrength
};
