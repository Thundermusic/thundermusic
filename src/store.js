import Vue from 'vue';
import Vuex from 'vuex';
import { downloadFromYoutube } from './youtube'

Vue.use(Vuex);

/*function extract(videoTitle, channel) {
    let video = videoTitle.trim();

    while (video.indexOf('[') !== -1 && video.indexOf(']') !== -1) {
        let a = video.indexOf('['), b = video.indexOf(']');

        if (video.length > a + 2 && video.substr(a + 1, a + 3) === 'ft' ||
            video.length > a + 4 && video.substr(a + 1, a + 5) === 'feat')
            continue;

        video = (video.substr(0, a) + video.substr(b + 1, video.length)).trim();
    }

    while (video.indexOf('(') !== -1 && video.indexOf(')') !== -1) {
        let a = video.indexOf('('), b = video.indexOf(')');

        if (video.length > a + 2 && video.substr(a + 1, a + 3) === 'ft' ||
            video.length > a + 4 && video.substr(a + 1, a + 5) === 'feat')
            continue;

        video = (video.substr(0, a) + video.substr(b + 1, video.length)).trim();
    }

    video = video.replace(/\s+/g, ' ')
        .replace(/[^[(]((ft|feat)\..*)/g, (_, r) => ` (${r})`).trim();

    if (video.startsWith('-'))
    {
        video = video.substr(1).trim();
    }

    if (video.endsWith('-'))
    {
        video = video.substr(video.length - 1).trim();
    }

    // 'The Chainsmokers - Side Effects (Lyric Video) ft. Emily Warren '
    // --> 'The Chainsmokers - Side Effects (ft. Emily Warren)'

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

    return { title: title.trim(), artist: artist.trim() };
}*/

const DEFAULT_SONG = { id: 'default', title: 'Aucune musique', artist: 'Personne' };

const AUDIO = new Audio();

export default new Vuex.Store({
    state: {
        initialized: false,
        musics: [],
        downloads: [],
        current: DEFAULT_SONG,
        paused: false,
        position: 0,
        duration: 0
    },
    mutations: {
        setInitialized(state)
        {
            state.initialized = true;
        },

        push(state, music)
        {
            state.musics.push(music);
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
        load({ commit })
        {
            AUDIO.addEventListener('timeupdate', () => {
                commit('setPosition', { pos: AUDIO.currentTime, dur: AUDIO.duration })
            })
        },

        async play({ commit }, music)
        {
            console.log('Play', music)
            AUDIO.src = await music.url;
            AUDIO.play();
            commit('setCurrent', music)
        },

        pause({ state, commit })
        {
            console.log('Pause')
            state.paused ? AUDIO.play() : AUDIO.pause();
            commit('setPaused', !state.paused)
        },

        previous()
        {
            console.log('Previous')
        },

        next()
        {
            console.log('Next')
        },

        seek(_, position)
        {
            console.log('Seek', position)
        },

        update(_, song)
        {
            console.log('Update', song)
        },

        remove(_, song)
        {
            console.log('Remove', song)
        },

        async download({ commit }, song)
        {
            console.log('Download', song, song.youtube)
            if (song.youtube) {
                console.log('Youtube')
                commit('push', {
                    ...song,
                    url: downloadFromYoutube(song)
                })
            }

        }
    }
});