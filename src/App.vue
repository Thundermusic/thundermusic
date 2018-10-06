<template>
    <v-app>
        <v-card height="100%" flat>
            <div id="content">
                <router-view/>
            </div>
            <v-bottom-nav :value="true" absolute color="transparent">
                <v-btn color="primary" flat to="/search">
                    <span>Rechercher</span>
                    <v-icon>search</v-icon>
                </v-btn>
                <v-btn color="primary" flat to="/musics">
                    <span>Musiques</span>
                    <v-icon>library_music</v-icon>
                </v-btn>
                <v-btn color="primary" flat to="/playlists">
                    <span>Playlists</span>
                    <v-icon>queue_music</v-icon>
                </v-btn>
                <v-btn color="primary" flat to="/settings">
                    <span>Paramètres</span>
                    <v-icon>settings</v-icon>
                </v-btn>
            </v-bottom-nav>
        </v-card>
    </v-app>
</template>

<script>
export default {
    name: 'App',

    data() {
        return {
        }
    }
}
</script>
