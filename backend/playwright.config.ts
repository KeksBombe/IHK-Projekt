import {defineConfig, devices} from '@playwright/test';

export default defineConfig({
    testDir: './playwright_tests',
    /* Run tests in files in parallel */
    fullyParallel: true,
    /* Fail the build on CI if you accidentally left test.only in the source code. */
    forbidOnly: false,
    /* Retry on CI only */
    retries: 1,
    /* Opt out of parallel tests on CI. */
    workers: 1,
    /* Reporter to use. See https://playwright.dev/docs/test-reporters */
    reporter: 'html',
    /* Shared settings for all the projects below. See https://playwright.dev/docs/api/class-testoptions. */
    use: {
        /* Collect trace when retrying the failed test. See https://playwright.dev/docs/trace-viewer */
        trace: 'on-first-retry',

        /* Run in headed mode when not in CI */
        headless: false,

        /* Set locale to German */
        locale: 'de-DE',

        /* Set timezone to German timezone */
        timezoneId: 'Europe/Berlin',

        /* Slow down by 1000ms between each action if SLOWMO env var is set */
        launchOptions: {
            slowMo: 1000, // Default slowmo of 1 second
            args: ['--lang=de-DE'],
        },
    },

    /* Configure projects for major browsers */
    projects: [
        {
            name: 'chromium',
            use: {...devices['Desktop Chrome']},
        },
    ],
});