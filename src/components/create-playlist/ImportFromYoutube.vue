<template>
	<v-card>
		<v-card-text>
		<v-form v-model="valid">
			<v-text-field v-model="youtubeId" label="Youtube Playlist"></v-text-field>
			<v-btn
				:disabled="!valid"
				@click="createPlaylistFromYoutube"
			>
				submit
			</v-btn>
		</v-form>
		</v-card-text>
	</v-card>
</template>

<script>
import { mapGetters } from "vuex";
import { getPlaylistContent, getPlaylistInfos } from "../../platform/web";

function toId(name) {
  return name
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/ /g, "-")
    .replace(/\[|\]/g, "")
    .toLowerCase();
}

export default {
  header: "Import from Youtube",
  data() {
    return {
      valid: true,
      youtubeId: null
    };
  },
  computed: mapGetters("musics", ["hasMusic"]),
  methods: {
    async createPlaylistFromYoutube() {
      const [infos, musics] = await Promise.all([
        getPlaylistInfos(this.youtubeId),
        getPlaylistContent(this.youtubeId)
      ]);

      for (const music of musics) {
        if (!this.hasMusic(music))
          this.$store.dispatch("downloader/download", music);
      }

      const id = toId(infos.title);

      this.$store.dispatch("musics/addPlaylist", {
        id,
        title: infos.title,
        musics: musics.map(({ id }) => id),
        youtube: {
          playlistId: this.youtubeId
        }
      });

      this.$emit("close");

      this.$router.push({
        name: "playlist",
        params: { playlist: id }
      });
    }
  }
};
</script>

<style>
</style>
