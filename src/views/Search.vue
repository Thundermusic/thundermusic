<template>
    <div id="search">
        <loading :isLoading="searching" />

        <v-form @submit.prevent="search" class="input">
            <v-text-field
              hide-details
              :readonly="searching"
              box
              single-line
              v-model="query"
              autofocus
              autocapitalize="off"
              autocomplete="off"
              spellcheck="false"
              autocorrect="off"
              placeholder="Rechercher..."
              append-icon="search"
            />
        </v-form>

        <music-list :musics="results" :selected="selected" @select="select"/>
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
                results: [],
                selected: []
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
            },
            select(music) {
                const index = this.selected.indexOf(music.id)
                if (index >= 0)
                    this.selected.splice(index, 1)
                else
                    this.selected.push(music.id)
                this.$store.dispatch('download', music)
            }
        }
    }
</script>

<style lang="scss">
    #search {
        padding-top: 56px;

        .input {
            position: fixed;
            top: 64px;
            width: 100%;
            z-index: 1;
            background: white;
        }
    }
</style>