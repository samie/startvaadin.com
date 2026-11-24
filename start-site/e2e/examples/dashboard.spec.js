// @ts-check
const { test, expect } = require('@playwright/test');
const { openApp, finish } = require('./helpers');

test('dashboard shows stats and progress', async ({ page }) => {
  const errors = await openApp(page);
  await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible();
  await expect(page.getByText('Revenue')).toBeVisible();
  await expect(page.locator('vaadin-progress-bar')).toHaveCount(3);
  await finish(page, errors, 'dashboard');
});
