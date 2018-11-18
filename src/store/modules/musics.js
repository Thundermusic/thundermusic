import Vue from "vue";
import { addHandlers } from "../../platform/web";

export const state = {
  musics: [],
  playlists: {},
  currentPlaylist: undefined,
  currentPlaylistIndex: 0
};

const types = {
  ADD_MUSIC: "ADD_MUSIC",
  SET_MUSICS: "SET_MUSICS",
  SET_PLAYLIST: "SET_PLAYLIST",
  SET_PLAYLIST_INDEX: "SET_PLAYLIST_INDEX",
  ADD_PLAYLIST: "ADD_PLAYLIST",
  SET_PLAYLISTS: "SET_PLAYLISTS"
};

export const mutations = {
  [types.ADD_MUSIC](state, music) {
    state.musics.push(music);
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
  add({ commit }, music) {
    commit(types.ADD_MUSIC, music);
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
  addPlaylist({ commit, state }, playlist) {
    commit(types.ADD_PLAYLIST, playlist);
    localStorage.setItem("playlists", JSON.stringify(state.playlists));
  },
  load({ commit, dispatch }) {
    const musics = localStorage.getItem("musics");
    if (musics) commit(types.SET_MUSICS, JSON.parse(musics));
    const playlists = localStorage.getItem("playlists");
    if (playlists) commit(types.SET_PLAYLISTS, JSON.parse(playlists));

    addHandlers({
      onNext() {
        dispatch("next");
      },
      onPrevious() {
        dispatch("previous");
      }
    });
  }
};
