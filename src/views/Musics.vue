<template>
    <div id="musics">
        <music-list
            :musics="playlist ? getMusicsByPlaylist(playlist) : musics"
            :selected="current && [current.id]"
            @select="music => play({ music, playlist })"
            :progress="progress"
        >
            <template slot-scope="{ music }">
                <v-list-tile-action>
                    <v-icon color="grey lighten-1">add</v-icon>
                    <v-icon color="grey lighten-1">edit</v-icon>
                </v-list-tile-action>
            </template>
        </music-list>
        <portal to="title" v-if="playlist">Playlist {{ playlists[playlist].title }}</portal>
    </div>
</template>

<script>
import MusicList from "../components/MusicList";
import { mapState, mapActions, mapGetters } from "vuex";

export default {
  props: ["playlist"],
  name: "musics",
  components: { MusicList },
  computed: {
    ...mapState("musics", ["musics", "playlists"]),
    ...mapGetters("musics", ["getMusicsByPlaylist"]),
    ...mapState("player", ["current"]),
    ...mapState("downloader", ["progress"])
  },
  methods: mapActions("musics", ["play"])
};
</script>

<style>
</style>
