// @ts-check
const { defineConfig, devices } = require('@playwright/test');

/**
 * Smoke tests for projects generated from the UI templates (see check.sh). Each spec
 * runs against one generated app started at EXAMPLE_BASE_URL. The viewport renders
 * at 1280x800 and captures at half scale, matching the 640x400 catalog screenshots.
 */
module.exports = defineConfig({
  testDir: '.',
  timeout: 60_000,
  expect: { timeout: 15_000 },
  workers: 1,
  retries: 0,
  reporter: [['list']],
  outputDir: '../test-results/examples',
  use: {
    ...devices['Desktop Chrome'],
    baseURL: process.env.EXAMPLE_BASE_URL || 'http://localhost:8080',
    headless: true,
    viewport: { width: 1280, height: 800 },
    deviceScaleFactor: 0.5,
    colorScheme: 'light',
    trace: 'retain-on-failure'
  }
});
