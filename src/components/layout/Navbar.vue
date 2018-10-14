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
			<v-subheader>Playlists</v-subheader>
		</v-list>
	</v-navigation-drawer>
</template>

<script>
export default {
	props: ['mini'],
	computed: {
		routes() {
			return this.$router.options.routes.filter(({ meta: { link, mobileOnly } = {} }) => link && !mobileOnly)
		},
		isSmall() {
			return this.$vuetify.breakpoint.width < 1264
		}
	},
	watch: {
		isSmall(value) {
			if (value && !this.mini) this.$emit('input', true);
		}
	}
}
</script>

<style lang="scss">
	.navbar {
		padding-bottom: 48px; //PlayerBar heigth 
	}
</style>
