import Vue from 'vue';
import Vuex from 'vuex';

Vue.use(Vuex);

export default new Vuex.Store({
    state: {
    }
});

/*
export default new Vuex.Store({
    state: {
        initialized: false,
        musics: [],
        current: null,
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
                                        console.log('ah ! on change de song');
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
                            }, _ => {}, 'Thundermusic', 'listen');
                        }
                        listen();

                        cordova.exec(_ => {
                            setInterval(() => {
                                cordova.exec(obj => {
                                    const pos = obj.position;
                                    const dur = obj.duration;

                                    if (pos !== -1 && (Math.abs(state.current.durationValue - pos) > 0.6 || state.current.durationMax !== dur)) {
                                        commit('setPosition', { pos, dur });
                                    }
                                }, _ => {}, 'Thundermusic', 'position');
                            }, 500);

                            commit('setInitialized');
                            resolve();
                        }, reject, 'Thundermusic', 'init');
                    },
                    false);
            });
        },

        play({}, music)
        {
            cordova.exec(null, null, 'Thundermusic', 'play', [music]);
        },

        pause()
        {
            cordova.exec(_ => {}, _ => {}, 'Thundermusic', 'pause');
        },

        previous()
        {
            cordova.exec(_ => {}, _ => {}, 'Thundermusic', 'previous');
        },

        next()
        {
            cordova.exec(_ => {}, _ => {}, 'Thundermusic', 'next');
        },

        seek({}, position)
        {
            cordova.exec(_ => {}, _ => {}, 'Thundermusic', 'seek', [position]);
        },

        update({}, song)
        {
            cordova.exec(_ => {}, _ => {}, 'Thundermusic', 'update', [song]);
        },

        remove({}, song)
        {
            cordova.exec(_ => {}, _ => {}, 'Thundermusic', 'remove', [song]);
        }
    }
});
 */