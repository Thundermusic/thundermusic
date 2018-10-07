import Vue from 'vue';
import Router from 'vue-router';

import Search from '@/views/Search.vue';
import Musics from '@/views/Musics.vue';
import Playlists from '@/views/Playlists.vue';
import Settings from '@/views/Settings.vue';
import Downloads from '@/views/Downloads.vue';

Vue.use(Router);

export default new Router({
    routes: [
        { path: '/', redirect: '/musics' },
        { path: '/search', component: Search, name: 'search' },
        { path: '/musics', component: Musics, name: 'musics' },
        { path: '/playlists', component: Playlists, name: 'playlists' },
        { path: '/settings', component: Settings, name: 'settings' },
        { path: '/downloads', component: Downloads, name: 'downloads' }
    ]
});