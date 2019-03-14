<template>
    <div class="edit">
        <v-dialog v-model="opened" max-width="250">
            <v-card>
                <v-card-title class="headline">Editer la chanson</v-card-title>

                <v-card-text>
                    <v-text-field solo autocomplete="off" spellcheck="false" autocorrect="off" v-model="song.artist" placeholder="Artiste" />
                    <v-text-field solo autocomplete="off" spellcheck="false" autocorrect="off" v-model="song.title" placeholder="Titre" />
                </v-card-text>

                <v-card-actions>
                    <v-spacer></v-spacer>

                    <v-btn color="primary" flat="flat" @click="opened = false">Annuler</v-btn>
                    <v-btn color="primary" flat="flat" @click="apply()">{{ download ? 'Télécharger' : 'OK' }}</v-btn>
                </v-card-actions>
            </v-card>
        </v-dialog>
    </div>
</template>

<script>
export default {
  name: "edit-dialog",
  props: ["value", "song", "download"],

  computed: {
    opened: {
      get() {
        return this.value;
      },
      set(val) {
        this.$emit("input", val);
      }
    }
  },
  methods: {
    apply() {
      this.song.title = this.song.title.trim();
      this.song.artist = this.song.artist.trim();

      if (this.song.title === "" || this.song.artist === "") {
        return;
      }

      if (this.download) {
        this.$emit("afterEdit");
      } else {
        this.$store.dispatch("musics/edit", this.song);
      }

      this.opened = false;
    }
  }
};
</script>

<style scoped>
.v-card__text {
  padding-top: 9px;
  padding-bottom: 0;
}
</style>
