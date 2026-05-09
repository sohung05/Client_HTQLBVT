document.addEventListener('DOMContentLoaded', () => {
    // 1. Dark Mode Toggle
    const themeToggle = document.getElementById('theme-toggle');
    const themeIcon = themeToggle.querySelector('i');
    
    // Check saved theme
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark') {
        document.documentElement.setAttribute('data-theme', 'dark');
        themeIcon.classList.replace('fa-moon', 'fa-sun');
    }

    themeToggle.addEventListener('click', () => {
        const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
        if (isDark) {
            document.documentElement.removeAttribute('data-theme');
            localStorage.setItem('theme', 'light');
            themeIcon.classList.replace('fa-sun', 'fa-moon');
        } else {
            document.documentElement.setAttribute('data-theme', 'dark');
            localStorage.setItem('theme', 'dark');
            themeIcon.classList.replace('fa-moon', 'fa-sun');
        }
    });

    // 2. Scroll Spy
    const sections = document.querySelectorAll('section[id]');
    const navLinks = document.querySelectorAll('.nav-link');

    function highlightNavigation() {
        let scrollY = window.scrollY;
        
        sections.forEach(current => {
            const sectionHeight = current.offsetHeight;
            const sectionTop = current.offsetTop - 100; // Offset for fixed header
            const sectionId = current.getAttribute('id');
            
            if (scrollY > sectionTop && scrollY <= sectionTop + sectionHeight) {
                navLinks.forEach(link => {
                    link.classList.remove('active');
                    if (link.getAttribute('href') === '#' + sectionId) {
                        link.classList.add('active');
                    }
                });
            }
        });
    }

    window.addEventListener('scroll', highlightNavigation);
    highlightNavigation(); // Initial call

    // 3. Back to Top Button
    const backToTopBtn = document.getElementById('back-to-top');

    window.addEventListener('scroll', () => {
        if (window.scrollY > 300) {
            backToTopBtn.classList.add('visible');
        } else {
            backToTopBtn.classList.remove('visible');
        }
    });

    backToTopBtn.addEventListener('click', () => {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    });

    // 4. Fade-in Animation (Intersection Observer)
    const fadeElements = document.querySelectorAll('.fade-in');
    
    const observerOptions = {
        root: null,
        rootMargin: '0px',
        threshold: 0.15
    };

    const observer = new IntersectionObserver((entries, observer) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
                observer.unobserve(entry.target); // Stop observing once visible
            }
        });
    }, observerOptions);

    fadeElements.forEach(el => observer.observe(el));

    // 5. Accordion
    const accordionItems = document.querySelectorAll('.accordion-item');

    accordionItems.forEach(item => {
        const header = item.querySelector('.accordion-header');
        
        header.addEventListener('click', () => {
            const isActive = item.classList.contains('active');
            
            // Close all
            accordionItems.forEach(acc => {
                acc.classList.remove('active');
                acc.querySelector('.accordion-content').style.maxHeight = null;
            });

            // Open if wasn't active
            if (!isActive) {
                item.classList.add('active');
                const content = item.querySelector('.accordion-content');
                content.style.maxHeight = content.scrollHeight + 'px';
            }
        });
    });

    // 6. Search Functionality
    const searchInput = document.getElementById('search-input');
    
    searchInput.addEventListener('input', function() {
        const query = this.value.toLowerCase().trim();
        
        sections.forEach(section => {
            const textContent = section.innerText.toLowerCase();
            if (textContent.includes(query)) {
                section.style.display = 'block';
                // Trigger animation immediately for searched items
                section.classList.add('visible');
            } else {
                section.style.display = 'none';
            }
        });

        // If search is empty, show all sections again
        if (query === '') {
            sections.forEach(section => {
                section.style.display = 'block';
            });
        }
    });
});
