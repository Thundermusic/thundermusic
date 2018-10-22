<template>
    <div id="playlists">
        <portal to="toolbar">
            <v-btn
              color="primary"
              dark
              absolute
              bottom
              right
              fab
              @click="create = true"
            >
              <v-icon>add</v-icon>
            </v-btn>
        </portal>
        <v-list>
            <template v-for="(playlist, i) in Object.values(playlists)">
              <v-divider v-if="i" :key="`divider-${i}`"/>
              <v-list-tile :key="playlist.id" :to="{ name: 'playlist', params: { playlist: playlist.id }}">
                  <v-list-tile-title>
                      {{ playlist.title }}
                  </v-list-tile-title>
              </v-list-tile>
            </template>
        </v-list>
        <create-playlist-dialog v-model="create" fullscreen />
    </div>
</template>

<script>
import CreatePlaylistDialog from "../components/CreatePlaylistDialog.vue";
import { mapState } from "vuex";

export default {
  data() {
    return {
      create: false
    };
  },

  components: {
    CreatePlaylistDialog
  },

  computed: mapState("musics", ["playlists"])
};
</script>

<style lang="scss">
</style>
