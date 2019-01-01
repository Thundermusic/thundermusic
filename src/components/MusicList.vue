<template>
    <v-list class="music-list pa-0" two-line>
        <recycle-scroller :items="musics" :item-height="73">
            <template slot-scope="{ item: music, index }">
                <v-list-tile avatar ripple :value="selected.includes(music.id)" @click="$emit('select', music)" :class="{ 'first': index === 0 }">
                    <v-list-tile-avatar :tile="true" size="auto">
                        <img class="thumbnail" :src="music.thumbnail || require('../assets/thumbnail_default.png')" />
                    </v-list-tile-avatar>
                    <v-list-tile-content>
                        <v-list-tile-title class="music-title">{{ music.title }}</v-list-tile-title>
                        <v-list-tile-sub-title class="music-artist text--primary">{{ music.artist || music.channel }}</v-list-tile-sub-title>
                        <v-list-tile-sub-title>{{ music.duration || '?' }}</v-list-tile-sub-title>
                    </v-list-tile-content>
                    <v-progress-linear
                        v-if="progress && music.id in progress"
                        class="ma-0 progress"
                        color="primary"
                        background-color="transparent"
                        height="2"
                        :value="progress[music.id]"
                        :indeterminate="!progress[music.id]"
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

export default {
  name: "music-list",
  components: { Loading, RecycleScroller },
  props: ["selected", "musics", "progress"]
};
</script>

<style lang="scss">
.music-list {
  padding-top: 4px !important;

  .music-title {
    font-weight: 500;
  }

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
}
</style>
