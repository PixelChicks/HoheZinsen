(function() {
    // This function applies the persistent theme class.
    function applyPersistentTheme() {
        // Access document.body safely inside the DOMContentLoaded handler
        const body = document.body;
        const savedMode = localStorage.getItem('darkMode');
        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;

        // CRITICAL: Apply the class instantly if the preference is dark
        if (savedMode === 'enabled' || (savedMode === null && prefersDark)) {
            document.documentElement.classList.add('dark-mode');
            body.classList.add('dark-mode');
        }
    }

    // Since this external script is loaded in the <head>, we must handle the timing.
    // We rely on the browser's native DOMContentLoaded or check if the document is already ready.
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', applyPersistentTheme);
    } else {
        applyPersistentTheme();
    }
})();