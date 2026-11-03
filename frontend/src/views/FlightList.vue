<template>
  <div class="page-wrapper">
    <el-card>
      <h3>机票预订</h3>
      <div v-if="queryText" style="margin-top: 6px; color: #666; font-size: 12px;">
        当前筛选：{{ queryText }}
      </div>
      <el-table :data="filteredFlights" style="margin-top: 12px" @row-click="goDetail">
        <el-table-column prop="flightNo" label="航班号" width="120" />
        <el-table-column prop="fromCity" label="出发" />
        <el-table-column prop="toCity" label="到达" />
        <el-table-column prop="departTime" label="起飞时间" width="170" />
        <el-table-column prop="arriveTime" label="到达时间" width="170" />
        <el-table-column prop="price" label="价格" width="100" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import api from '../api';

const route = useRoute();
const router = useRouter();
const flights = ref([]);

const load = async () => {
  const res = await api.get('/transport/flights');
  if (res.data.code === 0) flights.value = res.data.data || [];
};

const filteredFlights = computed(() => {
  const from = (route.query.from || '').toString().trim();
  const to = (route.query.to || '').toString().trim();
  const date = (route.query.date || '').toString().trim();
  return flights.value.filter((f) => {
    const okFrom = !from || (f.fromCity || '').includes(from);
    const okTo = !to || (f.toCity || '').includes(to);
    const okDate = !date || (f.departTime || '').startsWith(date);
    return okFrom && okTo && okDate;
  });
});

const queryText = computed(() => {
  const from = (route.query.from || '').toString().trim();
  const to = (route.query.to || '').toString().trim();
  const date = (route.query.date || '').toString().trim();
  const parts = [];
  if (from) parts.push(`出发=${from}`);
  if (to) parts.push(`到达=${to}`);
  if (date) parts.push(`日期=${date}`);
  return parts.join('，');
});

const goDetail = (row) => {
  router.push(`/flight/${row.id}`);
};

onMounted(() => load());
</script>

<style scoped>
.page-wrapper {
  width: 70%;
  max-width: 1100px;
  margin: 24px auto;
}
</style>

