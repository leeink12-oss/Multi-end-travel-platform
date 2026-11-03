<template>
  <el-badge :value="count || ''" :hidden="!count" :max="99">
    <slot />
  </el-badge>
</template>

<script setup>
defineProps({
  count: {
    type: Number,
    default: 0
  }
});
</script>
