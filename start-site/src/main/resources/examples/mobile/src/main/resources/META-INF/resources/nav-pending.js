/*
  Latency compensation for the menus of MainLayout: a tapped item looks current at
  once and the old view fades out, without waiting for the server round trip.

  It only marks elements with a nav-pending attribute, which styles.css draws, and
  it is purely client side: a message to the server here would make the navigation
  wait behind it. The mark goes away when the tapped item becomes current.
*/
(() => {
  let timer;

  const clear = () => {
    clearTimeout(timer);
    arrived.disconnect();
    document.querySelectorAll('[nav-pending]').forEach((element) => element.removeAttribute('nav-pending'));
  };

  const arrived = new MutationObserver(clear);

  document.addEventListener('click', (event) => {
    if (event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) {
      return;
    }
    const path = event.composedPath();
    const sideItem = path.find((node) => node.localName === 'vaadin-side-nav-item');
    const tab = path.find((node) => node.localName === 'a' && node.parentElement?.classList.contains('tab-bar'));
    const item = sideItem ?? tab;
    const currentAttribute = sideItem ? 'current' : 'highlight';
    const layout = item?.closest('vaadin-app-layout');
    if (!layout || item.hasAttribute(currentAttribute)) {
      return;
    }
    clear();
    item.setAttribute('nav-pending', '');
    layout.setAttribute('nav-pending', '');
    arrived.observe(item, { attributes: true, attributeFilter: [currentAttribute] });
    timer = setTimeout(clear, 10000);
  }, true);
})();
