// @ts-check
const { test, expect } = require('@playwright/test');
const { openApp, finish, checkColorSchemes, checkPhone } = require('./helpers');

/** @param {import('@playwright/test').Page} page */
async function openContact(page, name) {
  await page.locator('vaadin-grid-cell-content', { hasText: name }).click();
  await expect(page.getByRole('textbox', { name: 'First name' })).toBeVisible();
  // the editor slides in (an overlay on a phone); capture it once it has settled
  await expect(page.getByRole('heading', { level: 2, name })).toBeInViewport({ ratio: 1 });
  await page.waitForFunction(() => document.getAnimations().length === 0);
}

test('crud shows contacts beside the editor', async ({ page }) => {
  const errors = await openApp(page);
  await expect(page).toHaveTitle('Contacts');
  await expect(page.getByRole('heading', { level: 1, name: 'Contacts' })).toBeVisible();
  await expect(page.locator('vaadin-grid')).toContainText('aino.virtanen@example.com');
  await expect(page.getByText('Select a contact to edit it')).toBeVisible();

  await page.getByRole('button', { name: 'New contact' }).click();
  await expect(page.getByRole('heading', { level: 2, name: 'New contact' })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Delete' })).toBeHidden();

  await openContact(page, 'Aino Virtanen');
  await expect(page.getByRole('button', { name: 'Save' })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Delete' })).toBeVisible();
  await finish(page, errors, 'crud');
  await checkColorSchemes(page, errors, 'crud');
  await checkPhone(page, 'crud', (phone) => openContact(phone, 'Aino Virtanen'));
});
