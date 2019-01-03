import Vue from "vue";
import { addHandlers, cleanupMusic, storage } from "../../platform/web";

export const state = {
  musics: [],
  playlists: {},
  currentPlaylist: undefined,
  currentPlaylistIndex: 0,
  mode: "linear" // linear | loop | random
};

const types = {
  ADD_MUSIC: "ADD_MUSIC",
  DELETE_MUSIC: "DELETE_MUSIC",
  EDIT_MUSIC: "EDIT_MUSIC",
  SET_MUSICS: "SET_MUSICS",
  SET_PLAYLIST: "SET_PLAYLIST",
  SET_PLAYLIST_INDEX: "SET_PLAYLIST_INDEX",
  ADD_PLAYLIST: "ADD_PLAYLIST",
  SET_PLAYLISTS: "SET_PLAYLISTS",
  SET_MODE: "SET_MODE"
};

export const mutations = {
  [types.ADD_MUSIC](state, music) {
    state.musics.push(music);
  },
  [types.DELETE_MUSIC](state, id) {
    const index = state.musics.findIndex(music => music.id === id);
    if (index != -1) state.musics.splice(index, 1);
  },
  [types.EDIT_MUSIC](state, music) {
    const index = state.musics.findIndex(m => music.id === m.id);
    state.musics[index] = music;
  },
  [types.SET_MUSICS](state, musics) {
    state.musics = musics;
  },
  [types.SET_PLAYLIST](state, playlist) {
    state.currentPlaylist = playlist;
  },
  [types.SET_PLAYLIST_INDEX](state, index) {
    state.currentPlaylistIndex = index;
  },
  [types.ADD_PLAYLIST](state, playlist) {
    Vue.set(state.playlists, playlist.id, playlist);
  },
  [types.SET_PLAYLISTS](state, playlists) {
    state.playlists = playlists;
  },
  [types.SET_MODE](state, mode) {
    state.mode = mode;
  }
};

export const getters = {
  getMusicsByPlaylist: ({ musics, playlists }) => playlist => {
    if (playlist === undefined) {
      return musics;
    } else {
      const playlistInfos = playlists[playlist];
      return musics.filter(({ id }) => playlistInfos.musics.includes(id));
    }
  },
  hasMusic: ({ musics }) => ({ id }) => {
    return !!musics.find(music => music.id === id);
  }
};

export const actions = {
  async add({ commit }, music) {
    commit(types.ADD_MUSIC, music);

    await storage.addMusic({
      ...music,
      url: await music.url
    });
  },
  async deleteMusic({ commit, state, getters, dispatch }, music) {
    const currentMusic = getters.getMusicsByPlaylist(state.currentPlaylist)[
      state.currentPlaylistIndex
    ];

    if (currentMusic.id === music.id)
      dispatch("player/setMusic", null, { root: true });

    await cleanupMusic(music);
    commit(types.DELETE_MUSIC, music.id);
    return storage.deleteMusic(music.id);
  },
  async edit({ commit }, music) {
    commit(types.EDIT_MUSIC, music);
    await storage.updateMusic({
      ...music,
      url: await music.url
    });
  },
  play({ dispatch, getters, commit }, { music, playlist }) {
    dispatch("player/setMusic", music, { root: true });
    commit(types.SET_PLAYLIST, playlist);
    commit(
      types.SET_PLAYLIST_INDEX,
      getters.getMusicsByPlaylist(playlist).indexOf(music)
    );
  },
  next({ dispatch, state, getters, commit }) {
    let { currentPlaylistIndex: index, currentPlaylist: playlist } = state;

    const musics = getters.getMusicsByPlaylist(playlist);
    index++;
    if (index >= musics.length) index = 0;

    dispatch("player/setMusic", musics[index], {
      root: true
    });
    commit(types.SET_PLAYLIST_INDEX, index);
  },
  previous({ dispatch, state, getters, commit }) {
    let { currentPlaylistIndex: index, currentPlaylist: playlist } = state;

    const musics = getters.getMusicsByPlaylist(playlist);
    index--;
    if (index < 0) index = musics.length - 1;

    dispatch("player/setMusic", musics[index], {
      root: true
    });
    commit(types.SET_PLAYLIST_INDEX, index);
  },
  end({ state, dispatch, getters, commit }) {
    const { currentPlaylistIndex, currentPlaylist: playlist, mode } = state;

    if (mode == "linear") return dispatch("next");

    const musics = getters.getMusicsByPlaylist(playlist);
    const index =
      mode === "random"
        ? Math.floor(Math.random() * musics.length)
        : currentPlaylistIndex; // loop mode

    dispatch("player/setMusic", musics[index], {
      root: true
    });

    commit(types.SET_PLAYLIST_INDEX, index);
  },
  addPlaylist({ commit }, playlist) {
    commit(types.ADD_PLAYLIST, playlist);
    return storage.addPlaylist(playlist);
  },
  setMode({ commit }, mode) {
    commit(types.SET_MODE, mode);
  },
  async load({ commit, dispatch }) {
    commit(types.SET_MUSICS, await storage.getMusics());
    commit(types.SET_PLAYLISTS, await storage.getPlaylists());

    addHandlers({
      onNext() {
        dispatch("next");
      },
      onPrevious() {
        dispatch("previous");
      },
      onEnd() {
        dispatch("end");
      }
    });
  }
};
