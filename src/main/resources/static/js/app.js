// Animaciones: sombra de la barra al bajar y aparición de elementos al hacer scroll.
(function () {
  var nav = document.querySelector('.nav');
  function alBajar() { if (nav) nav.classList.toggle('scrolled', window.scrollY > 10); }
  window.addEventListener('scroll', alBajar, { passive: true });
  alBajar();

  var items = document.querySelectorAll('.reveal');
  if (!('IntersectionObserver' in window)) {
    items.forEach(function (e) { e.classList.add('in'); });
    return;
  }
  var obs = new IntersectionObserver(function (entradas) {
    entradas.forEach(function (e) {
      if (e.isIntersecting) { e.target.classList.add('in'); obs.unobserve(e.target); }
    });
  }, { threshold: 0.12 });
  items.forEach(function (e) { obs.observe(e); });
})();
