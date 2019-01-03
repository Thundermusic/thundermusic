<template>
    <v-app :dark="dark">
        <v-toolbar app fixed>
            <v-toolbar-side-icon @click="mini = !mini" v-if="$vuetify.breakpoint.smAndUp">
                <v-icon>menu</v-icon>
            </v-toolbar-side-icon>
            <img id="icon" src="./assets/icon.png" width="35"/>
            <v-toolbar-title>
              {{ $route.meta.name_fr }}
              <portal-target name="title" />
            </v-toolbar-title>
            <v-spacer></v-spacer>
            <portal-target name="toolbar" />
            <v-menu left>
              <v-btn icon slot="activator">
                <v-icon color="primary">{{ modes[mode] }}</v-icon>
              </v-btn>

              <v-list>
                <v-list-tile
                  v-for="(icon, amode) in modes"
                  :key="amode"
                >
                    <v-btn icon @click="setMode(amode)">
                      <v-icon :color="mode === amode ? 'primary' : 'black'">{{ icon }}</v-icon>
                    </v-btn>
                </v-list-tile>
              </v-list>
            </v-menu>

        </v-toolbar>
        <v-content>
            <router-view/>
        </v-content>
        <player-bar/>
        <mobile-navbar v-if="$vuetify.breakpoint.xsOnly"/>
        <navbar :mini="mini" @input="value => mini = value" v-else/>
    </v-app>
</template>

<script>
import PlayerBar from "./components/layout/PlayerBar.vue";
import MobileNavbar from "./components/layout/MobileNavbar.vue";
import Navbar from "./components/layout/Navbar.vue";
import { mapState, mapActions } from "vuex";

const modes = {
  linear: "arrow_right_alt",
  random: "shuffle",
  loop: "repeat"
};

export default {
  name: "App",
  data() {
    return {
      mini: false,
      modes
    };
  },
  computed: {
    ...mapState("musics", ["mode"]),
    ...mapState("settings", ["dark"])
  },
  methods: mapActions("musics", ["setMode"]),
  components: {
    PlayerBar,
    MobileNavbar,
    Navbar
  }
};
</script>

<style lang="scss">
html {
  user-select: none;
}

.v-content {
  margin-bottom: 48px; //PlayerBar heigth
}

.v-list__tile--active {
  border-left: solid 3px #f67504;
}

.theme--light.v-toolbar {
  background-color: white;
}
</style>
