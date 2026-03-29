const { defineConfig } = require('cypress')

module.exports = defineConfig({
    e2e: {
        baseUrl: 'http://host.docker.internal:8080',
        specPattern: 'e2e/**/*.cy.{js,ts}',
        supportFile: 'support/e2e.js',

        defaultCommandTimeout: 10000,
        pageLoadTimeout: 30000,

        video: false,
        screenshotOnRunFailure: true,
        videosFolder: 'videos',
        screenshotsFolder: 'screenshots',

        viewportWidth: 1280,
        viewportHeight: 720,
    },
})