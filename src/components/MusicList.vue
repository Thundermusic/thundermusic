<template>
    <v-list class="music-list pa-0" two-line>
        <recycle-scroller :items="musics" :item-height="73">
            <template slot-scope="{ item: music, index }">
                <!-- <v-list-tile avatar ripple :value="selected.includes(music.id)" @click="$emit('select', music)" :class="{ 'first': index === 0 }">  -->
                <v-list-tile avatar ripple @click="$emit('select', music)" :class="{ 'current': current.id === music.id, 'first': index === 0, 'downloading': isDownloading(music), 'downloaded': hasMusic && hasMusic(music) }">
                    <v-list-tile-avatar :tile="true" size="auto">
                        <img class="thumbnail" :src="music.thumbnail || require('../assets/thumbnail_default.png')" />
                    </v-list-tile-avatar>
                    <v-list-tile-content>
                        <v-list-tile-title class="music-title">{{ music.title }}</v-list-tile-title>
                        <v-list-tile-sub-title class="music-artist text--primary">{{ music.artist }}</v-list-tile-sub-title>
                        <v-list-tile-sub-title>{{ isDownloading(music) ? getStatus(music) : (hasMusic && hasMusic(music) ? 'Déjà ajoutée' : music.duration || '?') }}</v-list-tile-sub-title>
                    </v-list-tile-content>
                    <v-progress-linear
                        v-if="isDownloading(music)"
                        class="ma-0 progress"
                        color="primary"
                        background-color="transparent"
                        height="2"
                        :value="progress[music.id]"
                        :indeterminate="isIndeterminate(music)"
                    ></v-progress-linear>
                    <slot :music="music"></slot>
                </v-list-tile>
                <v-divider v-if="index + 1 < musics.length"></v-divider>
            </template>
        </recycle-scroller>
    </v-list>
</template>

<script>
import Loading from "./Loading";
import RecycleScroller from "./RecycleScroller";

import { mapState } from "vuex";

export default {
  name: "music-list",
  components: { Loading, RecycleScroller },
  props: [/*"selected", */ "musics", "progress", "hasMusic"],
  computed: mapState("player", [
    "current",
    "paused",
    "volume",
    "position",
    "duration"
  ]),
  methods: {
    isDownloading(music) {
      return music.id in this.progress;
    },
    getStatus(music) {
      const status = this.progress[music.id];

      if (status === -2) {
        return "En attente";
      } else if (status === -1) {
        return "Conversion...";
      } else {
        return "Téléchargement...";
      }
    },
    isIndeterminate(music) {
      const status = this.progress[music.id];
      return isNaN(status) || status < 0;
    }
  }
};
</script>

<style lang="scss">
.music-list {
  padding-top: 4px !important;

  .music-title {
    font-weight: 500;
  }

  /* Youtube add black border to it's image, so we need to keep yt aspec ratio */
  .thumbnail {
    height: 50px !important;
    width: 90px !important;
    object-fit: cover;
  }

  .v-list__tile__avatar {
    min-width: 105px;
  }

  .progress {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
  }

  .downloaded {
    font-style: italic;
  }

  .current .v-list__tile {
    margin-left: -3px;
  }

  .current {
    border-left: solid 3px #f67504;
  }
}
</style>
