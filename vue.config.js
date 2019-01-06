const { resolve } = require("path");
const { BASE_URL = "/", PLATFORM } = process.env;

module.exports = {
  baseUrl: BASE_URL,
  pwa: {
    workboxPluginMode: "InjectManifest",
    workboxOptions: {
      swSrc: "./src/sw.js"
    },
    themeColor: "#f67504",
    iconPaths: {
      favicon32: "img/icons/favicon-32x32.png",
      favicon16: "img/icons/favicon-16x16.png",
      appleTouchIcon: "img/icons/apple-touch-icon-152x152.png",
      maskIcon: null,
      msTileImage: "img/icons/msapplication-icon-144x144.png"
    }
  },
  pluginOptions: {
    cordovaPath: "cordova"
  },
  chainWebpack: config => {
    config.resolve.alias.set(
      "platform",
      resolve(__dirname, `src/platform/${PLATFORM}`)
    );
  }
};
