// @ts-check
const { test, expect, chromium, webkit, devices } = require('@playwright/test');
const { openApp, finish, checkColorSchemes, checkPhone, pickAppearance } = require('./helpers');

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

test('the Appearance setting on Profile switches between light, dark and the system setting', async ({ page }) => {
  const errors = await openApp(page);
  await page.locator('vaadin-side-nav-item', { hasText: 'Profile' }).click();
  await expect(page.getByRole('heading', { name: 'Profile' })).toBeVisible();
  await checkColorSchemes(page, errors, 'mobile');
});

test('on a phone, the app reads well in light and dark', async ({ page }) => {
  await openApp(page);
  await checkPhone(page, 'mobile');
});

test('on a phone, the Appearance options open where the tab bar does not cover them', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  const errors = await openApp(page);
  await page.getByRole('link', { name: 'Profile' }).click();
  await expect(page.getByRole('heading', { name: 'Profile' })).toBeVisible();
  // pickAppearance clicks the option, which fails if the tab bar would receive the tap
  await pickAppearance(page, 'Dark');
  await pickAppearance(page, 'System');
  await expect(page.locator('.tab-bar')).toBeVisible();
  expect(errors, 'browser console errors').toEqual([]);
});

test('on a small phone, a feature page shows its header parts side by side and its Markdown text', async ({ page }) => {
  await page.setViewportSize({ width: 375, height: 667 });
  const errors = await openApp(page);
  await page.locator('vaadin-card', { hasText: 'Installable app' }).click();
  const header = page.locator('.view-header');
  const parts = [
    header.getByRole('link', { name: 'Back to Home' }),
    header.locator('vaadin-icon.feature-icon'),
    header.getByRole('heading', { name: 'Installable app' }),
    header.getByText('Add it to the home screen'),
  ];
  const boxes = await Promise.all(parts.map((part) => part.boundingBox()));
  const overlap = (a, b) => a.x < b.x + b.width && b.x < a.x + a.width && a.y < b.y + b.height && b.y < a.y + a.height;
  for (let i = 0; i < boxes.length; i++) {
    for (let j = i + 1; j < boxes.length; j++) {
      expect(overlap(boxes[i], boxes[j]), `header parts ${i} and ${j} overlap`).toBe(false);
    }
  }
  await expect(page.locator('vaadin-markdown code', { hasText: '@PWA' })).toBeVisible();
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

// iPhone: Mobile Safari's engine with touch taps. Emulation, not a real iPhone: no iOS compositing or gestures.
for (const [phone, browserType, device] of [['an Android phone', chromium, 'Pixel 7'], ['an iPhone', webkit, 'iPhone 15']]) {
  test(`on ${phone}, a tapped tab looks current before the server answers`, async ({ baseURL }) => {
    let browser;
    let page;
    try {
      browser = await browserType.launch();
      page = await (await browser.newContext({ ...devices[device], baseURL })).newPage();
    } catch (error) {
      await browser?.close();
      test.skip(true, `Playwright ${browserType.name()} cannot run here: ${error.message.split('\n')[0]}`);
    }
    try {
      // A slow server, in the page because the @PWA service worker hides these requests from page.route
      await page.addInitScript(() => {
        const send = XMLHttpRequest.prototype.send;
        XMLHttpRequest.prototype.send = function (...args) {
          if (window.serverDelay) {
            setTimeout(() => send.apply(this, args), window.serverDelay);
          } else {
            send.apply(this, args);
          }
        };
      });
      const errors = await openApp(page);
      const profile = page.getByRole('link', { name: 'Profile' });
      const selection = () => page.locator('.tab-bar').evaluate((bar) => getComputedStyle(bar, '::before').translate);

      await page.evaluate(() => { window.serverDelay = 3000; });
      await profile.tap();
      await expect(profile).toHaveAttribute('nav-pending', '');
      await expect(page.locator('vaadin-app-layout')).toHaveAttribute('nav-pending', '');
      await expect.poll(selection).toBe('100%');
      await expect(profile).not.toHaveAttribute('highlight');
      await expect(page.getByRole('heading', { name: 'Home' })).toBeVisible();

      await page.evaluate(() => { window.serverDelay = 0; });
      await expect(page.getByRole('heading', { name: 'Profile' })).toBeVisible();
      await expect(profile).toHaveAttribute('highlight', '');
      await expect(profile).not.toHaveAttribute('nav-pending');
      expect(errors, 'browser console errors').toEqual([]);
    } finally {
      await browser.close();
    }
  });
}
