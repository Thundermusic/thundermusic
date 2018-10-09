<template>
    <div id="downloads">
        <v-list class="list" two-line>
            <template v-for="(music, index) in downloads">
                <v-list-tile :key="index" avatar ripple>
                    <v-list-tile-avatar :tile="true" size="auto">
                        <img class="thumbnail" :src="music.image || music.thumbnail" />
                    </v-list-tile-avatar>
                    <v-list-tile-content>
                        <v-list-tile-title class="music-title">{{ music.title }}</v-list-tile-title>
                        <v-list-tile-sub-title class="music-artist text--primary">{{ music.artist || music.channel }}</v-list-tile-sub-title>
                        <v-list-tile-sub-title class="song-progress">
                            {{ music.progress === -2 ? 'En attente...' : (music.progress === -1 ? 'Conversion...' : '') }}
                            <v-progress-linear v-if="music.progress >= 0" color="primary" :value="music.progress" :height="6"></v-progress-linear>
                        </v-list-tile-sub-title>
                    </v-list-tile-content>
                </v-list-tile>
                <v-divider v-if="index + 1 < downloads.length" :key="`divider-${index}`"></v-divider>
            </template>
        </v-list>
    </div>
</template>

<script>
    export default {
        name: 'downloads',

        computed: {
            downloads() {
                return this.$store.state.downloads;
            }
        }
    }
</script>

<style>
    .list {
        padding-top: 4px;
    }

    .music-title {
        font-weight: 500;
    }

    .song-progress {
        max-width: calc(100% - 30px);
        height: 18px;
    }

    .thumbnail {
        height: 50px !important;
        width: 90px !important;
        margin-right: 15px;
        object-fit: cover;
    }

    .v-list__tile__avatar {
        min-width: 105px;
    }

    .v-progress-linear {
        margin: 4px 0 0 0 !important;
    }
</style>