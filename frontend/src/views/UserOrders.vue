<template>
  <div class="page-shell">
    <el-card class="page-card" :body-style="{ padding: '0' }">
      <div class="page-header">
        <div>
          <div class="page-kicker">我的订单 / Orders</div>
          <h2 class="page-title">我的订单</h2>
          <div class="page-subtitle">查看酒店、景点、交通和餐饮订单的最新状态，统一管理出行行程。</div>
        </div>
        <div class="header-badge">{{ orders.length }} 条记录</div>
      </div>
      <div class="table-shell">
        <el-table :data="orders" class="order-table" stripe>
      <el-table-column prop="id" label="订单号" width="90" />
      <el-table-column label="类型" width="80">
        <template #default="scope">
          <span v-if="scope.row.targetType === 'HOTEL'">酒店</span>
          <span v-else-if="scope.row.targetType === 'SCENIC'">景点</span>
          <span v-else-if="scope.row.targetType === 'FLIGHT'">飞机票</span>
          <span v-else-if="scope.row.targetType === 'TRAIN'">火车票</span>
          <span v-else>{{ scope.row.targetType }}</span>
        </template>
      </el-table-column>
      <el-table-column label="名称" min-width="180">
        <template #default="scope">
          <span v-if="scope.row.targetType === 'HOTEL'">{{ hotelMap[scope.row.targetId]?.name || '酒店订单' }}</span>
          <span v-else-if="scope.row.targetType === 'SCENIC'">{{ scenicMap[scope.row.targetId]?.name || '景点订单' }}</span>
          <span v-else-if="scope.row.targetType === 'FLIGHT'">{{ transportMap[scope.row.targetId]?.name || '航班订单' }}</span>
          <span v-else-if="scope.row.targetType === 'TRAIN'">{{ transportMap[scope.row.targetId]?.name || '火车订单' }}</span>
          <span v-else-if="scope.row.targetType === 'RESTAURANT'">{{ restaurantMap[scope.row.targetId]?.name || '餐饮订单' }}</span>
          <span v-else>{{ scope.row.targetName || scope.row.merchantName || '订单' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" width="80" />
      <el-table-column prop="totalPrice" label="总价" width="100" />
      <el-table-column label="行程时间" width="220">
        <template #default="scope">
          <span v-if="scope.row.targetType === 'HOTEL'">
            {{ scope.row.checkInDate }} 至 {{ scope.row.checkOutDate }}
          </span>
          <span v-else-if="scope.row.targetType === 'SCENIC'">
            出行日期：{{ scope.row.useDate }}
          </span>
          <span v-else-if="scope.row.targetType === 'FLIGHT'">
            {{ transportMap[scope.row.targetId]?.departTime || '—' }} 至 {{ transportMap[scope.row.targetId]?.arriveTime || '—' }}
          </span>
          <span v-else-if="scope.row.targetType === 'TRAIN'">
            {{ transportMap[scope.row.targetId]?.departTime || '—' }} 至 {{ transportMap[scope.row.targetId]?.arriveTime || '—' }}
          </span>
          <span v-else-if="scope.row.targetType === 'RESTAURANT'">餐饮订座</span>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column label="出行/入住状态" width="160">
        <template #default="scope">
          <span v-if="scope.row.targetType === 'HOTEL'">
            {{ scope.row.started ? '已入住' : '未入住' }}
          </span>
          <span v-else-if="scope.row.targetType === 'SCENIC'">
            {{ scope.row.started ? '已出行' : '未出行' }}
          </span>
          <span v-else-if="scope.row.targetType === 'FLIGHT' || scope.row.targetType === 'TRAIN'">
            {{ scope.row.started ? '已检票' : '未检票' }}
          </span>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="订单状态" width="100" />
      <el-table-column prop="createTime" label="创建时间" />
      <el-table-column label="操作" width="160">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === 'CREATED'"
            size="small"
            type="warning"
            @click="requestCancel(scope.row)"
          >申请取消</el-button>
          <span v-else-if="scope.row.status === 'CANCEL_REQUESTED'">已申请取消</span>
        </template>
      </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import api from '../api';

const orders = ref([]);
// Vue 模板会自动解包 ref，因此这里直接当对象用：transportMap[id]
const transportMap = ref({});
const hotelMap = ref({});
const scenicMap = ref({});
const restaurantMap = ref({});

const loadOrders = async () => {
  try {
    const res = await api.get('/orders/my');
    if (res.data.code === 0) {
      orders.value = res.data.data;
      await loadRelatedDetails();
    }
  } catch {
    ElMessage.error('请先登录后再查看订单');
  }
};

const loadRelatedDetails = async () => {
  const flightIds = orders.value.filter(o => o.targetType === 'FLIGHT').map(o => o.targetId);
  const trainIds = orders.value.filter(o => o.targetType === 'TRAIN').map(o => o.targetId);
  const hotelIds = orders.value.filter(o => o.targetType === 'HOTEL').map(o => o.targetId);
  const scenicIds = orders.value.filter(o => o.targetType === 'SCENIC').map(o => o.targetId);
  const restaurantIds = orders.value.filter(o => o.targetType === 'RESTAURANT').map(o => o.targetId);
  const tMap = {};
  const hMap = {};
  const sMap = {};
  const rMap = {};

  await Promise.all([
    ...flightIds.map(async (id) => {
      try {
        const r = await api.get(`/transport/flights/${id}`);
        if (r.data.code === 0 && r.data.data) tMap[id] = r.data.data;
      } catch {}
    }),
    ...trainIds.map(async (id) => {
      try {
        const r = await api.get(`/transport/trains/${id}`);
        if (r.data.code === 0 && r.data.data) tMap[id] = r.data.data;
      } catch {}
    }),
    ...hotelIds.map(async (id) => {
      try {
        const r = await api.get(`/hotels/${id}`);
        if (r.data.code === 0 && r.data.data?.hotel) hMap[id] = r.data.data.hotel;
      } catch {}
    }),
    ...scenicIds.map(async (id) => {
      try {
        const r = await api.get(`/scenics/${id}`);
        if (r.data.code === 0 && r.data.data) sMap[id] = r.data.data;
      } catch {}
    }),
    ...restaurantIds.map(async (id) => {
      try {
        const r = await api.get(`/restaurants/${id}`);
        if (r.data.code === 0 && r.data.data?.restaurant) rMap[id] = r.data.data.restaurant;
      } catch {}
    })
  ]);

  transportMap.value = tMap;
  hotelMap.value = hMap;
  scenicMap.value = sMap;
  restaurantMap.value = rMap;
};

const requestCancel = async (row) => {
  try {
    await api.put(`/orders/${row.id}/cancel-request`);
    ElMessage.success('已提交取消申请');
    await loadOrders();
  } catch (e) {
    const msg = e?.response?.data?.message || '取消申请失败';
    ElMessage.error(msg);
  }
};

onMounted(() => {
  loadOrders();
});
</script>

<style scoped>
.page-shell {
  width: 70%;
  max-width: 1100px;
  margin: 24px auto 40px;
}
.page-card {
  border: none;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0,0,0,0.08);
}
.page-header {
  padding: 20px 20px 16px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  background: linear-gradient(135deg, #f7fbff 0%, #ffffff 100%);
  border-bottom: 1px solid #eef3f8;
}
.page-kicker {
  font-size: 12px;
  color: #7a8798;
  letter-spacing: 0.6px;
  text-transform: uppercase;
  margin-bottom: 6px;
}
.page-title {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  color: #1a1a1a;
}
.page-subtitle {
  margin-top: 6px;
  color: #667085;
  font-size: 13px;
  line-height: 1.7;
}
.header-badge {
  padding: 8px 12px;
  border-radius: 999px;
  background: #1e73be;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
}
.table-shell {
  padding: 8px 12px 14px;
}
.order-table {
  margin: 8px 0 4px;
}
.order-table :deep(.el-table__header th) {
  background: #f7fbff;
  color: #1a1a1a;
  font-weight: 700;
}
@media (max-width: 1100px) {
  .page-shell {
    width: 92%;
  }
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>

