import Vue from "vue";
import Vuex from "vuex";
import modules from "./modules";

Vue.use(Vuex);

const store = new Vuex.Store({
  modules
});

for (const module in modules) {
  store.dispatch(`${module}/load`).catch(err => {
    console.error(`Error during loading of module '${module}'`);
    console.log(err);
  });
}

export default store;
