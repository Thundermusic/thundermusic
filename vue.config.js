module.exports = {
    baseUrl: '.',
    pwa: {
        workboxPluginMode: 'InjectManifest',
        workboxOptions: {
            swSrc: './src/sw.js'
        },
        themeColor: '#f67504'
    }
};3