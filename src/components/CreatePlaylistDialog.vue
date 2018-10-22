<template>
	<v-dialog
	  :value="value"
		@input="value => $emit('input', value)"
		:fullscreen="fullscreen"
		:max-width="fullscreen ? undefined : '500px'"
		hide-overlay
		:transition="fullscreen ? 'dialog-bottom-transition' : undefined"
	>
		<v-card>
			<v-toolbar v-if="fullscreen" dark color="primary">
				<v-btn icon dark @click.native="$emit('input', false)">
					<v-icon>close</v-icon>
				</v-btn>
				<v-toolbar-title>Create Playlist</v-toolbar-title>
			</v-toolbar>
			<v-card-title v-else>
        <span class="headline">Create Playlist</span>
      </v-card-title>
			<v-expansion-panel v-model="currentPanel">
				<v-expansion-panel-content
					v-for="component in playlistComponents"
					:key="component.header"
				>
					<div slot="header"> {{ component.header }}</div>
					<component :is="component" @close="$emit('input', false)" />
				</v-expansion-panel-content>
				<v-expansion-panel-content>
					<div slot="header">New</div>
				</v-expansion-panel-content>
			</v-expansion-panel>
		</v-card>
	</v-dialog>
</template>

<script>
import { playlistComponents } from "../platform/web";

export default {
  props: {
    fullscreen: Boolean,
    value: Boolean
  },
  data() {
    return {
      currentPanel: 0,
      playlistComponents
    };
  }
};
</script>

<style>
</style>
