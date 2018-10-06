import '@babel/polyfill';
import Vue from 'vue';
import './plugins/vuetify';

import App from './App.vue';

import 'roboto-fontface/css/roboto/roboto-fontface.css';
import 'material-design-icons-iconfont/dist/material-design-icons.css';

import router from './router';
import store from './store';

Vue.config.productionTip = false;

new Vue({
    render: h => h(App),
    router,
    store
}).$mount('#app');
