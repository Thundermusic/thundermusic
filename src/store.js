import Vue from 'vue';
import Vuex from 'vuex';

Vue.use(Vuex);

const cordova = window.cordova;

function extract(videoTitle, channel) {
    const video = videoTitle.trim().replace(/(\(|\[)(?!(ft|feat)).*(\)|\])/g, '')
        .replace(/\s+/g, ' ')
        .replace(/[^\[\(]((ft|feat)\..*)/g, (_, r) => ` (${r})`);

    let title = null;
    let artist = null;

    const splitChars = ['-', '–', ':'];
    for (const c of splitChars)
    {
        if (video.indexOf(c) > 0) {
            const split = video.split(c);

            title = split[1];
            artist = split[0];

            let result = '';

            for (const c of artist.split(' '))
            {
                result += c.charAt(0).toUpperCase() + c.substring(1) + ' ';
            }

            artist = result.substring(0, result.length - 1);

            break;
        }
    }

    if (title === null)
    {
        title = videoTitle;
    }

    if (artist === null)
    {
        artist = channel;
    }

    return { title, artist };
}

export default new Vuex.Store({
    state: {
        initialized: false,
        musics: [],
        downloads: [],
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

        pushDownloads(state, downloads)
        {
            state.downloads = downloads;
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
                                    case 'downloads':
                                        commit('pushDownloads', obj.downloads);
                                        break;
                                    case 'play':
                                        commit('setPaused', false);
                                        commit('setCurrent', obj.song);
                                        break;
                                    case 'pause':
                                        commit('setPaused', obj.paused);
                                        break;
                                    case 'position':
                                        if (obj.position !== -1 && (Math.abs(state.current.durationValue - obj.position) > 0.6 || state.current.durationMax !== obj.duration)) {
                                            commit('setPosition', { pos: obj.position, dur: obj.duration });
                                        }
                                        break;
                                }

                                listen();
                            }, () => {}, 'Thundermusic', 'listen');
                        }
                        listen();

                        cordova.exec(() => {
                            setInterval(() => cordova.exec(() => {}, () => {}, 'Thundermusic', 'position'), 500);
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
        },

        download(_, song)
        {
            let { title, artist } = extract(song.title, song.channel);

            cordova.exec(null, null, 'Thundermusic', 'download', [{
                id: song.id,
                title,
                artist,
                thumbnail: song.thumbnail
            }])
        }
    }
});