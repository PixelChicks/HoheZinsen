// ============================================================================
// DARK MODE FUNCTIONALITY
// ============================================================================

// The logic inside initializeDarkMode runs when the DOM is ready,
// guaranteeing elements like #darkModeToggleContainer are available.
document.addEventListener('DOMContentLoaded', function() {
    initializeDarkMode();
});

function initializeDarkMode() {
    // The class should have been applied by the inline script in the <head>.
    const isDarkModeApplied = document.documentElement.classList.contains('dark-mode');

    // 1. Stabilize the current state (set theme color meta tag, etc.)
    if (isDarkModeApplied) {
        enableDarkMode();
    } else {
        disableDarkMode();
    }

    // 2. Create the toggle button UI
    createDarkModeToggle();

    // 3. Listen for system theme changes (only if the user hasn't set a manual preference)
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
    const container = document.getElementById('darkModeToggleContainer');
    if (!container) {
        return;
    }

    if (document.getElementById('darkModeToggle')) {
        return;
    }

    const toggle = document.createElement('button');
    toggle.id = 'darkModeToggle';
    toggle.className = 'dark-mode-toggle';
    toggle.setAttribute('aria-label', 'Toggle dark mode');
    toggle.setAttribute('title', 'Toggle dark mode');

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

    toggle.addEventListener('click', toggleDarkMode);
    container.appendChild(toggle);
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
    document.documentElement.classList.add('dark-mode');

    let metaThemeColor = document.querySelector('meta[name="theme-color"]');
    if (!metaThemeColor) {
        metaThemeColor = document.createElement('meta');
        metaThemeColor.name = 'theme-color';
        document.head.appendChild(metaThemeColor);
    }
    metaThemeColor.content = '#111827';
}

function disableDarkMode() {
    document.body.classList.remove('dark-mode');
    document.documentElement.classList.remove('dark-mode');

    let metaThemeColor = document.querySelector('meta[name="theme-color"]');
    if (metaThemeColor) {
        metaThemeColor.content = '#ffffff';
    }
}

window.darkModeControls = {
    enable: enableDarkMode,
    disable: disableDarkMode,
    toggle: toggleDarkMode,
    isEnabled: () => document.body.classList.contains('dark-mode')
};