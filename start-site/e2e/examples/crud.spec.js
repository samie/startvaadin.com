// @ts-check
const { test, expect } = require('@playwright/test');
const { openApp, finish } = require('./helpers');

test('crud shows contacts and the editor', async ({ page }) => {
  const errors = await openApp(page);
  await expect(page.locator('vaadin-grid')).toContainText('john@example.com');
  await expect(page.getByRole('textbox', { name: 'First name' })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Save' })).toBeVisible();
  await finish(page, errors, 'crud');
});
