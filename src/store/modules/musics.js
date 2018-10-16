import { addHandlers } from '../../platform/web'

const GLOBAL_PLAYLIST = Symbol('global')

export const state = {
	musics: [],
	currentPlaylist: GLOBAL_PLAYLIST,
	currentPlaylistIndex: 0
}

const types = {
	ADD_MUSIC: 'ADD_MUSIC',
	SET_MUSICS: 'SET_MUSICS',
	SET_PLAYLIST: 'SET_PLAYLIST',
	SET_PLAYLIST_INDEX: 'SET_PLAYLIST_INDEX'
}

export const mutations = {
	[types.ADD_MUSIC](state, music) {
		state.musics.push(music)
	},
	[types.SET_MUSICS](state, musics) {
		state.musics = musics
	},
	[types.SET_PLAYLIST](state, playlist) {
		state.currentPlaylist = playlist
	},
	[types.SET_PLAYLIST_INDEX](state, index) {
		state.currentPlaylistIndex = index
	}
}

export const getters = {
	playlists({ musics }) {
		const playlists = {
			[GLOBAL_PLAYLIST]: musics
		}

		for (const music of musics) {
			if (music.playlist)
				playlists[music.playlist] = music
		}

		return playlists
	}
}

export const actions = {
	add({ commit }, music) {
		commit(types.ADD_MUSIC, music)
	},
	play({ dispatch, getters, commit }, { music, playlist = GLOBAL_PLAYLIST }) {
		dispatch('player/setMusic', music, { root: true })
		commit(types.SET_PLAYLIST, playlist)
		commit(types.SET_PLAYLIST_INDEX, getters.playlists[playlist].indexOf(music))
	},
	next({ dispatch, state, getters, commit }) {
		let { currentPlaylistIndex: index, currentPlaylist: playlist } = state;

		index++
		index %= getters.playlists[playlist].length

		dispatch('player/setMusic', getters.playlists[playlist][index], { root: true })
		commit(types.SET_PLAYLIST_INDEX, index)
	},
	previous({ dispatch, state, getters, commit }) {
		let { currentPlaylistIndex: index, currentPlaylist: playlist } = state;

		index--
		index %= getters.playlists[playlist].length

		dispatch('player/setMusic', getters.playlists[playlist][index], { root: true })
		commit(types.SET_PLAYLIST_INDEX, index)
	},
	load({ commit, dispatch }) {
		const musics = localStorage.getItem('musics')
		if (musics)
			commit(types.SET_MUSICS, JSON.parse(musics))

		addHandlers({
			onNext() {
				dispatch('next')
			},
			onPrevious() {
				dispatch('previous')
			}
		})
	}
}