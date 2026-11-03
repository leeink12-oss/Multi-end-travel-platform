<template>
  <div class="booking-wrapper">
    <TransportBookingPanel />
  </div>
</template>

<script setup>
import TransportBookingPanel from '../components/TransportBookingPanel.vue';
</script>

<style scoped>
.booking-wrapper {
  width: 70%;
  max-width: 1100px;
  margin: 24px auto;
}
</style>

