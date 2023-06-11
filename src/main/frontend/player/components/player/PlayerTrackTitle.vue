<template>
  <h5 v-if="visible && title">{{ title }}</h5>
  <h5 v-else>&nbsp;</h5>
</template>

<script>
export default {
  props: ['title', 'visible']
}
</script>

<style scoped>

</style>