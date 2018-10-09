<template>
    <div class="music-list">
        <Loading :isLoading="!initialized" />

        <v-list class="list" two-line>
            <!--<template v-for="(music, index) in content">-->
            <recycle-list :items="content" :item-height="72" page-mode v-if="initialized">
                <template slot-scope="{ item: music, index }">
                    <v-list-tile :key="index" :class="{ 'current': current.id === music.id }" avatar ripple @click="play(music)">
                        <v-list-tile-avatar :tile="true" size="auto">
                            <img class="thumbnail" :src="music.image || music.thumbnail" />
                        </v-list-tile-avatar>
                        <v-list-tile-content>
                            <v-list-tile-title class="music-title">{{ music.title }}</v-list-tile-title>
                            <v-list-tile-sub-title class="music-artist text--primary">{{ music.artist || music.channel }}</v-list-tile-sub-title>
                            <v-list-tile-sub-title>(3:21)</v-list-tile-sub-title>
                        </v-list-tile-content>
                        <v-list-tile-action v-if="sideButtons">
                            <v-icon color="grey lighten-1">add</v-icon>
                            <v-icon color="grey lighten-1">edit</v-icon>
                        </v-list-tile-action>
                    </v-list-tile>
                    <v-divider v-if="index + 1 < content.length" :key="`divider-${index}`"></v-divider>
                </template>
            </recycle-list>
            <!--</template>-->
        </v-list>
    </div>
</template>

<script>
    import Loading from "./Loading";
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

                if (this.download) {
                    this.$store.dispatch('download', music);
                }
            }
        },
        computed: {
            current() {
                return this.$store.state.current;
            },
            initialized() {
                return this.$store.state.initialized;
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

    .music-title, .music-artist {
        max-width: calc(100% - 20px);
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
        min-width: initial;
    }

    .current {
        border-left: solid 3px #F67504;
    }
</style>