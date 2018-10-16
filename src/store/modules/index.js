const requireModule = require.context(".", false, /\.js$/);
const modules = {};

requireModule.keys().forEach(fileName => {
  if (fileName === "./index.js") return;
  const moduleName = fileName.replace(/(\.\/|\.js)/g, "");
  const module = requireModule(fileName);
  modules[moduleName] = {
    namespaced: true,
    ...(module.default || module)
  };
});

export default modules;
