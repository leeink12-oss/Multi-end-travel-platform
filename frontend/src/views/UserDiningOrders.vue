<template>
  <div class="page-shell">
    <el-card class="page-card" :body-style="{ padding: '0' }">
      <div class="page-header">
        <div>
          <div class="page-kicker">我的餐饮 / Dining</div>
          <h2 class="page-title">我的餐饮预订</h2>
          <div class="page-subtitle">统一查看订座记录和团购订单，快速处理退款和到店安排。</div>
        </div>
        <div class="header-badge">{{ reservations.length + grouponOrders.length }} 条记录</div>
      </div>
      <div class="table-shell">
        <el-table :data="reservations" class="order-table" stripe>
          <el-table-column prop="id" label="预订号" width="90" />
          <el-table-column label="餐厅" width="180">
            <template #default="{ row }">{{ row.restaurant?.name }}</template>
          </el-table-column>
          <el-table-column prop="reserveTime" label="到店时间" width="180" />
          <el-table-column prop="peopleCount" label="人数" width="80" />
          <el-table-column prop="contactName" label="联系人" width="110" />
          <el-table-column prop="contactPhone" label="电话" width="130" />
          <el-table-column prop="status" label="状态" width="110" />
          <el-table-column prop="createTime" label="创建时间" />
        </el-table>

        <h3 class="section-title">我的团购订单</h3>
        <el-table :data="grouponOrders" class="order-table" stripe>
          <el-table-column prop="id" label="订单号" width="90" />
          <el-table-column label="套餐" width="200">
            <template #default="{ row }">{{ row.grouponPackage?.title }}</template>
          </el-table-column>
          <el-table-column label="店铺" width="170">
            <template #default="{ row }">{{ row.grouponPackage?.restaurant?.name }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="totalPrice" label="总价" width="110" />
          <el-table-column prop="status" label="状态" width="110" />
          <el-table-column prop="verifyCode" label="核销码" width="100" />
          <el-table-column prop="createTime" label="创建时间" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button
                v-if="row.status === 'UNUSED'"
                size="small"
                type="warning"
                @click="refundOrder(row)"
              >
                申请退款
              </el-button>
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

const reservations = ref([]);
const grouponOrders = ref([]);

const loadData = async () => {
  try {
    const [rRes, gRes] = await Promise.all([
      api.get('/restaurants/my-reservations'),
      api.get('/restaurants/my-groupon-orders')
    ]);
    if (rRes.data.code === 0) reservations.value = rRes.data.data || [];
    if (gRes.data.code === 0) grouponOrders.value = gRes.data.data || [];
  } catch {
    ElMessage.error('请先登录用户账号');
  }
};

const refundOrder = async (row) => {
  try {
    const res = await api.put(`/restaurants/my-groupon-orders/${row.id}/refund`);
    if (res.data.code === 0) {
      ElMessage.success('退款成功');
      await loadData();
    } else {
      ElMessage.error(res.data.message || '退款失败');
    }
  } catch {
    ElMessage.error('退款失败');
  }
};

onMounted(loadData);
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
.section-title {
  font-size: 16px;
  font-weight: 800;
  color: #1a1a1a;
  margin: 16px 0 10px;
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

