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
            <v-card id="current" class="elevation-6" height="46px" :class="{ 'menu': menu }">
                <div id="current-content">
                    <div>
                        <div id="infos" @click="menu = !menu">
                            <div id="title">{{ current.title }}</div>
                            <div id="author">{{ current.artist }}</div>
                        </div>
                        <div id="control">
                            <v-icon id="next" @click="previous">skip_previous</v-icon>
                            <v-icon id="pause" @click="pause">{{ paused ? 'play_arrow' : 'pause' }}</v-icon>
                            <v-icon id="next" @click="next">skip_next</v-icon>
                        </div>
                    </div>
                    <div id="slider-container">
                        {{ songPosition | duration }} <v-slider color="primary" v-model="songPosition" :max="songDuration"></v-slider> {{ songDuration | duration }}
                    </div>
                </div>

                <v-progress-linear id="song-progress" color="primary" background-color="transparent" height="2" :value="positionValue"></v-progress-linear>
            </v-card>
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
            ],
            menu: false
        }
    },
    computed: {
        paused() {
            return this.$store.state.paused;
        },
        current() {
            return this.$store.state.current;
        },
        positionValue() {
            return Math.round(this.$store.state.position / this.$store.state.duration * 100);
        },
        songPosition: {
            get() {
                return Math.round(this.$store.state.position / 1000);
            },
            set(value) {
                this.$store.dispatch('seek', value * 1000);
            }
        },
        songDuration() {
            return Math.round(this.$store.state.duration / 1000);
        }
    },
    methods: {
        previous() {
            this.$store.dispatch('previous');
        },
        pause() {
            this.$store.dispatch('pause');
        },
        next() {
            this.$store.dispatch('next');
        },
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
    filters: {
        duration(val) {
            const seconds = val % 60;
            const minutes = (val - seconds) / 60;

            return minutes + ':' + (seconds < 10 ? '0' : '') + seconds;
        }
    }
}
</script>

<style lang="scss">
    $player-height: 46px;

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

    #current {
        position: fixed;
        bottom: 56px;
        width: 100%;
    }

    #song-progress {
        opacity: 1;
        transition: opacity 125ms ease-in-out;

        position: absolute;
        width: 100%;
        bottom: 0;

        margin: 0;
        padding: 0;
    }

    #current.menu {
        height: 85px !important;

        #song-progress {
            opacity: 0;
        }
    }

    #current-content {
        height: calc(#{$player-height} - 2px);
    }

    #infos {
        width: calc(100% - 125px);

        margin-left: 13px;
        padding-top: 3px;
    }

    #control .v-icon {
        font-size: 34px;
        color: #222;

        padding-top: 5px;
    }

    #infos, #control {
        display: inline-block;
        float: left;

        height: calc(#{$player-height} - 2px);
    }

    #title, #author {
        text-overflow: ellipsis;
        white-space: nowrap;
        overflow: hidden;
    }

    #title {
        font-weight: 500;
        font-size: 14px;
    }

    #author {
        color: #555;
        font-size: 13px;
        margin-top: -2px;
    }

    #slider-container {
        width: 100%;

        padding-left: 20px;
        padding-right: 20px;

        float: left;
    }

    .v-input--slider {
        display: inline-block;

        width: calc(100% - 62px);

        margin-top: 0 !important;
        padding-left: 10px;
        padding-right: 10px;
    }
</style>
