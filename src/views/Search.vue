<template>
    <div id="search">
        <loading :isLoading="searching" />

        <v-form @submit.prevent="search" class="input" :style="{ 'padding-left': `${$vuetify.application.left}px`}">
          <v-text-field
            hide-details
            :readonly="searching"
            box
            single-line
            :value="query"
            @input="q => $router.replace({ query: { q }})"
            autofocus
            autocapitalize="off"
            autocomplete="off"
            spellcheck="false"
            autocorrect="off"
            placeholder="Rechercher..."
          >
            <template slot="append">
              <v-menu left>
                <img slot="activator" class="provider-logo" :src="providers[currentProvider].icon">
                <v-list dense class="search-providers">
                  <v-list-tile v-for="(provider, i) in providers" :key="provider.name">
                    <v-list-tile-action>
                      <v-btn flat icon @click="currentProvider = i">
                        <img class="provider-logo" :src="provider.icon">
                      </v-btn>
                    </v-list-tile-action>
                  </v-list-tile>
                </v-list>
              </v-menu>
            </template>
          </v-text-field>
        </v-form>

        <!-- <music-list :musics="results" :selected="selected" @select="select" /> -->
        <music-list :musics="results" @select="play" :progress="progress" :hasMusic="hasMusic">
            <template slot-scope="{ music }">
                <v-list-tile-action>
                    <v-btn icon @click.stop="download(music)">
                        <v-icon color="grey lighten-1">cloud_download</v-icon>
                    </v-btn>

                    <v-btn icon @click.stop="editAndDownload(music)">
                        <v-icon color="grey lighten-1">edit</v-icon>
                    </v-btn>
                </v-list-tile-action>
            </template>
        </music-list>

        <!-- <v-layout column class="controls" v-show="selected.length">
            <v-btn fab dark color="green" @click="downloadAll">
                <v-icon>cloud_download</v-icon>
            </v-btn>
            <v-btn fab dark color="red" @click="selected = []">
                <v-icon>select_all</v-icon>
            </v-btn>
        </v-layout> -->

        <EditDialog v-model="editDialog" :song="selected" :download="true" @afterEdit="download(selected)" />
    </div>
</template>

<script>
import { mapGetters, mapState } from "vuex";
import EditDialog from "../components/EditDialog";
import MusicList from "../components/MusicList";
import Loading from "../components/Loading";

import { providers } from "../platform/web";

export default {
  props: ["query"],
  name: "search",
  components: { EditDialog, Loading, MusicList },

  data() {
    return {
      providers,
      currentProvider: Object.keys(providers)[0],
      searching: false,
      results: [],
      selected: {},
      metas: {},
      editDialog: false /*,
      selected: []*/
    };
  },
  computed: {
    ...mapGetters("musics", ["hasMusic"]),
    ...mapState("downloader", ["progress"])
  },
  watch: {
    currentProvider() {
      this.results = [];
      //this.selected = [];
      this.searching = false;
    }
  },
  methods: {
    search() {
      this.results = [];
      this.searching = true;

      this.providers[this.currentProvider].search(this.query).then(
        results => {
          this.results = results;
          this.searching = false;
        },
        e => {
          this.searching = false;
          throw e;
        }
      );
    },
    play(music) {
      const meta = this.providers[this.currentProvider].getMeta(music);
      this.metas[music.id] = meta;

      this.$store.dispatch("player/setMusic", {
        ...music,
        url: meta.then(({ url }) => url)
      });
    },
    download(music) {
      if (music in this.progress || this.hasMusic(music)) {
        return;
      }

      if (!this.hasMusic(music)) {
        this.$store.dispatch("downloader/download", {
          music: this.$extract(music),
          provider: this.currentProvider,
          meta: this.metas[music.id]
        });
      }
    },
    editAndDownload(music) {
      if (music in this.progress || this.hasMusic(music)) {
        return;
      }

      this.selected = this.$extract(music);
      this.editDialog = true;
    }
    /*select(music) {
      const index = this.selected.indexOf(music.id);
      if (index >= 0) this.selected.splice(index, 1);
      else this.selected.push(music.id);
    },
    downloadAll() {
      this.selected
        .map(id => this.results.find(music => music.id === id))
        .forEach(music => {
          if (!this.hasMusic(music))
            this.$store.dispatch("downloader/download", {
              music,
              provider: this.currentProvider
            });
        });
      this.$router.push({ name: "musics" });
    }*/
  }
};
</script>

<style lang="scss">
#search {
  padding-top: 56px;

  .input {
    position: fixed;
    margin-top: -56px;
    right: 0;
    left: 0;
    z-index: 1;
    background: white;
  }

  .controls {
    position: fixed;
    right: 12px;
    top: 50%;
    transform: translateY(-50%);

    .v-btn__content {
      height: unset;
    }
  }
}

.provider-logo {
  height: 34px;
  width: 34px;
  margin-top: -(10px / 4);
}

.search-providers .v-list__tile__action {
  min-width: inherit;
}

.v-input__append-inner {
  margin-top: 14px !important;
}

.theme--dark #search {
  .input {
    background: black;
  }
}
</style>
