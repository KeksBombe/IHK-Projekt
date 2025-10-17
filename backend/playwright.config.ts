import {defineConfig, devices} from '@playwright/test';

export default defineConfig({
    testDir: './playwright_tests',
    fullyParallel: true,
    forbidOnly: false,
    retries: 0,
    workers: 1,
    reporter: 'json',
    use: {
        trace: 'on-first-retry',
        headless: false,
        locale: 'de-DE',
        timezoneId: 'Europe/Berlin',
        launchOptions: {
            args: ['--lang=de-DE'],
        },
        actionTimeout: 10000,
    },
    expect: {
        timeout: 5000,
    },
    projects: [
        {
            name: 'chromium',
            use: {...devices['Desktop Chrome']},
        },
    ],
});