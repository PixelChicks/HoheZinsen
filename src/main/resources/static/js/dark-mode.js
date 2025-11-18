// ============================================================================
// DARK MODE FUNCTIONALITY
// ============================================================================

document.addEventListener('DOMContentLoaded', function() {
    initializeDarkMode();
});

function initializeDarkMode() {
    const isDarkModeApplied = document.documentElement.classList.contains('dark-mode');

    if (isDarkModeApplied) {
        enableDarkMode();
    } else {
        disableDarkMode();
    }

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

    document.getElementById('darkModeBtn')?.classList.add('active-segment');
    document.getElementById('lightModeBtn')?.classList.remove('active-segment');

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

    document.getElementById('lightModeBtn')?.classList.add('active-segment');
    document.getElementById('darkModeBtn')?.classList.remove('active-segment');

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