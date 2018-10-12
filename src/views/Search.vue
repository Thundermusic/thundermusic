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

    import { search, addDuration } from '../youtube/search'

    export default {
        name: 'search',
        components: { Loading, MusicList },

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

                search(this.query)
                    .then(results => {
                        this.results = results;
                        addDuration(results);
                        this.searching = false;                        
                    }, (e) => {
                        this.searching = false
                        throw e;
                    })
            }
        }
    }
</script>

<style>
    #search .music-list {
        margin-top: -31px;
    }
</style>