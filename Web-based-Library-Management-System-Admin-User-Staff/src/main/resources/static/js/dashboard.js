document.addEventListener('DOMContentLoaded', () => {
    // Staggered animation for dashboard cards
    const cards = document.querySelectorAll('.card.animate-fade-in-up');
    cards.forEach((card, index) => {
        card.style.animationDelay = `${index * 100}ms`;
    });

    // Typing effect for the main header
    const header = document.querySelector('.content-header h1[data-typing-effect]');
    if (header) {
        const text = header.dataset.typingEffect;
        header.textContent = '';
        let i = 0;
        function typeWriter() {
            if (i < text.length) {
                header.textContent += text.charAt(i);
                i++;
                setTimeout(typeWriter, 75);
            }
        }
        typeWriter();
    }
});