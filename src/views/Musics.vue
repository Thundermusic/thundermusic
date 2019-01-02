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
                    <!-- <v-btn v-if="!(music.id in progress)" icon @click.stop="deleteMusic(music)">
                        <v-icon color="grey lighten-1">delete</v-icon>
                    </v-btn> -->
                    <v-menu offset-y @click.native.stop>
                        <v-icon color="grey" slot="activator">menu</v-icon>
                        <v-list>
                            <v-list-tile>
                                <v-list-tile-title>Ajouter à...</v-list-tile-title>
                            </v-list-tile>
                            <v-list-tile @click="edit(music)">
                                <v-list-tile-title>Editer</v-list-tile-title>
                            </v-list-tile>
                            <v-list-tile @click="remove(music)">
                                <v-list-tile-title>Supprimer</v-list-tile-title>
                            </v-list-tile>
                        </v-list>
                    </v-menu>
                </v-list-tile-action>
            </template>
        </music-list>

        <EditDialog v-model="editDialog" :song="selected" :download="false" />
        <DeleteDialog v-model="deleteDialog" :song="selected" />

        <portal to="title" v-if="playlist">Playlist {{ playlists[playlist].title }}</portal>
    </div>
</template>

<script>
import MusicList from "../components/MusicList";
import EditDialog from "../components/EditDialog";
import DeleteDialog from "../components/DeleteDialog";
import { mapState, mapActions, mapGetters } from "vuex";

export default {
  props: ["playlist"],
  name: "musics",
  components: { MusicList, EditDialog, DeleteDialog },
  computed: {
    ...mapState("musics", ["musics", "playlists"]),
    ...mapGetters("musics", ["getMusicsByPlaylist"]),
    ...mapState("player", ["current"]),
    ...mapState("downloader", ["progress"])
  },
  //methods: mapActions("musics", ["play", "deleteMusic"])
  data() {
    return {
      editDialog: false,
      deleteDialog: false,
      selected: {}
    };
  },
  methods: {
    edit(music) {
      this.selected = music;
      this.editDialog = true;
    },
    remove(music) {
      this.selected = music;
      this.deleteDialog = true;
    },
    ...mapActions("musics", ["play"])
  }
};
</script>

<style>
</style>
