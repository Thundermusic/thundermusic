import * as Sentry from "@sentry/browser";

const dsn = process.env.VUE_APP_SENTRY_DSN;

if (dsn) {
  Sentry.init({ dsn });
}

import "@babel/polyfill";
import Vue from "vue";
import "./vuetify";

import App from "./App.vue";

import "roboto-fontface/css/roboto/roboto-fontface.css";
import "material-design-icons-iconfont/dist/material-design-icons.css";

import router from "./router";
import store from "./store";

Vue.config.productionTip = false;
import "vue-virtual-scroller/dist/vue-virtual-scroller.css";
import "./registerServiceWorker";

import PortalVue from "portal-vue";

Vue.use(PortalVue);

new Vue({
  render: h => h(App),
  router,
  store
}).$mount("#app");
