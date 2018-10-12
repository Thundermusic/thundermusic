module.exports = {
    baseUrl: '.',
    pwa: {
        workboxPluginMode: 'InjectManifest',
        workboxOptions: {
            swSrc: './src/sw.js'
        }
    }
};3