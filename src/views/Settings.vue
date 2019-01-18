<template>
    <div id="settings">
        <v-container fluid py-0>
            <v-subheader>UI</v-subheader>
            <v-switch label="Show Disk" :inputValue="showDisk" @change="value => set({ key: 'showDisk', value })" ma-0 hide-details></v-switch>
            <v-switch label="Dark Theme" :inputValue="dark" @change="value => set({ key: 'dark', value })" hide-details></v-switch>
            <v-subheader>Sync</v-subheader>
        </v-container>
    </div>
</template>

<script>
import { mapState, mapActions } from "vuex";
export default {
  computed: mapState("settings", ["showDisk", "dark"]),
  methods: mapActions("settings", ["set"])
};
</script>
