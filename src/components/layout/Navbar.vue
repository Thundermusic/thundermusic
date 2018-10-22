<template>
	<v-navigation-drawer class="navbar" app width="250" :mini-variant="mini" stateless value="true">
		<v-list>
			<v-subheader>Ma Musique</v-subheader>
			<v-list-tile
				v-for="(route, i) in routes"
				:key="i"
				:to="route"
			>
				<v-list-tile-action>
					<v-icon v-html="route.meta.icon"></v-icon>
				</v-list-tile-action>
				<v-list-tile-content>{{ route.meta.name_fr }}</v-list-tile-content>
			</v-list-tile>
			<v-subheader>
        Playlists
        <v-btn
          color="primary"
          icon
          flat
          dark
          absolute
          right
          small
          @click="create = true"
        >
          <v-icon>add</v-icon>
        </v-btn>
      </v-subheader>
			<v-list-tile
				v-for="playlist in playlists"
				:key="playlist.id"
				:to="{ name: 'playlist', params: { playlist: playlist.id }}"
        class="playlist"
			>
				<v-list-tile-content>{{ playlist.title }}</v-list-tile-content>
			</v-list-tile>
		</v-list>
    <create-playlist-dialog v-model="create"/>
	</v-navigation-drawer>
</template>

<script>
import { mapState } from "vuex";
import CreatePlaylistDialog from "../CreatePlaylistDialog";

export default {
  props: ["mini"],
  data() {
    return {
      create: false
    };
  },
  computed: {
    ...mapState("musics", ["playlists"]),
    routes() {
      return this.$router.options.routes.filter(
        ({ meta: { link, mobileOnly } = {} }) => link && !mobileOnly
      );
    },
    isSmall() {
      return this.$vuetify.breakpoint.width < 1264;
    }
  },
  watch: {
    isSmall(value) {
      if (value && !this.mini) this.$emit("input", true);
    }
  },
  components: {
    CreatePlaylistDialog
  }
};
</script>

<style lang="scss">
.navbar {
  padding-bottom: 48px; //PlayerBar heigth

  .playlist > .v-list__tile {
    height: 32px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
</style>
