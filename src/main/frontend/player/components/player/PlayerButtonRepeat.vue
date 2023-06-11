<template>
  <button @click.prevent="toggle" :class="{'teal white-text': repeat}" class="btn-flat"
          title="Режим повтора">
    <i class="material-icons">repeat</i>
  </button>
</template>

<script>
export default {
  props: ['repeat'],
  methods: {
    toggle() {
      this.$emit('repeat', !this.repeat);
    },
  },
}
</script>

<style scoped>

</style>