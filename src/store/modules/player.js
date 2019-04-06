import { addHandlers, seek, setVolume, play, pause, setMusic } from "platform";

const DEFAULT_SONG = {
  id: "default",
  title: "Aucune musique",
  artist: "Personne",
  url: ""
};

const types = {
  SET_POSITION: "SET_POSITION",
  SET_DURATION: "SET_DURATION",
  SET_VOLUME: "SET_VOLUME",
  SET_MUSIC: "SET_MUSIC",
  PLAY: "PLAY",
  PAUSE: "PAUSE"
};

export const state = {
  current: DEFAULT_SONG,
  paused: true,
  position: 0,
  duration: 0,
  volume: localStorage.getItem("volume") || 100
};

export const mutations = {
  [types.SET_POSITION](state, position) {
    state.position = position;
  },
  [types.SET_DURATION](state, duration) {
    state.duration = duration;
  },
  [types.SET_VOLUME](state, volume) {
    state.volume = volume;
    localStorage.setItem("volume", volume);
  },
  [types.SET_MUSIC](state, music) {
    state.current = music;
  },
  [types.PLAY](state) {
    state.paused = false;
  },
  [types.PAUSE](state) {
    state.paused = true;
  }
};

export const actions = {
  play({ commit }) {
    commit(types.PLAY);
    play().catch(() => {}); // Silence errors
  },
  pause({ commit }) {
    commit(types.PAUSE);
    pause();
  },
  seek({ state }, time) {
    if (isFinite(time) && time != state.position) {
      // onPositionChange will make the commit
      seek(time);
    }
  },
  setVolume({ commit }, volume) {
    setVolume(volume);
    commit(types.SET_VOLUME, volume);
  },
  async setMusic({ commit, dispatch }, music) {
    if (music) {
      commit(types.SET_MUSIC, music);
      await setMusic(music);
      dispatch("play");
    } else {
      dispatch("pause");
      commit(types.SET_MUSIC, DEFAULT_SONG);
      await setMusic(DEFAULT_SONG, true);
    }
  },
  load({ dispatch, commit, state }) {
    addHandlers({
      onPositionChange(position, duration) {
        position = Math.round(position);
        duration = Math.round(duration);
        if (state.position !== position) commit(types.SET_POSITION, position);
        if (state.duration !== duration) commit(types.SET_DURATION, duration);
      },
      onPlay() {
        dispatch("play");
      },
      onPause() {
        dispatch("pause");
      }
    });
  }
};
