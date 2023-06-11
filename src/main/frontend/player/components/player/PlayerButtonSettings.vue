<template>
  <div style="display: inline-block;">
    <a class="waves-effect waves-light btn-flat modal-trigger" title="Настройки" href="#settings">
      <i class="material-icons">settings</i>
    </a>
    <div id="settings" class="modal bottom-sheet">
      <div class="modal-content">
        <h4>Настройки</h4>
        <div class="row">
          <div class="col s2">
            <p>
              <label>
                <input type="checkbox" v-model="showTitle"/>
                <span>Показать название</span>
              </label>
            </p>
          </div>
          <div class="col s2">
            <p>
              <label>
                <input type="checkbox" v-model="showPlaylist"/>
                <span>Показать плейлист</span>
              </label>
            </p>
          </div>
        </div>
        <div class="row">
          <div class="col s4">
            <a href="/controls" target="_blank">Управление</a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Materialize from "materialize-css";

export default {
  props: ['title', 'playlist'],
  data() {
    return {
      showTitle: this.title,
      showPlaylist: this.playlist,
    };
  },
  watch: {
    showTitle() {
      this.$emit('toggle-title', this.showTitle);
    },
    showPlaylist() {
      this.$emit('toggle-playlist', this.showPlaylist);
    },
  },
  mounted() {
    Materialize.Modal.init(document.querySelector('#settings'));
  },
}
</script>

<style scoped>

</style>