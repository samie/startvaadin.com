// @ts-check
const { test, expect } = require('@playwright/test');
const { openApp, finish } = require('./helpers');

test('mobile app lists features, opens one and reaches profile', async ({ page }) => {
  const errors = await openApp(page);
  await expect(page.getByRole('heading', { name: 'Home' })).toBeVisible();
  await expect(page.locator('vaadin-card')).toHaveCount(6);
  await expect(page.locator('link[rel="manifest"]')).toHaveCount(1);
  await finish(page, errors, 'mobile');
  await page.locator('vaadin-card').first().click();
  await expect(page.getByRole('heading', { level: 1 })).toHaveText('Bottom tab bar');
  await page.locator('vaadin-side-nav-item', { hasText: 'Profile' }).click();
  await expect(page.getByRole('heading', { name: 'Profile' })).toBeVisible();
});

test('mobile app shows the bottom tab bar on a phone', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  const errors = await openApp(page);
  await page.getByRole('link', { name: 'Profile' }).click();
  await expect(page.getByRole('heading', { name: 'Profile' })).toBeVisible();
  expect(errors, 'browser console errors').toEqual([]);
});

test('turning on notifications answers with a message, not a server error', async ({ page, context, baseURL }) => {
  await context.grantPermissions(['notifications'], { origin: baseURL });
  const errors = await openApp(page);
  await page.getByRole('link', { name: 'Profile' }).first().click();
  const toggle = page.getByRole('switch', { name: 'Notifications' });
  await expect(toggle).toBeEnabled();
  await toggle.click();
  // Playwright's browser context is incognito-like, without push, so the message tells why subscribing failed
  await expect(page.locator('vaadin-notification-card')).toContainText('Notifications');
  const appErrors = errors.filter((error) => !error.includes('Push API in incognito mode'));
  expect(appErrors, 'browser console errors').toEqual([]);
});
