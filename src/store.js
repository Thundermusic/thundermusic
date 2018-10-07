import Vue from 'vue';
import Vuex from 'vuex';

Vue.use(Vuex);

const cordova = window.cordova;

export default new Vuex.Store({
    state: {
        initialized: false,
        musics: [],
        current: { title: 'Aucune musique', artist: 'Personne' },
        paused: false,
        position: 0,
        duration: 0
    },
    mutations: {
        setInitialized(state)
        {
            state.initialized = true;
        },

        push(state, musics)
        {
            state.musics = musics;
        },

        setCurrent(state, current)
        {
            state.current = current;
        },

        setPaused(state, paused)
        {
            state.paused = paused;
        },

        setPosition(state, { pos, dur })
        {
            state.position = pos;
            state.duration = dur;
        }
    },
    actions: {
        load({ state, commit })
        {
            return new Promise((resolve, reject) => {
                document.addEventListener('deviceready', () =>
                    {
                        function listen()
                        {
                            cordova.exec(obj => {
                                switch (obj.type)
                                {
                                    case 'update':
                                        commit('push', obj.songs);
                                        break;
                                    case 'play':
                                        commit('setPaused', false);
                                        commit('setCurrent', obj.song);
                                        break;
                                    case 'pause':
                                        commit('setPaused', obj.paused);
                                        break;
                                    case 'download':
                                        // obj.song & obj.value (converted, progress, finished) (obj.param)
                                        break;
                                }

                                listen();
                            }, () => {}, 'Thundermusic', 'listen');
                        }
                        listen();

                        cordova.exec(() => {
                            setInterval(() => {
                                cordova.exec(obj => {
                                    const pos = obj.position;
                                    const dur = obj.duration;

                                    if (pos !== -1 && (Math.abs(state.current.durationValue - pos) > 0.6 || state.current.durationMax !== dur)) {
                                        commit('setPosition', { pos, dur });
                                    }
                                }, () => {}, 'Thundermusic', 'position');
                            }, 500);

                            commit('setInitialized');
                            resolve();
                        }, reject, 'Thundermusic', 'init');
                    },
                    false);
            });
        },

        play(_, music)
        {
            cordova.exec(null, null, 'Thundermusic', 'play', [music]);
        },

        pause()
        {
            cordova.exec(null, null, 'Thundermusic', 'pause');
        },

        previous()
        {
            cordova.exec(null, null, 'Thundermusic', 'previous');
        },

        next()
        {
            cordova.exec(null, null, 'Thundermusic', 'next');
        },

        seek(_, position)
        {
            cordova.exec(null, null, 'Thundermusic', 'seek', [position]);
        },

        update(_, song)
        {
            cordova.exec(null, null, 'Thundermusic', 'update', [song]);
        },

        remove(_, song)
        {
            cordova.exec(null, null, 'Thundermusic', 'remove', [song]);
        }
    }
});