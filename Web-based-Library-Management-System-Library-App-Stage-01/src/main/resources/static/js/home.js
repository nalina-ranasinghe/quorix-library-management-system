document.addEventListener('DOMContentLoaded', function () {
    const userIcon = document.getElementById('userIcon');
    const userDropdown = document.getElementById('userDropdown');

    // --- YOUR ORIGINAL DROPDOWN LOGIC WITH THE FIX ---
    if (userIcon) {
        userIcon.addEventListener('click', (e) => {
            // FIX: Add this line to stop the click from closing the menu immediately.
            e.stopPropagation();
            userDropdown.classList.toggle('show');
        });

        window.addEventListener('click', (e) => {
            // This part correctly closes the menu if you click outside of it.
            // No changes were needed here.
            if (!userIcon.contains(e.target) && !userDropdown.contains(e.target)) {
                userDropdown.classList.remove('show');
            }
        });
    }
    // --- END OF FIX ---


    // --- YOUR EXISTING "ABOUT" LINK SMOOTH SCROLL LOGIC (UNCHANGED) ---
    const aboutLink = document.getElementById('aboutLink');
    const footerSection = document.getElementById('footerSection');
    if (aboutLink && footerSection) {
        aboutLink.addEventListener('click', (e) => {
            e.preventDefault();
            footerSection.scrollIntoView({ behavior: 'smooth' });
            if (userDropdown) userDropdown.classList.remove('show');
        });
    }

    // --- YOUR EXISTING API-BASED SEARCH LOGIC (UNCHANGED) ---
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        const bookGrid = document.getElementById('bookGrid');
        const initialBooksHtml = bookGrid.innerHTML;
        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfParameterName = document.querySelector('meta[name="_csrf_parameter_name"]').getAttribute('content');

        searchInput.addEventListener('input', debounce(handleSearch, 300));

        async function handleSearch(event) {
            const keyword = event.target.value.trim();
            if (keyword.length === 0) {
                bookGrid.innerHTML = initialBooksHtml;
                return;
            }
            try {
                const response = await fetch(`/api/books/search?keyword=${encodeURIComponent(keyword)}`);
                if (!response.ok) throw new Error('Network response was not ok');
                const books = await response.json();
                displayBooks(books);
            } catch (error) {
                console.error('Search error:', error);
                bookGrid.innerHTML = `<p class="text-center text-white">Could not perform search.</p>`;
            }
        }

        function displayBooks(books) {
            bookGrid.innerHTML = '';
            if (books.length === 0) {
                bookGrid.innerHTML = `<p class="text-center text-white-50">No books found.</p>`;
                return;
            }
            books.forEach(book => {
                const availabilityBadge = book.quantity > 0
                    ? `<span class="badge bg-success">${book.quantity} Available</span>`
                    : `<span class="badge bg-danger">Out of Stock</span>`;
                const actionButton = book.quantity > 0
                    ? `<form action="/reserve" method="post" class="d-inline">
                           <input type="hidden" name="bookId" value="${book.bookId}">
                           <input type="hidden" name="${csrfParameterName}" value="${csrfToken}">
                           <button type="submit" class="btn btn-primary btn-sm">Reserve</button>
                       </form>`
                    : `<form action="/waitlist" method="post" class="d-inline">
                           <input type="hidden" name="bookId" value="${book.bookId}">
                           <input type="hidden" name="${csrfParameterName}" value="${csrfToken}">
                           <button type="submit" class="btn btn-warning btn-sm">Join Waitlist</button>
                       </form>`;
                const bookCardHtml = `
                    <div class="col-lg-3 col-md-4 col-sm-6">
                        <div class="book-card">
                            <img src="/images/books/${book.isbn}.jpg"
                                 onerror="this.onerror=null; this.src='/images/books/placeholder.jpg';"
                                 class="card-img-top book-card-img" alt="Book Cover">
                            <div class="card-body">
                                <h5 class="card-title">${book.title}</h5>
                                <p class="card-text text-secondary-light">${book.author}</p>
                            </div>
                            <div class="card-footer d-flex justify-content-between align-items-center bg-transparent border-top-0 pt-0">
                                ${availabilityBadge}
                                ${actionButton}
                            </div>
                        </div>
                    </div>
                `;
                bookGrid.insertAdjacentHTML('beforeend', bookCardHtml);
            });
        }
    }

    // --- YOUR EXISTING DEBOUNCE HELPER FUNCTION (UNCHANGED) ---
    function debounce(func, delay) {
        let timeout;
        return function(...args) {
            clearTimeout(timeout);
            timeout = setTimeout(() => func.apply(this, args), delay);
        };
    }
});