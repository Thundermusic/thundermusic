<template>
    <div id="search">
        <v-form @submit.prevent="search">
            <v-text-field solo :readonly="searching" v-model="query" autofocus autocapitalize="off" autocomplete="off" spellcheck="false" autocorrect="off" placeholder="Rechercher..." append-icon="search" />
        </v-form>

        <MusicList :content="results" />
    </div>
</template>

<script>
    import MusicList from '../components/MusicList';

    export default {
        name: 'search',
        components: { MusicList },

        data() {
            return {
                searching: false,
                query: '',
                results: []
            }
        },
        methods: {
            search() {
                this.searching = true;

                fetch(`${this.$api}search?query=${encodeURIComponent(this.query)}`)
                    .then(res => res.json())
                    .then(results => {
                        this.results = results;
                        this.searching = false;
                    }).catch(() => {
                        this.searching = false;
                    }); // TODO: catch
            }
        }
    }
</script>

<style>
    #search .music-list {
        margin-top: -31px;
    }
</style>