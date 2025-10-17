// ============================================================================
// DARK MODE FUNCTIONALITY
// ============================================================================

// Initialize dark mode on page load
document.addEventListener('DOMContentLoaded', function() {
    initializeDarkMode();
});

function initializeDarkMode() {
    // Check if user has a saved preference
    const savedMode = localStorage.getItem('darkMode');
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;

    // Apply dark mode if saved preference is 'enabled' or if user prefers dark and no preference is saved
    if (savedMode === 'enabled' || (savedMode === null && prefersDark)) {
        enableDarkMode();
    } else {
        disableDarkMode();
    }

    // Create dark mode toggle button
    createDarkModeToggle();

    // Listen for system theme changes (optional)
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
        if (localStorage.getItem('darkMode') === null) {
            if (e.matches) {
                enableDarkMode();
            } else {
                disableDarkMode();
            }
        }
    });
}

function createDarkModeToggle() {
    // Check if toggle already exists
    if (document.getElementById('darkModeToggle')) {
        return;
    }

    // Create toggle button
    const toggle = document.createElement('button');
    toggle.id = 'darkModeToggle';
    toggle.className = 'dark-mode-toggle';
    toggle.setAttribute('aria-label', 'Toggle dark mode');
    toggle.setAttribute('title', 'Toggle dark mode');

    // Add icons
    toggle.innerHTML = `
        <svg class="sun-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="5"></circle>
            <line x1="12" y1="1" x2="12" y2="3"></line>
            <line x1="12" y1="21" x2="12" y2="23"></line>
            <line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line>
            <line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line>
            <line x1="1" y1="12" x2="3" y2="12"></line>
            <line x1="21" y1="12" x2="23" y2="12"></line>
            <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line>
            <line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line>
        </svg>
        <svg class="moon-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>
        </svg>
    `;

    // Add click event
    toggle.addEventListener('click', toggleDarkMode);

    // Add to page
    document.body.appendChild(toggle);
}

function toggleDarkMode() {
    const isDarkMode = document.body.classList.contains('dark-mode');

    if (isDarkMode) {
        disableDarkMode();
        localStorage.setItem('darkMode', 'disabled');
    } else {
        enableDarkMode();
        localStorage.setItem('darkMode', 'enabled');
    }
}

function enableDarkMode() {
    document.body.classList.add('dark-mode');

    // Update meta theme-color for mobile browsers (optional)
    let metaThemeColor = document.querySelector('meta[name="theme-color"]');
    if (!metaThemeColor) {
        metaThemeColor = document.createElement('meta');
        metaThemeColor.name = 'theme-color';
        document.head.appendChild(metaThemeColor);
    }
    metaThemeColor.content = '#111827';

    console.log('Dark mode enabled');
}

function disableDarkMode() {
    document.body.classList.remove('dark-mode');

    // Update meta theme-color for mobile browsers (optional)
    let metaThemeColor = document.querySelector('meta[name="theme-color"]');
    if (metaThemeColor) {
        metaThemeColor.content = '#ffffff';
    }

    console.log('Dark mode disabled');
}

// Export functions for manual control (optional)
window.darkModeControls = {
    enable: enableDarkMode,
    disable: disableDarkMode,
    toggle: toggleDarkMode,
    isEnabled: () => document.body.classList.contains('dark-mode')
};