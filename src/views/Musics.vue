<template>
    <div id="musics">
        <music-list :musics="musics" :selected="current && [current.id]" @select="play">
            <template slot-scope="{ music }">
                <v-list-tile-action>
                    <v-icon color="grey lighten-1">add</v-icon>
                    <v-icon color="grey lighten-1">edit</v-icon>
                </v-list-tile-action>
            </template>
        </music-list>
    </div>
</template>

<script>
    import MusicList from '../components/MusicList';
    import { mapState, mapActions } from 'vuex';

    export default {
        name: 'musics',
        components: { MusicList },
        computed: mapState(['musics', 'current']),
        methods: mapActions(['play'])
    }
</script>

<style>

</style>