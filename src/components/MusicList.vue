<template>
    <div class="music-list">
        <v-list class="list" two-line>
            <recycle-list :items="content" :item-height="72" page-mode>
                <template slot-scope="{ item: music, index }">
                    <v-list-tile :key="index" :class="{ 'current': current.id === music.id, 'downloading': isDownloading(music), 'downloaded': isDownloaded(music) }" avatar ripple @click="play(music)">
                        <v-list-tile-avatar :tile="true" size="auto">
                            <img class="thumbnail" :src="music.image || music.thumbnail || require('../assets/thumbnail_default.png')" />
                        </v-list-tile-avatar>
                        <v-list-tile-content>
                            <v-list-tile-title class="music-title" :class="{ 'small': download }">{{ music.title }}</v-list-tile-title>
                            <v-list-tile-sub-title class="music-artist text--primary" :class="{ 'small': download }">{{ music.artist || music.channel }}</v-list-tile-sub-title>
                            <v-list-tile-sub-title>{{ isDownloading(music) ? 'Téléchargement...' : (isDownloaded(music) ? 'Déjà ajoutée' : music.duration || '?') }}</v-list-tile-sub-title>
                        </v-list-tile-content>
                        <v-list-tile-action v-if="sideButtons">
                            <v-icon color="grey lighten-1">add</v-icon>
                            <v-icon color="grey lighten-1">edit</v-icon>
                        </v-list-tile-action>
                    </v-list-tile>
                    <v-divider v-if="index + 1 < content.length" :key="`divider-${index}`"></v-divider>
                </template>
            </recycle-list>
        </v-list>
    </div>
</template>

<script>
    import Loading from './Loading';

    export default {
        name: 'music-list',
        components: { Loading },
        props: ['content', 'sideButtons', 'canPlay', 'download'],

        methods: {
            play(music) {
                if (this.canPlay) {
                    this.$store.dispatch('play', music);
                    return;
                }

                if (this.download && !this.isDownloaded(music) && !this.isDownloading(music)) {
                    this.$store.dispatch('download', music);
                }

                return false;
            },
            isDownloading(music) {
                if (!this.download) {
                    return false;
                }

                let downloads = this.$store.state.downloads;
                for (let download of downloads) {
                    if (download.id === music.id) {
                        return true;
                    }
                }

                return false;
            },
            isDownloaded(music) {
                if (!this.download) {
                    return false;
                }

                let musics = this.$store.state.musics;
                for (let m of musics) {
                    if (m.id === music.id) {
                        return true;
                    }
                }

                return false;
            }
        },
        computed: {
            current() {
                return this.$store.state.current;
            },
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

    .thumbnail {
        height: 50px !important;
        width: 90px !important;
        margin-right: 15px;
        object-fit: cover;
    }

    .current .thumbnail {
        margin-left: -3px;
    }

    .v-list__tile__avatar {
        min-width: 105px;
    }

    .current {
        border-left: solid 3px #F67504;
    }

    .downloading, .downloaded {
        color: #444;
    }

    .downloaded {
        font-style: italic;
    }
</style>