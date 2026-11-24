// @ts-check
const { test, expect } = require('@playwright/test');
const { openApp, finish } = require('./helpers');

test('helloworld greets the user', async ({ page }) => {
  const errors = await openApp(page);
  await page.getByRole('textbox', { name: 'Your name' }).fill('Ada');
  await page.getByRole('button', { name: 'Say hello' }).click();
  await expect(page.locator('vaadin-notification-card')).toContainText('Hello, Ada');
  await finish(page, errors, 'helloworld');
});
