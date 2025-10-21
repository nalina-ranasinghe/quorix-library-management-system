document.addEventListener('DOMContentLoaded', () => {
    const registerForm = document.getElementById('registerForm');
    const errorDiv = document.getElementById('register-error');
    const successDiv = document.getElementById('register-success');
    const submitButton = registerForm.querySelector('button[type="submit"]');

    // Read CSRF token/header exposed in the page (added to index.html)
    const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    const csrfToken = csrfTokenMeta ? csrfTokenMeta.getAttribute('content') : null;
    const csrfHeader = csrfHeaderMeta ? csrfHeaderMeta.getAttribute('content') : 'X-CSRF-TOKEN';

    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        // Hide previous messages
        errorDiv.classList.add('d-none');
        successDiv.classList.add('d-none');
        submitButton.disabled = true;
        submitButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Creating...';

        const username = document.getElementById('regUsername').value;
        const fullName = document.getElementById('fullName').value;
        const email = document.getElementById('regEmail').value;
        const phone = document.getElementById('regPhone').value;
        const password = document.getElementById('regPassword').value;
        const confirmPassword = document.getElementById('confirmPassword').value;

        if (password !== confirmPassword) {
            showError('Passwords do not match.');
            // re-enable button and restore label
            submitButton.disabled = false;
            submitButton.innerHTML = 'Create Account';
            return;
        }

        try {
            const headers = {
                'Content-Type': 'application/json',
            };
            if (csrfToken) {
                headers[csrfHeader] = csrfToken;
            }

            const response = await fetch('/auth/register', {
                method: 'POST',
                headers,
                body: JSON.stringify({
                    username,
                    fullName,
                    email,
                    phone,
                    password,
                    confirmPassword
                }),
            });

            // Try to parse response as JSON if possible, otherwise read text
            const contentType = response.headers.get('Content-Type') || '';
            let result = null;
            if (contentType.includes('application/json')) {
                result = await response.json();
            } else {
                // not JSON (could be HTML error page from Spring Security), read text
                const text = await response.text();
                result = { message: text };
            }

            if (response.ok) {
                showSuccess(result.message || 'Registration successful.');
                registerForm.reset();
            } else {
                // Show message from server when available; otherwise show status text
                const msg = (result && result.message) ? result.message : (`Request failed: ${response.status} ${response.statusText}`);
                showError(msg);
            }
        } catch (error) {
            // Network error or parse failure
            console.error('Register request failed', error);
            showError('Could not connect to the server. Please try again later.');
        } finally {
            submitButton.disabled = false;
            submitButton.innerHTML = 'Create Account';
        }
    });

    function showError(message) {
        errorDiv.textContent = message;
        errorDiv.classList.remove('d-none');
    }

    function showSuccess(message) {
        successDiv.textContent = message;
        successDiv.classList.remove('d-none');
    }
});