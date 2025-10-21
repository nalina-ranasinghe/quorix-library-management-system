document.addEventListener('DOMContentLoaded', () => {
    const registerForm = document.getElementById('registerForm');
    const errorDiv = document.getElementById('register-error');
    const successDiv = document.getElementById('register-success');
    const submitButton = registerForm.querySelector('button[type="submit"]');

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
            return;
        }

        try {
            const response = await fetch('/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    username,
                    fullName,
                    email,
                    phone,
                    password,
                    confirmPassword
                }),
            });

            const result = await response.json();

            if (response.ok) {
                showSuccess(result.message);
                registerForm.reset();
            } else {
                showError(result.message || 'An unknown error occurred.');
            }
        } catch (error) {
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