<template>
    <div class="music-list">
        <div class="loading" v-if="!initialized">
            <v-progress-circular indeterminate color="primary" :size="50" :width="5"></v-progress-circular>
        </div>
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
                            <v-list-tile-sub-title class="text--primary">{{ music.artist || music.channel }}</v-list-tile-sub-title>
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
    export default {
        name: 'music-list',
        props: ['content', 'sideButtons', 'canPlay'],

        methods: {
            play(music) {
                if (this.canPlay) {
                    this.$store.dispatch('play', music);
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

    .loading {
        position: absolute;
        top: calc(50vh - 60px);
        left: calc(50% - 25px);
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
        min-width: initial;
    }

    .current {
        border-left: solid 3px #F67504;
    }
</style>