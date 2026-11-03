<template>
  <div class="page-wrapper">
    <el-card>
      <h3>火车票预订</h3>
      <div v-if="queryText" style="margin-top: 6px; color: #666; font-size: 12px;">
        当前筛选：{{ queryText }}
      </div>
      <el-table :data="filteredTrains" style="margin-top: 12px" @row-click="goDetail">
        <el-table-column label="封面" width="120">
          <template #default="{ row }">
            <el-image
              v-if="firstImage(row.coverImage)"
              :src="firstImage(row.coverImage)"
              :preview-src-list="[firstImage(row.coverImage)]"
              fit="cover"
              style="width: 96px; height: 54px; border-radius: 6px"
            />
          </template>
        </el-table-column>
        <el-table-column prop="trainNo" label="车次" width="120" />
        <el-table-column prop="fromCity" label="出发" />
        <el-table-column prop="toCity" label="到达" />
        <el-table-column prop="departTime" label="出发时间" width="170" />
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
const trains = ref([]);

const firstImage = (value) => {
  if (!value) return '';
  const arr = String(value).split(',').map((s) => s.trim()).filter((s) => s);
  return arr[0] || '';
};

const load = async () => {
  const res = await api.get('/transport/trains');
  if (res.data.code === 0) trains.value = res.data.data || [];
};

const filteredTrains = computed(() => {
  const from = (route.query.from || '').toString().trim();
  const to = (route.query.to || '').toString().trim();
  const date = (route.query.date || '').toString().trim();
  return trains.value.filter((t) => {
    const okFrom = !from || (t.fromCity || '').includes(from);
    const okTo = !to || (t.toCity || '').includes(to);
    const okDate = !date || (t.departTime || '').startsWith(date);
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
  router.push(`/train/${row.id}`);
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

