<template>
    <div id="settings">
        Paramètres
        <v-container fluid px-0>
            <v-switch label="Show Disk" :inputValue="showDisk" @change="value => set({ key: 'showDisk', value })"></v-switch>
        </v-container>
    </div>
</template>

<script>
import { mapState, mapActions } from "vuex";
export default {
  computed: mapState("settings", ["showDisk"]),
  methods: mapActions("settings", ["set"])
};
</script>
