import Vue from 'vue'
import Vuex from 'vuex'
import modules from './modules'

Vue.use(Vuex)

const store = new Vuex.Store({
    modules
})

for (const module in modules) {
	store.dispatch(`${module}/load`)
}

export default store;