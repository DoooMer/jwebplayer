<template>
  <button @click.prevent="next" class="btn-flat waves-effect waves-teal" title="Следующий трек">
    <i class="material-icons">skip_next</i>
  </button>
</template>

<script>
import {API} from "../../../common/utils.js";

export default {
  methods: {
    next() {
      // get link to next track
      API.playbackNext()
          .then(response => {
            this.$emit('next', {
              trackUrl: response.data.downloadUrl,
              trackName: response.data.name,
              trackId: response.data.id,
            })
          })
          .catch(e => {
            console.error(e);
            this.next();
          });
    },
  }
}
</script>

<style scoped>

</style>