<template>
    <div id="search">
        <Loading :isLoading="searching" />

        <v-form @submit.prevent="search">
            <v-text-field solo :readonly="searching" v-model="query" autofocus autocapitalize="off" autocomplete="off" spellcheck="false" autocorrect="off" placeholder="Rechercher..." append-icon="search" />
        </v-form>

        <MusicList :content="results" :download="true" />
    </div>
</template>

<script>
    import MusicList from '../components/MusicList';
    import Loading from "../components/Loading";

    export default {
        name: 'search',
        components: {Loading, MusicList },

        data() {
            return {
                searching: false,
                query: '',
                results: []
            }
        },
        methods: {
            search() {
                this.results = [];
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