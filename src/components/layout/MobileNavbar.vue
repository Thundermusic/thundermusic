<template>
	<v-bottom-nav app :value="true" fixed color="white">
		<v-btn v-for="(route, index) in routes" :key="index" color="primary" flat :to="route">
			<span>{{ route.meta.name_fr }}</span>
			<v-icon>{{ route.meta.icon }}</v-icon>
		</v-btn>
	</v-bottom-nav>
</template>

<script>
export default {
  computed: {
    routes() {
      return this.$router.options.routes.filter(
        ({ meta: { link } = {} }) => link
      );
    }
  }
};
</script>

<style>
</style>
