<template>
  <el-card>
    <h3>我的优惠券</h3>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="可用" name="available">
        <div v-if="availableList.length" class="coupon-list">
          <div v-for="c in availableList" :key="c.id" class="coupon-card available">
            <div class="coupon-left">
              <div class="coupon-name">{{ c.couponName }}</div>
              <div class="coupon-desc">无门槛 · 下单景点或酒店时抵扣</div>
            </div>
            <div class="coupon-right">
              <span class="discount">{{ discountLabel(c.discountRate) }}</span>
            </div>
          </div>
        </div>
        <div v-else class="empty-hint">暂无可用优惠券</div>
      </el-tab-pane>
      <el-tab-pane label="已使用" name="used">
        <div v-if="usedList.length" class="coupon-list">
          <div v-for="c in usedList" :key="c.id" class="coupon-card used">
            <div class="coupon-left">
              <div class="coupon-name">{{ c.couponName }}</div>
              <div class="coupon-desc" v-if="c.usedAt">使用时间：{{ formatTime(c.usedAt) }}</div>
            </div>
            <div class="coupon-right">
              <span class="discount">{{ discountLabel(c.discountRate) }}</span>
            </div>
          </div>
        </div>
        <div v-else class="empty-hint">暂无已使用的优惠券</div>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import api from '../api';

const activeTab = ref('available');
const allCoupons = ref([]);

const loadCoupons = async () => {
  try {
    const res = await api.get('/coupons/my', { params: { used: true } });
    if (res.data.code === 0) allCoupons.value = res.data.data || [];
  } catch {
    ElMessage.error('加载优惠券失败');
  }
};

const availableList = computed(() => allCoupons.value.filter(c => !c.used));
const usedList = computed(() => allCoupons.value.filter(c => c.used));

function discountLabel(rate) {
  if (rate == null) return '';
  const p = Math.round((1 - Number(rate)) * 100);
  return p + '% off';
}

function formatTime(t) {
  if (!t) return '';
  const d = new Date(t);
  return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0') + ' ' + String(d.getHours()).padStart(2, '0') + ':' + String(d.getMinutes()).padStart(2, '0');
}

onMounted(() => loadCoupons());
</script>

<style scoped>
.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.coupon-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #eee;
}
.coupon-card.available {
  border-color: #1e73be;
  background: linear-gradient(135deg, #f0f7ff 0%, #fff 100%);
}
.coupon-card.used {
  background: #f5f5f5;
  color: #999;
}
.coupon-left .coupon-name {
  font-weight: 600;
  font-size: 16px;
}
.coupon-left .coupon-desc {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}
.coupon-card.used .coupon-desc {
  color: #999;
}
.coupon-right .discount {
  font-size: 18px;
  font-weight: bold;
  color: #e6a23c;
}
.coupon-card.used .discount {
  color: #999;
}
.empty-hint {
  color: #999;
  padding: 24px;
  text-align: center;
}
</style>
