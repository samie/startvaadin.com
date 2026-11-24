// @ts-check
const path = require('path');
const { expect } = require('@playwright/test');

const SCREENSHOTS = process.env.SCREENSHOT_DIR || path.resolve(__dirname, '../../target/example-projects');

/**
 * Open the generated app's root route, collecting browser console errors.
 * @param {import('@playwright/test').Page} page
 * @returns {Promise<string[]>} console errors seen so far (live array)
 */
async function openApp(page) {
  const errors = [];
  page.on('console', (message) => {
    if (message.type() === 'error') {
      errors.push(message.text());
    }
  });
  page.on('pageerror', (error) => errors.push(error.message));
  await page.goto('/', { waitUntil: 'networkidle' });
  return errors;
}

/**
 * Assert no console errors, then write the template's screenshot to SCREENSHOT_DIR.
 * @param {import('@playwright/test').Page} page
 * @param {string[]} errors
 * @param {string} id template id
 */
async function finish(page, errors, id) {
  expect(errors, 'browser console errors').toEqual([]);
  await page.screenshot({ path: path.join(SCREENSHOTS, `${id}.png`) });
}

module.exports = { openApp, finish };
