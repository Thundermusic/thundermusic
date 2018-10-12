<template>
	<v-card class="player-bar elevation-6" :class="{ opened }">
		<v-layout row wrap @click="opened = !opened">
			<v-flex sm2 xs6 class="pl-2 pt-2">
				<span class="title">{{ current.title }}</span>
				<span class="artist">{{ current.artist || current.channel }}</span>
			</v-flex>
			<v-flex sm8 xs12 class="slider-row">
				<v-layout row fill-height>
					<v-flex class="flex-0">
						<span>{{ songPosition | duration }}</span>
					</v-flex>
					<v-flex>
						<v-slider class="px-2 ma-0 mx-3 slider" color="primary" v-model="songPosition" :max="songDuration"></v-slider>
					</v-flex>
					<v-flex class="flex-0">
						<span>{{ songDuration | duration }}</span>
					</v-flex>
				</v-layout>
			</v-flex>
			<v-flex sm2 xs6 class="text-xs-center">
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
			<v-flex xs12>
				<v-progress-linear 
					v-if="$vuetify.breakpoint.xsOnly"
					class="song-progress ma-0"
					color="primary"
					background-color="transparent"
					height="2"
					:value="positionValue"
				></v-progress-linear>
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
	.player-bar {
		position: fixed;
		bottom: 56px;
		width: 100%;

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

		.slider {
			height: 100%;

			.v-input__control, .v-input__slot {
				height: 100%;
			}
			.v-messages {
				display: none;
			}

			.v-input__slot {
				margin-bottom: 0;
			}
		}

		.flex-0 {
			flex: 0;
			display: flex;
			align-items: center;
		}


		@media screen and (max-width: 600px) {
			bottom: 56px - 32px;

			.slider-row {
				order: 4;
				padding: 0 12px;
			}

			&.opened {
				bottom: 56px;

				.song-progress {
					opacity: 0;
				}
			}
		}
	}
</style>
