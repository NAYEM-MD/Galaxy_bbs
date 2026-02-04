/**
 * Global theme toggle — Light / Dark with localStorage persistence.
 * Run in <head> (no defer) so theme applies before first paint.
 */
(function () {
    'use strict';

    var STORAGE_KEY = 'theme';
    var THEME_DARK = 'dark';

    function getStored() {
        try {
            return localStorage.getItem(STORAGE_KEY);
        } catch (e) {
            return null;
        }
    }

    function setStored(value) {
        try {
            localStorage.setItem(STORAGE_KEY, value);
        } catch (e) {}
    }

    function setTheme(isDark) {
        var root = document.documentElement;
        if (isDark) {
            root.setAttribute('data-theme', THEME_DARK);
            setStored(THEME_DARK);
        } else {
            root.removeAttribute('data-theme');
            setStored('light');
        }
    }

    function applyTheme(isDark) {
        setTheme(!!isDark);
        var checkbox = document.getElementById('darkModeToggle');
        if (checkbox) {
            checkbox.checked = !!isDark;
        }
    }

    function toggleDisplayMode(event) {
        if (event) {
            event.stopPropagation();
        }
        var checkbox = document.getElementById('darkModeToggle');
        if (checkbox) {
            checkbox.checked = !checkbox.checked;
            applyTheme(checkbox.checked);
        } else {
            var current = getStored() === THEME_DARK;
            applyTheme(!current);
        }
    }

    // Apply stored theme immediately (no FOUC)
    var stored = getStored();
    if (stored === THEME_DARK) {
        document.documentElement.setAttribute('data-theme', THEME_DARK);
    }

    // Expose for header Display Mode and any other callers
    window.applyTheme = applyTheme;
    window.toggleDisplayMode = toggleDisplayMode;
    window.getTheme = function () {
        return getStored() === THEME_DARK ? 'dark' : 'light';
    };
})();
