document.addEventListener('DOMContentLoaded', function () {
    // --- 1. User Dropdown Menu Logic ---
    const userIcon = document.getElementById('userIcon');
    const userDropdown = document.getElementById('userDropdown');

    if (userIcon) {
        userIcon.addEventListener('click', () => {
            userDropdown.classList.toggle('show');
        });

        // Close dropdown if clicking outside
        window.addEventListener('click', (e) => {
            if (!userIcon.contains(e.target) && !userDropdown.contains(e.target)) {
                userDropdown.classList.remove('show');
            }
        });
    }

    // --- 2. "About" Link Smooth Scroll Logic ---
    const aboutLink = document.getElementById('aboutLink');
    const footerSection = document.getElementById('footerSection');

    if (aboutLink && footerSection) {
        aboutLink.addEventListener('click', (e) => {
            e.preventDefault();
            footerSection.scrollIntoView({ behavior: 'smooth' });
            userDropdown.classList.remove('show'); // Hide dropdown after click
        });
    }

    // --- 3. Live Search Logic ---
    const searchInput = document.getElementById('searchInput');
    const bookGrid = document.getElementById('bookGrid');
    const initialBooksHtml = bookGrid.innerHTML; // Store initial state

    if (searchInput) {
        searchInput.addEventListener('input', debounce(handleSearch, 300));
    }

    async function handleSearch(event) {
        const keyword = event.target.value;

        if (keyword.trim() === '') {
            bookGrid.innerHTML = initialBooksHtml; // Restore initial list if search is empty
            return;
        }

        try {
            const response = await fetch(`/api/books/search?keyword=${encodeURIComponent(keyword)}`);
            if (!response.ok) throw new Error('Network response was not ok');

            const books = await response.json();
            renderBooks(books);
        } catch (error) {
            console.error('Search failed:', error);
            bookGrid.innerHTML = `<p class="text-danger">Failed to load search results.</p>`;
        }
    }

    function renderBooks(books) {
        bookGrid.innerHTML = ''; // Clear current books
        if (books.length === 0) {
            bookGrid.innerHTML = `<p class="text-center w-100">No books found matching your search.</p>`;
            return;
        }

        books.forEach(book => {
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
                            <span class="badge bg-success">${book.quantity} Available</span>
                            <button class="btn btn-primary btn-sm">Reserve</button>
                        </div>
                    </div>
                </div>
            `;
            bookGrid.insertAdjacentHTML('beforeend', bookCardHtml);
        });
    }

    // Debounce function to limit how often the search function is called
    function debounce(func, delay) {
        let timeout;
        return function(...args) {
            clearTimeout(timeout);
            timeout = setTimeout(() => func.apply(this, args), delay);
        };
    }
});