<template>
    <v-app>
        <v-toolbar app fixed color="white">
            <v-toolbar-side-icon @click="mini = !mini" v-if="$vuetify.breakpoint.smAndUp">
                <v-icon>menu</v-icon>
            </v-toolbar-side-icon>
            <img id="icon" src="./assets/icon.png" width="35"/>
            <v-toolbar-title>{{ $route.meta.name_fr }}</v-toolbar-title>
            <v-spacer></v-spacer>
            <v-icon v-if="$route.meta.name_fr === 'Rechercher'" @click="$router.push('/downloads')">cloud_download</v-icon>
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
import PlayerBar from "./components/layout/PlayerBar.vue"
import MobileNavbar from "./components/layout/MobileNavbar.vue"
import Navbar from "./components/layout/Navbar.vue"

export default {
    name: 'App',
    data() {
        return {
            mini: false,
        }
    },
    mounted() {
        this.$store.dispatch('load');
    },
    components: {
        PlayerBar,
        MobileNavbar,
        Navbar
    }
}
</script>

<style lang="scss">
    html {
        user-select: none;
    }

    .v-content {
       margin-bottom: 48px; //PlayerBar heigth 
    }

</style>
