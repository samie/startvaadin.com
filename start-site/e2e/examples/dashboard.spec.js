// @ts-check
const { test, expect } = require('@playwright/test');
const { openApp, finish, checkColorSchemes, checkPhone } = require('./helpers');

test('dashboard shows stats and progress', async ({ page }) => {
  const errors = await openApp(page);
  await expect(page).toHaveTitle('Dashboard');
  await expect(page.getByRole('heading', { level: 1 })).toHaveText('Dashboard');
  await expect(page.getByText('Revenue')).toBeVisible();
  await expect(page.locator('vaadin-progress-bar')).toHaveCount(3);
  for (const [resource, percentage] of [['CPU', '42%'], ['Memory', '67%'], ['Disk', '23%']]) {
    await expect(page.getByRole('progressbar', { name: resource })).toBeVisible();
    await expect(page.getByText(percentage, { exact: true })).toBeVisible();
  }
  await finish(page, errors, 'dashboard');
  await checkColorSchemes(page, errors, 'dashboard');
  await checkPhone(page, 'dashboard', async (phone) => {
    const cards = phone.locator('vaadin-card');
    await expect(cards).toHaveCount(3);
    await expect(cards.last()).toBeVisible();
    const width = phone.viewportSize()?.width ?? 0;
    const boxes = await Promise.all((await cards.all()).map((card) => card.boundingBox()));
    boxes.forEach((box, index) => {
      expect(box, `key-figure card ${index + 1} is laid out`).not.toBeNull();
      if (!box) {
        return;
      }
      expect(box.x, `key-figure card ${index + 1} starts on screen`).toBeGreaterThanOrEqual(0);
      expect(box.x + box.width, `key-figure card ${index + 1} fits the phone width`).toBeLessThanOrEqual(width);
      const previous = boxes[index - 1];
      if (previous) {
        expect(box.x, `key-figure card ${index + 1} sits in the same column`).toBeCloseTo(previous.x, 0);
        expect(box.y, `key-figure card ${index + 1} sits below the one before`).toBeGreaterThanOrEqual(previous.y + previous.height);
      }
    });
  });
});
