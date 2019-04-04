<template>
    <div class="delete-dialog">
        <v-dialog v-model="opened" max-width="250">
            <v-card>
                <v-card-title class="headline">Supprimer la chanson ?</v-card-title>

                <v-card-text>
                    Voulez-vous vraiment supprimer la chanson :
                    <div class="delete-title">{{ song.title }}</div>
                    <div class="delete-artist">{{ song.artist }}</div>
                </v-card-text>

                <v-card-actions>
                    <v-spacer></v-spacer>

                    <v-btn color="primary" flat="flat" @click="opened = false">Non</v-btn>
                    <v-btn color="primary" flat="flat" @click="yes()">Oui</v-btn>
                </v-card-actions>
            </v-card>
        </v-dialog>
    </div>
</template>

<script>
export default {
  name: "delete-dialog",
  props: ["value", "song"],

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
    yes() {
      this.$store.dispatch("musics/deleteMusic", this.song);
      this.opened = false;
    }
  }
};
</script>

<style scoped>
.delete-title {
  margin-top: 10px;
  font-weight: bold;
}

.delete-title,
.delete-artist {
  font-size: 18px;
}

.v-card__text {
  padding-top: 10px;
}
</style>
