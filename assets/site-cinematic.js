(() => {
  'use strict';
  const menu = document.querySelector('.nav-toggle');
  const nav = document.querySelector('.main-nav');
  if (menu && nav) {
    const setMenu = open => {
      nav.classList.toggle('is-open', open);
      menu.setAttribute('aria-expanded', String(open));
      menu.setAttribute('aria-label', open ? 'Close menu' : 'Open menu');
    };
    menu.addEventListener('click', () => setMenu(menu.getAttribute('aria-expanded') !== 'true'));
    nav.querySelectorAll('a').forEach(link => link.addEventListener('click', () => setMenu(false)));
    document.addEventListener('keydown', e => { if (e.key === 'Escape') setMenu(false); });
    document.addEventListener('click', e => { if (!nav.contains(e.target) && !menu.contains(e.target)) setMenu(false); });
  }

  const gallery = document.querySelector('#gallery-track');
  document.querySelector('[data-gallery-prev]')?.addEventListener('click', () => gallery?.scrollBy({left:-290,behavior:'smooth'}));
  document.querySelector('[data-gallery-next]')?.addEventListener('click', () => gallery?.scrollBy({left:290,behavior:'smooth'}));
  const dialog = document.querySelector('#screenshot-dialog');
  if (gallery && dialog) {
    const dialogImage = dialog.querySelector('.dialog-img');
    const title = dialog.querySelector('#dialog-title');
    gallery.querySelectorAll('.gallery-slide').forEach(slide => slide.addEventListener('click', () => {
      dialogImage.src = slide.dataset.image;
      dialogImage.alt = `${slide.dataset.title} screenshot`;
      title.textContent = slide.dataset.title;
      if (typeof dialog.showModal === 'function') dialog.showModal();
    }));
    dialog.querySelector('.dialog-close')?.addEventListener('click', () => dialog.close());
    dialog.addEventListener('click', e => { if (e.target === dialog) dialog.close(); });
  }
})();
