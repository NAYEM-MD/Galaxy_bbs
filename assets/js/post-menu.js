/**
 * Post 3-dot menu: toggle dropdown (.open), prevent post navigation when clicking menu.
 * Close on outside click. The delete button is only visible when the dropdown is open.
 */
(function () {
    'use strict';

    function closeAllPostMenus() {
        document.querySelectorAll('.post-menu-dropdown.open').forEach(function (el) {
            el.classList.remove('open');
            el.setAttribute('aria-hidden', 'true');
        });
        document.querySelectorAll('.post-menu-trigger[aria-expanded="true"]').forEach(function (el) {
            el.setAttribute('aria-expanded', 'false');
        });
    }

    function init() {
        var cards = document.querySelectorAll('.post-card[data-href]');
        cards.forEach(function (card) {
            card.addEventListener('click', function (e) {
                if (e.target.closest('.post-menu')) {
                    return;
                }
                var href = card.getAttribute('data-href');
                if (href) {
                    window.location.href = href;
                }
            });
        });

        document.addEventListener('click', function (e) {
            var trigger = e.target.closest('.post-menu-trigger');
            if (trigger) {
                e.stopPropagation();
                e.preventDefault();
                var menu = trigger.closest('.post-menu');
                var dropdown = menu ? menu.querySelector('.post-menu-dropdown') : null;
                if (!dropdown) return;
                var isOpen = dropdown.classList.contains('open');
                closeAllPostMenus();
                if (!isOpen) {
                    dropdown.classList.add('open');
                    dropdown.setAttribute('aria-hidden', 'false');
                    trigger.setAttribute('aria-expanded', 'true');
                }
                return;
            }
            if (!e.target.closest('.post-menu-dropdown')) {
                closeAllPostMenus();
            }
        });

        document.addEventListener('click', function (e) {
            if (e.target.closest('.post-menu-dropdown')) {
                e.stopPropagation();
            }
        }, true);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
