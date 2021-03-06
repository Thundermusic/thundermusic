<template>
	<v-card class="player-bar elevation-6" :class="{ opened }">
		<v-layout row wrap @click="$vuetify.breakpoint.xsOnly ? opened = !opened : null">
			<v-flex class="pl-2 music flex-0">
				<div class="disk" :class="{ paused }" v-if="showDisk">
          			<img v-if="current.thumbnail" :src="current.thumbnail">
					<span class="inner"/>
				</div>
				<div class="infos pt-2 pb-1">
					<span class="title">{{ current.title }}</span>
					<span class="artist">{{ current.artist }}</span>
				</div>
			</v-flex>
			<v-flex class="slider-row">
				<v-layout row class="slider-layout mx-4">
					<v-flex class="flex-0 display-flex">
						<span>{{ position | duration }}</span>
					</v-flex>
					<v-flex class="display-flex">
						<v-slider hide-details class="px-2 ma-0 mx-3 slider" color="primary" :value="position" @input="seek" @click.native.stop :max="duration"></v-slider>
					</v-flex>
					<v-flex class="flex-0 display-flex">
						<span>{{ duration | duration }}</span>
					</v-flex>
				</v-layout>
			</v-flex>
			<v-flex class="text-xs-center flex-0 display-flex controls">
				<v-btn flat icon @click.stop="previous">
					<v-icon>skip_previous</v-icon>
				</v-btn>
				<v-btn flat icon @click.stop="paused ? play() : pause()">
					<v-icon>{{ paused ? 'play_arrow' : 'pause' }}</v-icon>
				</v-btn>
				<v-btn flat icon @click.stop="next">
					<v-icon>skip_next</v-icon>
				</v-btn>
			</v-flex>
			<v-flex xs12 v-if="$vuetify.breakpoint.xsOnly">
				<v-progress-linear
					class="song-progress ma-0"
					color="primary"
					background-color="transparent"
					height="2"
					:value="(position / duration) * 100"
				></v-progress-linear>
			</v-flex>
			<v-flex v-else class="display-flex flex-0 volume">
				<v-slider prepend-icon="volume_up" hide-details class="px-2 ma-0 mx-3 slider" color="primary" :value="volume" @input="setVolume" :max="100"></v-slider>
			</v-flex>
		</v-layout>
	</v-card>
</template>

<script>
import { mapActions, mapState } from "vuex";
export default {
  data() {
    return {
      opened: false
    };
  },
  computed: {
    ...mapState("player", [
      "current",
      "paused",
      "volume",
      "position",
      "duration"
    ]),
    ...mapState("settings", ["showDisk"])
  },
  methods: {
    ...mapActions("player", ["play", "pause", "seek", "setVolume"]),
    ...mapActions("musics", ["next", "previous"])
  },
  filters: {
    duration(val) {
      if (isFinite(val)) {
        const seconds = val % 60;
        const minutes = (val - seconds) / 60;

        return `${minutes}:${seconds.toString().padStart(2, "0")}`;
      } else return "0:00";
    }
  }
};
</script>

<style lang="scss">
$xs-bar-height: 56px;

.player-bar {
  position: fixed;
  bottom: 0;
  width: 100%;
  z-index: 4;

  .music {
    min-width: 250px;
    display: flex;
    align-items: center;
  }

  .volume {
    min-width: 175px;
  }

  .title,
  .artist {
    text-overflow: ellipsis;
    white-space: nowrap;
    font-style: normal;
    overflow: hidden;
    display: block;
  }

  .title {
    font-weight: 500;
    font-size: 14px !important;
  }

  .artist {
    color: #555;
    font-size: 13px;
  }

  .flex-0 {
    flex: 0;
  }

  .display-flex {
    display: flex;
    align-items: center;
    justify-content: space-evenly;
  }

  .infos,
  .disk {
    display: inline-block;
  }

  .slider-layout {
    height: 100%;
  }

  $disk-size: 32px;
  $inner-size: 10px;

  @keyframes spin {
    from {
      transform: rotate(0deg);
    }
    to {
      transform: rotate(360deg);
    }
  }

  .disk {
    width: $disk-size;
    height: $disk-size;
    border-radius: 50%;
    position: relative;
    animation: spin 2s linear infinite;
    box-shadow: 0 0 5px black;
    overflow: hidden;

    /* Youtube add black border to it's image, so we need to keep yt aspec ratio */
    & > img {
      position: absolute;
      height: $disk-size;
      width: 9/5 * $disk-size;
      left: -(4/5 * $disk-size)/2;
      object-fit: cover;
    }

    &.paused {
      animation-play-state: paused;
    }

    .inner {
      box-shadow: 0 0 2.5px black inset;
      position: absolute;
      border-radius: 50%;
      top: ($disk-size - $inner-size)/2;
      left: ($disk-size - $inner-size)/2;
      width: $inner-size;
      height: $inner-size;
      background: white;
    }
  }

  .infos {
    padding-left: 8px;
    width: calc(100% - #{$disk-size});
  }

  @media screen and (max-width: 959px) {
    .music,
    .volume,
    .controls {
      min-width: 33.33%;
    }

    .slider-row {
      order: 4;
      padding: 0 12px;
    }
  }

  @media screen and (max-width: 599px) {
    bottom: $xs-bar-height - 32px;

    .music {
      min-width: 60%;
    }

    .controls {
      min-width: 40%;
    }

    &.opened {
      bottom: $xs-bar-height;

      .song-progress {
        opacity: 0;
      }
    }

    .controls .v-btn {
      margin: 0;
    }

    .slider-layout {
      height: 70%;
    }
  }
}

.theme--dark .player-bar {
  .artist {
    color: #aaa;
  }
  .disk .inner {
    background: #424242;
  }
}
</style>
