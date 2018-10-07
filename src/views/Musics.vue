<template>
    <div id="musics">
        <MusicList :content="musics" :sideButtons="true" :canPlay="true" />
    </div>
</template>

<script>
    import MusicList from '../components/MusicList';

    export default {
        name: 'musics',
        components: { MusicList },

        computed: {
            musics() {
                return this.$store.state.musics;
            }
        }
    }
</script>

<style>

</style>