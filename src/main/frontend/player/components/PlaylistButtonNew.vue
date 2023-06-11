<template>
  <div class="inline">
    <button data-target="playlist-new" class="btn modal-trigger btn-block">Новый плейлист</button>
    <div id="playlist-new" class="modal">
      <div class="modal-content">
        <h4>Новый плейлист</h4>
        <label>
          <input type="text" v-model="playlistName" placeholder="Название"/>
        </label>
      </div>
      <div class="modal-footer">
        <button class="waves-effect waves-green btn" @click.prevent="playlistCreate">Добавить
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import Materialize from "materialize-css";
import {API} from "../../common/utils";

export default {
  data() {
    return {
      playlistName: '',
      modal: null,
    };
  },
  mounted() {
    this.modal = Materialize.Modal.init(document.querySelector('#playlist-new'));
  },
  methods: {
    playlistCreate() {
      API.createPlaylist(this.playlistName)
          .then(response => {
            this.playlistName = null;
            this.modal.close();
            this.$emit('playlist-created', response.data);
          })
          .catch(console.error);
    },
  },
}
</script>

<style scoped>

</style>