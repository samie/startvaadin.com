// @ts-check
const { test, expect } = require('@playwright/test');
const { openApp, finish, checkColorSchemes, checkPhone } = require('./helpers');

const WIDGETS = ['CPU load', 'Heap used', 'Live threads', 'Uptime', 'CPU trend', 'Heap trend', 'Resources', 'System'];

/** @param {import('@playwright/test').Page} page */
const updatedTime = (page) => page.getByText(/^Updated \d\d:\d\d:\d\d$/);

/** @param {import('@playwright/test').Page} page */
const widgets = (page) => page.locator(':is(.metric-grid, .wide-grid) > vaadin-card');

/** @param {import('@playwright/test').Page} page */
async function waitForCharts(page) {
  await expect(updatedTime(page)).toBeVisible({ timeout: 10_000 });
  // A line in every sparkline: CPU, heap and threads, and the two trend charts
  await expect(page.locator('vaadin-card svg .sparkline-line')).toHaveCount(5, { timeout: 10_000 });
}

test('dashboard shows live JVM metrics', async ({ page }) => {
  const errors = await openApp(page);
  await expect(page).toHaveTitle('JVM Dashboard');
  // The logo is decorative: the heading's accessible name is its text alone
  await expect(page.getByRole('heading', { level: 1, name: 'JVM Dashboard', exact: true })).toBeVisible();
  await expect(page.locator('h1 vaadin-icon')).toBeVisible();
  await waitForCharts(page);
  await expect(widgets(page)).toHaveCount(WIDGETS.length);
  for (const title of WIDGETS) {
    await expect(page.getByRole('heading', { level: 2, name: title, exact: true })).toBeVisible();
  }
  for (const resource of ['Heap of max', 'Physical memory', 'Disk']) {
    await expect(page.getByRole('progressbar', { name: resource })).toBeVisible();
  }
  await expect(page.locator('vaadin-badge').filter({ hasText: /^\s*(OK|High|Critical)\s*$/ })).toHaveCount(3);

  // Pushed from the server: the time moves on without any user action
  const first = await updatedTime(page).textContent();
  await expect(updatedTime(page)).not.toHaveText(first ?? '', { timeout: 5_000 });

  await finish(page, errors, 'dashboard');

  // Pause freezes this view only
  await page.getByText('Pause', { exact: true }).click();
  await expect(page.getByText('Paused', { exact: true })).toBeVisible();
  const frozen = await updatedTime(page).textContent();
  await page.waitForTimeout(4_500);
  await expect(updatedTime(page)).toHaveText(frozen ?? '');
  await page.getByText('Pause', { exact: true }).click();
  await expect(page.getByText('Live', { exact: true })).toBeVisible();
  await expect(updatedTime(page)).not.toHaveText(frozen ?? '', { timeout: 5_000 });

  await checkColorSchemes(page, errors, 'dashboard');
  await checkPhone(page, 'dashboard', async (phone) => {
    await waitForCharts(phone);
    await expect(widgets(phone)).toHaveCount(WIDGETS.length);
    const width = phone.viewportSize()?.width ?? 0;
    const boxes = await Promise.all((await widgets(phone).all()).map((widget) => widget.boundingBox()));
    boxes.forEach((box, index) => {
      expect(box, `widget ${index + 1} is laid out`).not.toBeNull();
      if (!box) {
        return;
      }
      expect(box.x, `widget ${index + 1} starts on screen`).toBeGreaterThanOrEqual(0);
      expect(box.x + box.width, `widget ${index + 1} fits the phone width`).toBeLessThanOrEqual(width);
      const previous = boxes[index - 1];
      if (previous) {
        expect(box.x, `widget ${index + 1} sits in the same column`).toBeCloseTo(previous.x, 0);
        expect(box.y, `widget ${index + 1} sits below the one before`).toBeGreaterThanOrEqual(previous.y + previous.height);
      }
    });
  });
});
