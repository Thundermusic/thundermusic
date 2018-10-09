<template>
    <div id="downloads">
        <div v-for="download of downloads">
            {{ download.title }} => {{ download.progress }}
        </div>
    </div>
</template>

<script>
    export default {
        name: 'downloads',

        computed: {
            downloads() {
                return this.$store.state.downloads;
            }
        }
    }
</script>

<style>

</style>