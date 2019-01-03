import Vue from "vue";
import { storage } from "platform";

/* Default values */
export const state = {
  showDisk: false,
  dark: false
};

const types = {
  SET: "SET"
};

export const mutations = {
  [types.SET](state, { key, value }) {
    Vue.set(state, key, value);
  }
};

export const actions = {
  set({ commit }, { key, value }) {
    commit(types.SET, { key, value });
    return storage.setSetting(key, value);
  },
  async load({ commit }) {
    for (const [key, value] of await storage.getSettings())
      commit(types.SET, { key, value });
  }
};
