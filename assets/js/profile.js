/**
 * Profile page: Upload Photo with crop/resize step, View Photo modal.
 * On file select → open crop modal (drag + zoom to choose what shows in circle) → Save uploads, Cancel aborts.
 */
(function () {
    'use strict';

    var uploadForm = document.querySelector('.profile-upload-form');
    var uploadInput = document.querySelector('.profile-photo-input');
    var cropModal = document.getElementById('profileCropModal');
    var cropCanvas = document.getElementById('profileCropCanvas');
    var cropZoom = document.getElementById('profileCropZoom');
    var cropCancelBtn = document.getElementById('profileCropCancel');
    var cropSaveBtn = document.getElementById('profileCropSave');
    var cropBackdrop = cropModal && cropModal.querySelector('.profile-crop-modal-backdrop');

    var cropState = {
        img: null,
        scale: 1,
        panX: 0,
        panY: 0,
        isDragging: false,
        lastX: 0,
        lastY: 0
    };

    var CROP_SIZE = 280;
    var CENTER = CROP_SIZE / 2;
    var RADIUS = CENTER;

    function drawCrop() {
        if (!cropCanvas || !cropState.img) return;
        var ctx = cropCanvas.getContext('2d');
        var img = cropState.img;
        var scale = cropState.scale;
        var panX = cropState.panX;
        var panY = cropState.panY;

        ctx.clearRect(0, 0, CROP_SIZE, CROP_SIZE);
        ctx.save();
        ctx.beginPath();
        ctx.arc(CENTER, CENTER, RADIUS, 0, 2 * Math.PI);
        ctx.clip();

        var w = img.naturalWidth;
        var h = img.naturalHeight;
        var minDim = Math.min(w, h);
        var scaleFactor = (CROP_SIZE * scale) / minDim;
        var drawW = w * scaleFactor;
        var drawH = h * scaleFactor;
        var x = CENTER + panX - drawW / 2;
        var y = CENTER + panY - drawH / 2;
        ctx.drawImage(img, 0, 0, w, h, x, y, drawW, drawH);
        ctx.restore();
    }

    function openCropModal(file) {
        if (!cropModal || !cropCanvas || !file) return;
        var reader = new FileReader();
        reader.onload = function (e) {
            var img = new Image();
            img.onload = function () {
                cropState.img = img;
                cropState.scale = 1;
                cropState.panX = 0;
                cropState.panY = 0;
                if (cropZoom) cropZoom.value = '1';
                drawCrop();
                cropModal.setAttribute('aria-hidden', 'false');
                document.body.style.overflow = 'hidden';
            };
            img.src = e.target.result;
        };
        reader.readAsDataURL(file);
    }

    function closeCropModal() {
        if (!cropModal) return;
        cropModal.setAttribute('aria-hidden', 'true');
        document.body.style.overflow = '';
        cropState.img = null;
        if (uploadInput) uploadInput.value = '';
    }

    function getCroppedBlob(callback) {
        if (!cropState.img) return callback(null);
        var canvas = document.createElement('canvas');
        canvas.width = CROP_SIZE;
        canvas.height = CROP_SIZE;
        var ctx = canvas.getContext('2d');
        var img = cropState.img;
        var scale = cropState.scale;
        var panX = cropState.panX;
        var panY = cropState.panY;

        ctx.save();
        ctx.beginPath();
        ctx.arc(CENTER, CENTER, RADIUS, 0, 2 * Math.PI);
        ctx.clip();
        var w = img.naturalWidth;
        var h = img.naturalHeight;
        var minDim = Math.min(w, h);
        var scaleFactor = (CROP_SIZE * scale) / minDim;
        var drawW = w * scaleFactor;
        var drawH = h * scaleFactor;
        var x = CENTER + panX - drawW / 2;
        var y = CENTER + panY - drawH / 2;
        ctx.drawImage(img, 0, 0, w, h, x, y, drawW, drawH);
        ctx.restore();

        canvas.toBlob(function (blob) {
            callback(blob);
        }, 'image/jpeg', 0.92);
    }

    function saveCrop() {
        var url = uploadForm && uploadForm.getAttribute('data-upload-url');
        if (!url) return;
        getCroppedBlob(function (blob) {
            if (!blob) return;
            var fd = new FormData();
            fd.append('profilePhoto', blob, 'profile.jpg');
            var csrfInput = uploadForm && uploadForm.querySelector('input[name="csrfToken"]');
            if (csrfInput && csrfInput.value) fd.append('csrfToken', csrfInput.value);
            cropSaveBtn.disabled = true;
            fetch(url, { method: 'POST', body: fd, redirect: 'follow', credentials: 'same-origin' })
                .then(function (r) {
                    if (r.redirected) {
                        window.location.href = r.url;
                    } else {
                        cropSaveBtn.disabled = false;
                    }
                })
                .catch(function () {
                    cropSaveBtn.disabled = false;
                });
        });
    }

    if (uploadInput) {
        uploadInput.addEventListener('change', function () {
            if (this.files && this.files[0]) {
                openCropModal(this.files[0]);
            }
        });
    }

    if (cropZoom) {
        cropZoom.addEventListener('input', function () {
            cropState.scale = parseFloat(this.value) || 1;
            drawCrop();
        });
    }

    if (cropCanvas) {
        cropCanvas.addEventListener('mousedown', function (e) {
            cropState.isDragging = true;
            cropState.lastX = e.offsetX;
            cropState.lastY = e.offsetY;
        });
        cropCanvas.addEventListener('mousemove', function (e) {
            if (!cropState.isDragging) return;
            cropState.panX += e.offsetX - cropState.lastX;
            cropState.panY += e.offsetY - cropState.lastY;
            cropState.lastX = e.offsetX;
            cropState.lastY = e.offsetY;
            drawCrop();
        });
        cropCanvas.addEventListener('mouseup', function () {
            cropState.isDragging = false;
        });
        cropCanvas.addEventListener('mouseleave', function () {
            cropState.isDragging = false;
        });
    }

    if (cropCancelBtn) cropCancelBtn.addEventListener('click', closeCropModal);
    if (cropSaveBtn) cropSaveBtn.addEventListener('click', saveCrop);
    if (cropBackdrop) cropBackdrop.addEventListener('click', closeCropModal);

    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && cropModal && cropModal.getAttribute('aria-hidden') === 'false') {
            closeCropModal();
        }
    });

    /* View Photo modal (unchanged) */
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
