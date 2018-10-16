<template>
    <div class="loading" :class="{ 'displayed': isLoading }">
        <v-progress-circular indeterminate color="primary" :size="50" :width="5"></v-progress-circular>
    </div>
</template>

<script>
export default {
  name: "loading",
  props: ["isLoading"]
};
</script>

<style>
.loading {
  position: absolute;
  top: calc(50vh - 60px);
  left: calc(50% - 25px);

  display: none;
}

.loading.displayed {
  display: block;
}
</style>
