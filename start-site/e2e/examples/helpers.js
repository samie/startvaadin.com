// @ts-check
const fs = require('fs');
const path = require('path');
const { expect } = require('@playwright/test');

const SCREENSHOTS = process.env.SCREENSHOT_DIR || path.resolve(__dirname, '../../target/example-projects');
// The light/dark and phone shots are for review only and never replace a catalog image
const REVIEW_SCREENSHOTS = process.env.REVIEW_SCREENSHOT_DIR || path.resolve(__dirname, '../../target/example-projects');
// The high-resolution shots shown in the repository README, committed with the template
const README_SCREENSHOTS = process.env.README_SCREENSHOT_DIR || path.resolve(__dirname, '../../../docs/templates');
const README_SCALE = 2;

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
  await readmeScreenshot(page, id);
}

/**
 * Write the template's README screenshot, docs/templates/<id>.png: the same view as
 * the gate screenshot, rendered at README_SCALE device pixels per CSS pixel so it
 * stays sharp on GitHub. The pixel ratio is switched through the DevTools protocol
 * (Chromium only) and restored afterwards, so the page keeps its state and layout.
 * @param {import('@playwright/test').Page} page
 * @param {string} id template id
 */
async function readmeScreenshot(page, id) {
  const viewport = page.viewportSize();
  if (!viewport) {
    return;
  }
  const session = await page.context().newCDPSession(page);
  const metrics = (deviceScaleFactor) => session.send('Emulation.setDeviceMetricsOverride',
    { width: viewport.width, height: viewport.height, deviceScaleFactor, mobile: false });
  const original = await page.evaluate(() => window.devicePixelRatio);
  try {
    await metrics(README_SCALE);
    await page.waitForFunction((scale) => window.devicePixelRatio === scale, README_SCALE);
    // Captured over the protocol: page.screenshot() would scale back to Playwright's own pixel ratio
    const { data } = await session.send('Page.captureScreenshot', { format: 'png' });
    fs.mkdirSync(README_SCREENSHOTS, { recursive: true });
    fs.writeFileSync(path.join(README_SCREENSHOTS, `${id}.png`), Buffer.from(data, 'base64'));
  }
  finally {
    await metrics(original);
    await session.detach();
  }
}

/**
 * Whether the page renders dark: the luminance of the first painted background
 * among body and html, and of the CSS system colour Canvas, which follows the
 * used color-scheme.
 * @param {import('@playwright/test').Page} page
 */
async function renderedScheme(page) {
  return page.evaluate(() => {
    // Any CSS colour (Aura uses oklch) to sRGB, by painting it on a canvas
    const context = document.createElement('canvas').getContext('2d', { willReadFrequently: true });
    const luminance = (color) => {
      context.clearRect(0, 0, 1, 1);
      context.fillStyle = color;
      context.fillRect(0, 0, 1, 1);
      const [r, g, b] = context.getImageData(0, 0, 1, 1).data;
      return (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255;
    };
    const painted = (element) => {
      const color = getComputedStyle(element).backgroundColor;
      return color === 'transparent' || /rgba\(.*,\s*0\)$/.test(color) ? null : color;
    };
    const probe = document.createElement('div');
    probe.style.backgroundColor = 'Canvas';
    document.body.append(probe);
    const canvas = getComputedStyle(probe).backgroundColor;
    probe.remove();
    const background = painted(document.body) ?? painted(document.documentElement);
    return {
      canvasDark: luminance(canvas) < 0.5,
      background,
      backgroundDark: background ? luminance(background) < 0.5 : null,
    };
  });
}

/**
 * Pick an option of the template's light/dark selector: a vaadin-select labelled
 * (or with the accessible name) "Appearance".
 * @param {import('@playwright/test').Page} page
 * @param {'System' | 'Light' | 'Dark'} option
 */
async function pickAppearance(page, option) {
  const found = await page.evaluate(() => {
    const select = [...document.querySelectorAll('vaadin-select')].find(
      (element) => element.label === 'Appearance' || element.accessibleName === 'Appearance');
    select?.setAttribute('data-appearance-select', '');
    return Boolean(select);
  });
  expect(found, 'a vaadin-select labelled "Appearance" (the light/dark selector)').toBe(true);
  await page.locator('vaadin-select[data-appearance-select]').click();
  await page.getByRole('option', { name: option, exact: true }).click();
  await expect(page.getByRole('option', { name: option, exact: true })).toBeHidden();
}

async function expectScheme(page, dark, when) {
  await expect.poll(async () => (await renderedScheme(page)).canvasDark, `color-scheme is ${dark ? 'dark' : 'light'} ${when}`)
    .toBe(dark);
  const { background, backgroundDark } = await renderedScheme(page);
  expect(background, `the page paints a background ${when}`).not.toBeNull();
  expect(backgroundDark, `the background (${background}) looks ${dark ? 'dark' : 'light'} ${when}`).toBe(dark);
}

/**
 * The light/dark check every template runs in the gate (not in the generated app's
 * tests): the "Appearance" selector switches the page to dark and back to light, and
 * "System" follows the operating system. Saves <id>-light.png and <id>-dark.png.
 * Call it on the view that shows the selector.
 * @param {import('@playwright/test').Page} page
 * @param {string[]} errors console errors collected by openApp
 * @param {string} id template id
 */
async function checkColorSchemes(page, errors, id) {
  await page.emulateMedia({ colorScheme: 'light' });
  await pickAppearance(page, 'Dark');
  await expectScheme(page, true, 'after picking Dark');
  await page.screenshot({ path: path.join(REVIEW_SCREENSHOTS, `${id}-dark.png`) });
  await pickAppearance(page, 'Light');
  await expectScheme(page, false, 'after picking Light');
  await page.screenshot({ path: path.join(REVIEW_SCREENSHOTS, `${id}-light.png`) });
  await pickAppearance(page, 'System');
  await page.emulateMedia({ colorScheme: 'dark' });
  await expectScheme(page, true, 'on System while the OS is dark');
  await page.emulateMedia({ colorScheme: 'light' });
  await expectScheme(page, false, 'on System while the OS is light');
  expect(errors, 'browser console errors').toEqual([]);
}

/**
 * Opens the app's root route on a 390 x 844 phone screen, light and dark, asserts
 * nothing scrolls sideways, and saves <id>-phone.png and <id>-phone-dark.png.
 * @param {import('@playwright/test').Page} page any page of the test, for its browser and origin
 * @param {string} id template id
 * @param {(phone: import('@playwright/test').Page) => Promise<void>} [prepare] brings the phone page to the state to capture
 */
async function checkPhone(page, id, prepare) {
  const browser = page.context().browser();
  const baseURL = new URL(page.url()).origin;
  for (const colorScheme of /** @type {const} */ (['light', 'dark'])) {
    const context = await browser.newContext({ viewport: { width: 390, height: 844 }, deviceScaleFactor: 1, colorScheme, baseURL });
    try {
      const phone = await context.newPage();
      const errors = await openApp(phone);
      if (prepare) {
        await prepare(phone);
      }
      const overflow = await phone.evaluate(() => document.documentElement.scrollWidth - window.innerWidth);
      expect(overflow, `page scrolls sideways on a phone (${colorScheme})`).toBeLessThanOrEqual(0);
      await expectScheme(phone, colorScheme === 'dark', `on a phone with the OS ${colorScheme}`);
      expect(errors, 'browser console errors').toEqual([]);
      await phone.screenshot({ path: path.join(REVIEW_SCREENSHOTS, colorScheme === 'dark' ? `${id}-phone-dark.png` : `${id}-phone.png`) });
    }
    finally {
      await context.close();
    }
  }
}

module.exports = { openApp, finish, checkColorSchemes, checkPhone, pickAppearance };
