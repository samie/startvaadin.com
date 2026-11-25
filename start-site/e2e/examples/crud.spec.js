// @ts-check
const { test, expect } = require('@playwright/test');
const { openApp, finish, checkColorSchemes, checkPhone } = require('./helpers');

test('crud shows contacts and the editor', async ({ page }) => {
  const errors = await openApp(page);
  await expect(page).toHaveTitle('Contacts');
  await expect(page.getByRole('heading', { level: 1, name: 'Contacts' })).toBeVisible();
  await expect(page.locator('vaadin-grid')).toContainText('john@example.com');
  await expect(page.getByRole('textbox', { name: 'First name' })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Save' })).toBeVisible();
  await finish(page, errors, 'crud');
  await checkColorSchemes(page, errors, 'crud');
  await checkPhone(page, 'crud');
});
