import Vue from "vue";
import { download } from "../../platform/web";

export const state = {
  progress: {}
};

const types = {
  SET_PROGRESS: "SET_PROGRESS",
  REMOVE_PROGRESS: "REMOVE_PROGRESS"
};

export const mutations = {
  [types.SET_PROGRESS](state, { id, progress }) {
    Vue.set(state.progress, id, progress);
  },
  [types.REMOVE_PROGRESS](state, id) {
    Vue.delete(state.progress, id);
  }
};

export const actions = {
  download({ commit, dispatch }, music) {
    const url = download(music, progress => {
      progress *= 100;
      if (progress === 100) {
        commit(types.REMOVE_PROGRESS, music.id);
      } else {
        commit(types.SET_PROGRESS, { id: music.id, progress });
      }
    });

    dispatch(
      "musics/add",
      {
        ...music,
        url
      },
      { root: true }
    );

    // Indeterminate
    commit(types.SET_PROGRESS, { id: music.id, progress: NaN });

    url.then(url => {
      localStorage.setItem(
        "musics",
        JSON.stringify([
          ...JSON.parse(localStorage.getItem("musics") || "[]"),
          {
            ...music,
            url
          }
        ])
      );
    });
  },
  load() {}
};
