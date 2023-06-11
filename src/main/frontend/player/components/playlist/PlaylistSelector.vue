<template>
  <button class="btn btn-block btn-flat waves-effect waves-teal hover-teal truncate" :title="unit.name"
          @click.prevent="selectPlaylist(unit.id)">
    {{ unit.name }}
  </button>
</template>

<script>
import {API} from "../../../common/utils.js";
import Materialize from "materialize-css";

export default {
  props: ['unit'],
  methods: {
    selectPlaylist(id) {
      API.selectPlaylist(id)
          .then(() => {
            this.$emit('playlist-selected', id);
          })
          .catch(e => {
            console.error('Ошибка выбора плейлиста: ', e);
            Materialize.toast({html: 'Ошибка переключения плейлиста'});
          });

    },
  },
}
</script>

<style scoped>

</style>