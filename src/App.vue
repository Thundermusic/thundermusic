<template>
    <v-app>
        <v-toolbar fixed color="white" id="toolbar">
            <v-toolbar-side-icon><img id="icon" src="./assets/icon.png"/></v-toolbar-side-icon>
            <v-toolbar-title>{{ title() }}</v-toolbar-title>
            <v-spacer></v-spacer>
            <v-icon v-if="title() === 'Rechercher'" @click="$router.push('/downloads')">cloud_download</v-icon>
        </v-toolbar>
        <v-card height="100%" flat>
            <div id="content">
                <router-view/>
            </div>
            <player-bar/>
            <v-bottom-nav :value="true" fixed color="white">
                <v-btn v-for="(route, index) in nav" :key="index" color="primary" flat :to="route.to">
                    <span>{{ route.name }}</span>
                    <v-icon>{{ route.icon }}</v-icon>
                </v-btn>
            </v-bottom-nav>
        </v-card>
    </v-app>
</template>

<script>
import PlayerBar from "./components/PlayerBar.vue"
export default {
    name: 'App',
    mounted() {
        this.$store.dispatch('load');
    },
    data() {
        return {
            nav: [
                { name: 'Rechercher', icon: 'search', to: '/search' },
                { name: 'Musiques', icon: 'library_music', to: '/musics' },
                { name: 'Playlists', icon: 'queue_music', to: '/playlists' },
                { name: 'Paramètres', icon: 'settings', to: '/settings' }
            ]
        }
    },
    methods: {
        title() {
            const current = this.$router.currentRoute.path;
            if (current === '/downloads') {
                return 'Téléchargements';
            }

            for (let route of this.nav) {
                if (route.to === current) {
                    return route.name;
                }
            }

            return 'Thundermusic';
        }
    },
    components: {
        PlayerBar
    }
}
</script>

<style lang="scss">
    html {
        user-select: none;
    }

    #toolbar {
        z-index: 5;
    }

    #icon {
        width: 35px;
    }

    #content {
        margin-top: 56px;
        margin-bottom: 92px;
    }

</style>
