<template>
	<v-card class="player-bar elevation-6" :class="{ opened }">
		<v-layout row wrap @click="$vuetify.breakpoint.xsOnly ? opened = !opened : null">
			<v-flex class="pl-2 pt-2 infos flex-0">
				<span class="title">{{ current.title }}</span>
				<span class="artist">{{ current.artist || current.channel }}</span>
			</v-flex>
			<v-flex class="slider-row">
				<v-layout row fill-height class="mx-4">
					<v-flex class="flex-0 display-flex">
						<span>{{ songPosition | duration }}</span>
					</v-flex>
					<v-flex class="display-flex">
						<v-slider hide-details class="px-2 ma-0 mx-3 slider" color="primary" v-model="songPosition" :max="songDuration"></v-slider>
					</v-flex>
					<v-flex class="flex-0 display-flex">
						<span>{{ songDuration | duration }}</span>
					</v-flex>
				</v-layout>
			</v-flex>
			<v-flex class="text-xs-center flex-0 display-flex controls">
				<v-btn flat icon @click.prevent="previous">
					<v-icon>skip_previous</v-icon>
				</v-btn>
				<v-btn flat icon @click.prevent="pause">
					<v-icon>{{ paused ? 'play_arrow' : 'pause' }}</v-icon>
				</v-btn>
				<v-btn flat icon @click.prevent="next">
					<v-icon>skip_next</v-icon>
				</v-btn>
			</v-flex>
			<v-flex xs12 v-if="$vuetify.breakpoint.xsOnly">
				<v-progress-linear
					class="song-progress ma-0"
					color="primary"
					background-color="transparent"
					height="2"
					:value="positionValue"
				></v-progress-linear>
			</v-flex>
			<v-flex v-else class="display-flex flex-0 volume">
				<v-slider prepend-icon="volume_up" hide-details class="px-2 ma-0 mx-3 slider" color="primary" v-model="volume" :max="100"></v-slider>
			</v-flex>
		</v-layout>
	</v-card>
</template>

<script>
export default {
	data() {
		return {
			opened: false
		}
	},
	computed: {
        paused() {
            return this.$store.state.paused;
        },
        current() {
            return this.$store.state.current;
        },
        positionValue() {
            return Math.round(this.$store.state.position / this.$store.state.duration * 100);
        },
        songPosition: {
            get() {
                return Math.round(this.$store.state.position / 1000);
            },
            set(value) {
                this.$store.dispatch('seek', value * 1000);
            }
        },
        volume: {
            get() {
                return this.$store.state.volume;
            },
            set(value) {
                this.$store.dispatch('changeVolume', value);
            }
        },
        songDuration() {
            return Math.round(this.$store.state.duration / 1000);
        }
	},
	methods: {
        previous() {
            this.$store.dispatch('previous');
        },
        pause() {
            this.$store.dispatch('pause');
        },
        next() {
            this.$store.dispatch('next');
        },
	},
    filters: {
        duration(val) {
            const seconds = val % 60;
            const minutes = (val - seconds) / 60;

            return minutes + ':' + (seconds < 10 ? '0' : '') + seconds;
        }
    }
}
</script>

<style lang="scss">
	$xs-bar-height: 56px;

	.player-bar {
		position: fixed;
		bottom: 0;
		width: 100%;
		z-index: 4;

		.infos {
			min-width: 250px;
		}

		.volume {
			min-width: 175px;
		}

		.title, .artist {
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
		}

		@media screen and (max-width: 959px) {
			.infos, .volume, .controls {
				min-width: 33.33%;
			}

			.slider-row {
				order: 4;
				padding: 0 12px;
			}
		}


		@media screen and (max-width: 599px) {
			bottom: $xs-bar-height - 32px;

			.controls, .infos {
				min-width: 50%;
			}

			&.opened {
				bottom: $xs-bar-height;

				.song-progress {
					opacity: 0;
				}
			}
		}
	}
</style>
