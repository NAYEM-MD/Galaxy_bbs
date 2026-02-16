/**
 * Profile page: Upload Photo (auto-submit on file select) and View Photo modal.
 * Plain JavaScript, no page reload for modal preview.
 */
(function () {
    'use strict';

    var uploadInput = document.querySelector('.profile-photo-input');
    if (uploadInput) {
        uploadInput.addEventListener('change', function () {
            if (this.files && this.files.length) {
                this.closest('form').submit();
            }
        });
    }

    var viewBtn = document.getElementById('viewPhotoBtn');
    var modal = document.getElementById('profilePhotoModal');
    var modalImg = document.getElementById('profilePhotoModalImg');
    var modalClose = document.getElementById('profilePhotoModalClose');
    var backdrop = modal && modal.querySelector('.profile-photo-modal-backdrop');
    var avatarImg = document.getElementById('profileAvatarImg');

    function openModal() {
        if (!modal || !modalImg || !avatarImg) return;
        var src = avatarImg.getAttribute('src');
        if (!src) return;
        modalImg.setAttribute('src', src);
        modal.setAttribute('aria-hidden', 'false');
        document.body.style.overflow = 'hidden';
    }

    function closeModal() {
        if (!modal) return;
        modal.setAttribute('aria-hidden', 'true');
        document.body.style.overflow = '';
    }

    if (viewBtn) viewBtn.addEventListener('click', openModal);
    if (modalClose) modalClose.addEventListener('click', closeModal);
    if (backdrop) backdrop.addEventListener('click', closeModal);

    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && modal && modal.getAttribute('aria-hidden') === 'false') {
            closeModal();
        }
    });
})();
