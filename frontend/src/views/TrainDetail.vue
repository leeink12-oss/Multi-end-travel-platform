<template>
  <div class="page-wrapper">
    <el-card v-if="train" class="train-card" :body-style="{ padding: '0' }">
      <div class="hero">
        <div class="hero-left">
          <div class="breadcrumb">交通出行 / 火车票</div>
          <h2 class="title">{{ train.trainNo }}</h2>
          <div class="route-line">
            <span>{{ train.fromCity }}</span>
            <span>→</span>
            <span>{{ train.toCity }}</span>
          </div>
          <div class="hero-tags">
            <el-tag type="success" effect="light">{{ train.trainType === 'EMU' ? '动车/高铁' : '普通列车' }}</el-tag>
            <el-tag effect="light">{{ train.status }}</el-tag>
            <el-tag type="warning" effect="light">最低票价 ¥{{ train.price }}</el-tag>
          </div>
        </div>
        <div class="hero-right">
          <div class="price-label">最低票价</div>
          <div class="price">¥{{ train.price }}</div>
          <el-button type="primary" size="large" class="book-btn" @click="orderDialogVisible = true">立即预订</el-button>
        </div>
      </div>

      <el-image
        v-if="firstImage(train.coverImage)"
        :src="firstImage(train.coverImage)"
        :preview-src-list="[firstImage(train.coverImage)]"
        fit="cover"
        class="cover-image"
      />

      <div class="info-grid">
        <div class="info-card">
          <div class="info-title">出发时间</div>
          <div class="info-value">{{ formatDateTime(train.departTime) }}</div>
        </div>
        <div class="info-card">
          <div class="info-title">到达时间</div>
          <div class="info-value">{{ formatDateTime(train.arriveTime) }}</div>
        </div>
        <div class="info-card">
          <div class="info-title">出发城市</div>
          <div class="info-value">{{ train.fromCity }}</div>
        </div>
        <div class="info-card">
          <div class="info-title">到达城市</div>
          <div class="info-value">{{ train.toCity }}</div>
        </div>
      </div>

      <div class="tips-row">
        <div class="tips-card">
          <div class="tips-title">购票提示</div>
          <div class="tips-text">
            请选择合适席别后提交订单，支付完成后可在“我的订单”中查看订单状态。部分车次支持不同席别票价，系统会根据你所选席别自动计算预估金额。
          </div>
        </div>
        <div class="tips-card">
          <div class="tips-title">适用场景</div>
          <div class="tips-text">
            适合城市间短途出行、周末旅游、商务往返等场景。你可以先浏览车次详情，再结合出发时间和票价进行选择。
          </div>
        </div>
      </div>
    </el-card>

  <el-dialog v-model="orderDialogVisible" title="火车票下单" width="820px" class="booking-dialog">
    <div class="booking-dialog-body">
      <div class="booking-hero">
        <div>
          <div class="booking-kicker">火车票预订 / Train Booking</div>
          <div class="booking-title">{{ train.trainNo }}</div>
          <div class="booking-subtitle">请选择席别、人数和付款方式后提交订单</div>
        </div>
        <div class="booking-price-box">
          <div class="booking-price-label">当前单价</div>
          <div class="booking-price">¥{{ unitPrice.toFixed(2) }}</div>
          <div class="booking-price-muted">{{ train.fromCity }} → {{ train.toCity }}</div>
        </div>
      </div>

      <el-row :gutter="16" class="booking-content">
        <el-col :span="9">
          <div class="booking-summary">
            <el-image
              v-if="firstImage(train.coverImage)"
              :src="firstImage(train.coverImage)"
              fit="cover"
              class="booking-image"
            />
            <div class="summary-name">{{ train.trainNo }}</div>
            <div class="summary-meta">{{ train.fromCity }} → {{ train.toCity }}</div>
            <div class="summary-meta">出发：{{ formatDateTime(train.departTime) }}</div>
            <div class="summary-meta">到达：{{ formatDateTime(train.arriveTime) }}</div>
            <div class="summary-tip">不同席别将影响最终价格，下单前请确认座位类型。</div>
          </div>
        </el-col>
        <el-col :span="15">
          <div class="booking-panel">
            <div class="panel-title">填写购票信息</div>
            <el-form :model="orderForm" class="booking-form" label-width="72px">
              <el-form-item label="席别">
                <el-radio-group v-model="orderForm.transportSeatClass" class="seat-group">
                  <template v-if="train.trainType === 'EMU'">
                    <el-radio-button v-if="train.emuFirstClassSeatPrice != null" label="EMU_FIRST_CLASS">头等座（¥{{ train.emuFirstClassSeatPrice }}）</el-radio-button>
                    <el-radio-button v-if="train.emuFirstSeatPrice != null" label="EMU_FIRST">一等座（¥{{ train.emuFirstSeatPrice }}）</el-radio-button>
                    <el-radio-button label="EMU_SECOND">二等座（¥{{ train.emuSecondSeatPrice ?? train.price }}）</el-radio-button>
                    <el-radio-button v-if="train.emuNoSeatPrice != null" label="EMU_NO_SEAT">无座（¥{{ train.emuNoSeatPrice }}）</el-radio-button>
                  </template>
                  <template v-else>
                    <el-radio-button label="TRAIN_HARD_SEAT">硬座（¥{{ train.trainHardSeatPrice ?? train.price }}）</el-radio-button>
                    <el-radio-button v-if="train.trainSoftSleeperPrice != null" label="TRAIN_SOFT_SLEEPER">软卧（¥{{ train.trainSoftSleeperPrice }}）</el-radio-button>
                    <el-radio-button v-if="train.trainHardSleeperPrice != null" label="TRAIN_HARD_SLEEPER">硬卧（¥{{ train.trainHardSleeperPrice }}）</el-radio-button>
                  </template>
                </el-radio-group>
              </el-form-item>
              <el-row :gutter="12">
                <el-col :span="12">
                  <el-form-item label="人数">
                    <el-input-number v-model="orderForm.quantity" :min="1" class="full" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="付款方式">
                    <el-select v-model="orderForm.paymentMethod" class="full">
                      <el-option label="支付宝" value="ALIPAY" />
                      <el-option label="微信" value="WECHAT" />
                      <el-option label="云闪付" value="UNIONPAY" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="12">
                <el-col :span="12">
                  <el-form-item label="联系人">
                    <el-input v-model="orderForm.contactName" placeholder="请输入联系人姓名" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="联系电话">
                    <el-input v-model="orderForm.contactPhone" placeholder="请输入联系电话" />
                  </el-form-item>
                </el-col>
              </el-row>
              <div class="booking-tip-box">
                <div class="tip-title">温馨提示</div>
                <div class="tip-text">提交订单后可在“我的订单”中查看火车票状态，出票前请保持联系电话畅通。</div>
              </div>
              <div class="booking-tip-box accent">
                <div class="tip-title">预估金额</div>
                <div class="price-plain">¥{{ (unitPrice * orderForm.quantity).toFixed(2) }}</div>
              </div>
            </el-form>
          </div>
        </el-col>
      </el-row>
    </div>
    <template #footer>
      <div class="booking-footer">
        <el-button @click="orderDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOrder">提交订单</el-button>
      </div>
    </template>
  </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import api from '../api';

const route = useRoute();
const train = ref(null);
const orderDialogVisible = ref(false);
const orderForm = ref({ quantity: 1, transportSeatClass: 'EMU_SECOND', paymentMethod: 'ALIPAY', contactName: '', contactPhone: '' });

const load = async () => {
  const res = await api.get(`/transport/trains/${route.params.id}`);
  if (res.data.code === 0) train.value = res.data.data;
};

const firstImage = (value) => {
  if (!value) return '';
  const arr = String(value).split(',').map((s) => s.trim()).filter((s) => s);
  return arr[0] || '';
};

const formatDateTime = (value) => {
  if (!value) return '-';
  return String(value).replace('T', ' ').slice(0, 16);
};

watch(train, (t) => {
  if (!t) return;
  orderForm.value.transportSeatClass = t.trainType === 'TRAIN' ? 'TRAIN_HARD_SEAT' : 'EMU_SECOND';
});

const unitPrice = computed(() => {
  const t = train.value;
  if (!t) return 0;
  const cls = orderForm.value.transportSeatClass;
  const num = (v) => (v == null ? null : Number(v));
  if (t.trainType === 'EMU') {
    if (cls === 'EMU_FIRST_CLASS') return num(t.emuFirstClassSeatPrice) ?? 0;
    if (cls === 'EMU_FIRST') return num(t.emuFirstSeatPrice) ?? 0;
    if (cls === 'EMU_NO_SEAT') return num(t.emuNoSeatPrice) ?? 0;
    return num(t.emuSecondSeatPrice) ?? num(t.price) ?? 0;
  }
  // TRAIN
  if (cls === 'TRAIN_SOFT_SLEEPER') return num(t.trainSoftSleeperPrice) ?? 0;
  if (cls === 'TRAIN_HARD_SLEEPER') return num(t.trainHardSleeperPrice) ?? 0;
  return num(t.trainHardSeatPrice) ?? num(t.price) ?? 0;
});

const submitOrder = async () => {
  if (!orderForm.value.contactName || !orderForm.value.contactPhone) {
    ElMessage.warning('请填写联系人和电话');
    return;
  }
  const res = await api.post('/orders', {
    targetId: Number(route.params.id),
    targetType: 'TRAIN',
    quantity: orderForm.value.quantity,
    transportSeatClass: orderForm.value.transportSeatClass,
    paymentMethod: orderForm.value.paymentMethod,
    contactName: orderForm.value.contactName,
    contactPhone: orderForm.value.contactPhone
  });
  if (res.data.code === 0) {
    ElMessage.success('下单成功');
    orderDialogVisible.value = false;
  } else {
    ElMessage.error(res.data.message || '下单失败');
  }
};

onMounted(() => load());
</script>

<style scoped>
.page-wrapper {
  width: 70%;
  max-width: 1100px;
  margin: 24px auto;
}
.train-card {
  border: none;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0,0,0,0.08);
}
.hero {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 24px;
  background: linear-gradient(135deg, #f8fbff 0%, #ffffff 100%);
  border-bottom: 1px solid #edf2f7;
}
.breadcrumb {
  color: #7a8798;
  font-size: 12px;
  margin-bottom: 8px;
}
.title {
  margin: 0;
  font-size: 30px;
  color: #1a1a1a;
}
.route-line {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
  font-size: 18px;
  font-weight: 700;
  color: #1e73be;
}
.hero-tags {
  margin-top: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.hero-right {
  min-width: 220px;
  text-align: right;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-end;
}
.price-label {
  font-size: 12px;
  color: #7a8798;
}
.price {
  font-size: 34px;
  font-weight: 800;
  color: #e74c3c;
  margin: 4px 0 12px;
}
.book-btn {
  min-width: 120px;
}
.cover-image {
  width: 100%;
  height: 320px;
  object-fit: cover;
  display: block;
}
.info-grid {
  padding: 20px 24px 0;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}
.info-card {
  border: 1px solid #eef3f8;
  border-radius: 14px;
  padding: 14px;
  background: #fff;
}
.info-title {
  font-size: 12px;
  color: #7a8798;
}
.info-value {
  margin-top: 8px;
  font-size: 16px;
  font-weight: 700;
  color: #1a1a1a;
}
.tips-row {
  padding: 16px 24px 24px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}
.tips-card {
  border-radius: 14px;
  padding: 16px;
  background: linear-gradient(180deg, #fbfdff 0%, #f7fbff 100%);
  border: 1px solid #eef3f8;
}
.tips-title {
  font-weight: 700;
  color: #1e73be;
}
.tips-text {
  margin-top: 8px;
  color: #556070;
  line-height: 1.7;
  font-size: 13px;
}
.order-form {
  padding-top: 4px;
}
.seat-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.price-box {
  font-size: 20px;
  font-weight: 800;
  color: #e74c3c;
}
.booking-dialog :deep(.el-dialog) {
  border-radius: 18px;
  overflow: hidden;
}
.booking-dialog :deep(.el-dialog__body) {
  max-height: 72vh;
  overflow-y: auto;
  padding-bottom: 16px;
}
.booking-dialog :deep(.el-dialog__footer) {
  padding: 12px 20px 20px;
  border-top: 1px solid #eef3f8;
}
.booking-dialog-body {
  padding: 0 4px 4px;
}
.booking-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 18px 20px;
  border-radius: 14px;
  background: linear-gradient(135deg, #f7fbff 0%, #edf6ff 100%);
  border: 1px solid #e4eef9;
  margin-bottom: 16px;
}
.booking-kicker {
  font-size: 12px;
  color: #7a8798;
  letter-spacing: 0.6px;
  text-transform: uppercase;
}
.booking-title {
  font-size: 20px;
  font-weight: 800;
  color: #1e73be;
  margin-top: 4px;
}
.booking-subtitle {
  margin-top: 4px;
  font-size: 13px;
  color: #667085;
}
.booking-price-box {
  min-width: 160px;
  text-align: right;
}
.booking-price-label {
  font-size: 12px;
  color: #7a8798;
}
.booking-price {
  font-size: 28px;
  font-weight: 800;
  color: #d93025;
  margin-top: 4px;
}
.booking-price-muted {
  margin-top: 4px;
  color: #7a8798;
  font-size: 12px;
}
.booking-content {
  align-items: stretch;
}
.booking-summary {
  height: 100%;
  border: 1px solid #eef3f8;
  border-radius: 16px;
  background: #fff;
  padding: 14px;
  box-shadow: 0 10px 24px rgba(30, 115, 190, 0.05);
}
.booking-image {
  width: 100%;
  height: 180px;
  border-radius: 12px;
  margin-bottom: 12px;
  object-fit: cover;
}
.summary-name {
  font-size: 16px;
  font-weight: 800;
  color: #1a1a1a;
}
.summary-meta {
  margin-top: 6px;
  font-size: 13px;
  color: #667085;
  line-height: 1.6;
}
.summary-tip {
  margin-top: 14px;
  padding: 10px 12px;
  border-radius: 12px;
  background: #f7fbff;
  color: #50627a;
  font-size: 12px;
  line-height: 1.6;
}
.booking-panel {
  height: 100%;
  border: 1px solid #eef3f8;
  border-radius: 16px;
  background: #fff;
  padding: 16px;
  box-shadow: 0 10px 24px rgba(30, 115, 190, 0.06);
}
.panel-title {
  font-size: 14px;
  font-weight: 800;
  color: #1a1a1a;
  margin-bottom: 14px;
  padding-left: 10px;
  border-left: 4px solid #1e73be;
}
.booking-form :deep(.el-form-item) {
  margin-bottom: 16px;
}
.booking-tip-box {
  margin-top: 8px;
  padding: 12px 14px;
  border-radius: 14px;
  background: linear-gradient(135deg, #f7fbff 0%, #eef6ff 100%);
  border: 1px solid #e4eef9;
}
.booking-tip-box.accent {
  background: linear-gradient(135deg, #eef6ff 0%, #ffffff 100%);
}
.ticket-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.price-plain {
  font-size: 16px;
  font-weight: 800;
  color: #d93025;
}
.tip-title {
  font-size: 13px;
  font-weight: 800;
  color: #1e73be;
}
.tip-text {
  margin-top: 6px;
  color: #50627a;
  line-height: 1.6;
  font-size: 12px;
}
.booking-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 8px;
}
@media (max-width: 1100px) {
  .hero {
    flex-direction: column;
  }
  .hero-right {
    align-items: flex-start;
    text-align: left;
  }
  .info-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .tips-row {
    grid-template-columns: 1fr;
  }
  .booking-hero,
  .booking-content {
    flex-direction: column;
  }
}
</style>

