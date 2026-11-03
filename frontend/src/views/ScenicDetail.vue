<template>
  <div class="scenic-detail-wrapper" v-if="spot">
    <!-- 景点详情页头部：展示景点名称、收藏和门票入口 -->
    <el-card class="scenic-detail-card">
      <div class="scenic-header">
        <div class="scenic-title-block">
          <div class="page-kicker">景点详情 / 门票预订</div>
          <h2 class="scenic-name">{{ spot.name }}</h2>
          <div class="scenic-location" v-if="spot.city">
            {{ spot.city.name }}
          </div>
          <div class="scenic-badges">
            <el-tag type="success" effect="light">{{ spot.free ? '免费景点' : '收费景点' }}</el-tag>
            <el-tag effect="light">{{ favoriteCount }} 人收藏</el-tag>
          </div>
        </div>
        <div class="scenic-header-actions">
          <el-button type="primary" @click="toggleFavorite">
            {{ isFav ? '取消收藏' : '收藏' }} ({{ favoriteCount }})
          </el-button>
          <el-button class="cta-btn" type="primary" @click="orderDialogVisible = true">
            预订门票
          </el-button>
        </div>
      </div>

      <div v-if="imageList.length" class="scenic-gallery">
        <el-image
          v-for="(img, index) in imageList"
          :key="img"
          :src="img"
          :preview-src-list="imageList"
          :initial-index="index"
          fit="cover"
          class="gallery-image"
        />
      </div>

      <!-- 景点详情核心内容：简介、票价和游玩建议 -->
      <div class="scenic-main">
        <div class="main-grid">
          <div class="main-panel">
            <h3 class="section-title">景点简介</h3>
            <p class="scenic-desc">
              {{ spot.description || '商家暂未填写详细介绍。' }}
            </p>
            <div v-if="spot.free" class="price-line free">
              <span class="price-label">免费景点</span>
              <span class="price-value">无需门票</span>
            </div>
            <div v-else class="ticket-prices">
              <div v-if="spot.childPrice != null" class="price-chip">
                <span class="chip-label">儿童票</span>
                <span class="chip-value">¥{{ spot.childPrice }}</span>
              </div>
              <div v-if="spot.adultPrice != null" class="price-chip">
                <span class="chip-label">成人票</span>
                <span class="chip-value">¥{{ spot.adultPrice }}</span>
              </div>
              <div v-if="spot.studentPrice != null" class="price-chip">
                <span class="chip-label">学生票</span>
                <span class="chip-value">¥{{ spot.studentPrice }}</span>
              </div>
              <div v-if="!spot.childPrice && !spot.adultPrice && !spot.studentPrice" class="price-line">
                <span class="price-label">参考价格</span>
                <span class="price-value">¥{{ spot.price }}</span>
              </div>
            </div>
          </div>
          <div class="side-panel">
            <div class="info-box">
              <div class="info-box-title">游玩建议</div>
              <div class="info-box-text">建议提前规划出行时间，避开节假日高峰，更容易获得更好的游览体验。</div>
            </div>
            <div class="info-box accent">
              <div class="info-box-title">预订说明</div>
              <div class="info-box-text">提交订单后可在“我的订单”中查看门票信息和使用日期。</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 游客点评模块：展示历史评论并支持用户提交评价 -->
      <div class="scenic-comments">
        <h3 class="section-title">游客点评</h3>
        <div v-if="comments.length" class="comment-list">
          <div v-for="c in comments" :key="c.id" class="comment-item">
            <div class="comment-score">评分：{{ c.score }}</div>
            <div class="comment-content">{{ c.content }}</div>
          </div>
        </div>
        <div v-else class="empty-hint">暂无评价</div>
        <div class="comment-editor">
          <div class="comment-editor-header">
            <div>
              <div class="comment-editor-title">写下你的体验</div>
              <div class="comment-editor-subtitle">分享交通、景色、服务等真实感受，帮助更多游客参考。</div>
            </div>
            <div class="score-pill">{{ newScore }} 分</div>
          </div>
          <el-input
            v-model="newComment"
            type="textarea"
            :rows="4"
            resize="none"
            maxlength="200"
            show-word-limit
            placeholder="分享你的出行体验"
            class="comment-textarea"
          />
          <div class="comment-footer">
            <div class="score-wrap">
              <span class="score-label">评分</span>
              <el-rate v-model="newScore" />
            </div>
            <el-button type="primary" class="submit-comment-btn" @click="submitComment">提交评价</el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>

  <el-dialog v-model="orderDialogVisible" title="景点下单" width="820px" class="booking-dialog scenic-booking-dialog">
    <div class="booking-dialog-body">
      <div class="booking-hero scenic-hero">
        <div>
          <div class="booking-kicker">景点门票 / Scenic Booking</div>
          <div class="booking-title">{{ spot?.name || '景点预订' }}</div>
          <div class="booking-subtitle">选择出行日期、门票类型和优惠券，快速完成下单。</div>
        </div>
        <div class="booking-price-box">
          <div class="booking-price-label">预计金额</div>
          <div class="booking-price" v-if="orderTotalBeforeCoupon != null">
            ¥{{ orderTotalBeforeCoupon.toFixed(2) }}
          </div>
          <div class="booking-price-muted" v-else>请选择日期和票种</div>
        </div>
      </div>

      <el-row :gutter="16" class="booking-content">
        <el-col :span="8">
          <div class="booking-summary">
            <el-image
              v-if="imageList.length"
              :src="imageList[0]"
              fit="cover"
              class="booking-image"
            />
            <div class="summary-name">{{ spot?.name }}</div>
            <div class="summary-meta">{{ spot?.city?.name || '未知城市' }}</div>
            <div class="summary-meta">{{ spot?.free ? '免费景点' : '收费景点' }}</div>
            <div class="summary-tip">建议提前规划出行时间，避开节假日高峰，更容易获得更好的游览体验。</div>
          </div>
        </el-col>
        <el-col :span="16">
          <div class="booking-panel">
            <div class="panel-title">填写门票信息</div>
            <el-form :model="orderForm" label-width="88px" class="booking-form">
              <el-row :gutter="12">
                <el-col :span="12">
                  <el-form-item label="出行日期">
                    <el-date-picker v-model="orderForm.useDate" type="date" value-format="YYYY-MM-DD" class="full" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="人数">
                    <el-input-number v-model="orderForm.quantity" :min="1" class="full" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="12">
                <el-col :span="12">
                  <el-form-item v-if="!spot.free" label="门票类型">
                    <el-radio-group v-model="orderForm.ticketType" class="ticket-group">
                      <el-radio-button v-if="spot.childPrice != null" label="CHILD">儿童票</el-radio-button>
                      <el-radio-button v-if="spot.adultPrice != null" label="ADULT">成人票</el-radio-button>
                      <el-radio-button v-if="spot.studentPrice != null" label="STUDENT">学生票</el-radio-button>
                    </el-radio-group>
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
                  <el-form-item label="优惠券">
                    <el-select v-model="orderForm.userCouponId" placeholder="不使用" clearable class="full">
                      <el-option
                        v-for="c in availableCoupons"
                        :key="c.id"
                        :label="`${c.couponName}（${couponDiscountLabel(c.discountRate)}）`"
                        :value="c.id"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="预估原价">
                    <div class="price-plain">¥{{ orderTotalBeforeCoupon != null ? orderTotalBeforeCoupon.toFixed(2) : '0.00' }}</div>
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
              <div v-if="selectedCouponRate != null && orderTotalBeforeCoupon != null" class="booking-tip-box">
                <div class="tip-title">券后预估</div>
                <div class="tip-text">¥{{ (orderTotalBeforeCoupon * selectedCouponRate).toFixed(2) }}</div>
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
</template>

<script setup>
import { onMounted, ref, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import api from '../api';
import { FavoriteTargetType } from '../utils';

const route = useRoute();
const router = useRouter();
const spot = ref(null);
const imageList = ref([]);
const favoriteCount = ref(0);
const comments = ref([]);
const isFav = ref(false);
const newComment = ref('');
const newScore = ref(5);
const availableCoupons = ref([]);

const orderDialogVisible = ref(false);
const orderForm = ref({
  useDate: '',
  quantity: 1,
  ticketType: '',
  userCouponId: null,
  paymentMethod: 'ALIPAY',
  contactName: '',
  contactPhone: ''
});

function couponDiscountLabel(rate) {
  if (rate == null) return '';
  const p = Math.round((1 - Number(rate)) * 100);
  return p + '% off';
}

const orderTotalBeforeCoupon = computed(() => {
  const s = spot.value;
  const q = orderForm.value.quantity || 1;
  if (!s) return null;
  if (s.free) return 0;
  const type = (orderForm.value.ticketType || '').toUpperCase();
  let unit = 0;
  if (type === 'CHILD' && s.childPrice != null) unit = s.childPrice;
  else if (type === 'ADULT' && s.adultPrice != null) unit = s.adultPrice;
  else if (type === 'STUDENT' && s.studentPrice != null) unit = s.studentPrice;
  else return null;
  return Number(unit) * q;
});

const selectedCouponRate = computed(() => {
  const id = orderForm.value.userCouponId;
  if (!id) return null;
  const c = availableCoupons.value.find((x) => x.id === id);
  return c && c.discountRate != null ? Number(c.discountRate) : null;
});

watch(orderDialogVisible, (visible) => {
  if (visible) {
    api.get('/coupons/my', { params: { used: false } }).then((res) => {
      if (res.data.code === 0) availableCoupons.value = res.data.data || [];
    }).catch(() => {});
  }
});

const loadDetail = async () => {
  const res = await api.get(`/scenics/${route.params.id}`);
  if (res.data.code === 0) {
    spot.value = res.data.data.spot;
    favoriteCount.value = res.data.data.favoriteCount;
    comments.value = res.data.data.comments;
    // 处理多张封面图
    if (spot.value.coverImage) {
      imageList.value = String(spot.value.coverImage)
        .split(',')
        .map((s) => s.trim())
        .filter((s) => s);
    } else {
      imageList.value = [];
    }
    // 记录最近浏览
    const view = {
      id: spot.value.id,
      type: 'SCENIC',
      name: spot.value.name,
      cityName: spot.value.city ? spot.value.city.name : '',
      coverImage: spot.value.coverImage
    };
    try {
      const list = JSON.parse(localStorage.getItem('recentViews') || '[]');
      const filtered = Array.isArray(list)
        ? list.filter((x) => !(x.type === view.type && x.id === view.id))
        : [];
      filtered.unshift(view);
      localStorage.setItem('recentViews', JSON.stringify(filtered.slice(0, 10)));
    } catch {
      localStorage.setItem('recentViews', JSON.stringify([view]));
    }
  }
};

const requireLoginThen = async (action) => {
  try {
    await action();
  } catch (e) {
    ElMessageBox.confirm('该操作需要先登录，是否前往登录？', '未登录', {
      confirmButtonText: '去登录',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(() => {
        router.push('/login');
      })
      .catch(() => {});
  }
};

const toggleFavorite = async () => {
  await requireLoginThen(async () => {
    const res = await api.post('/favorites/toggle', null, {
      params: { targetId: route.params.id, type: FavoriteTargetType.SCENIC }
    });
    if (res.data.code === 0) {
      isFav.value = res.data.data;
      favoriteCount.value += isFav.value ? 1 : -1;
    }
  });
};

const submitComment = async () => {
  if (!newComment.value) {
    ElMessage.warning('请输入评价内容');
    return;
  }
  await requireLoginThen(async () => {
    const res = await api.post('/comments', {
      targetId: Number(route.params.id),
      targetType: FavoriteTargetType.SCENIC,
      content: newComment.value,
      score: newScore.value
    });
    if (res.data.code === 0) {
      ElMessage.success('评价成功');
      newComment.value = '';
      await loadDetail();
    }
  });
};

const submitOrder = async () => {
  if (!orderForm.value.useDate) {
    ElMessage.warning('请选择出行日期');
    return;
  }
  if (!spot.value.free && !orderForm.value.ticketType) {
    ElMessage.warning('请选择门票类型');
    return;
  }
  if (!orderForm.value.contactName || !orderForm.value.contactPhone) {
    ElMessage.warning('请填写联系人和电话');
    return;
  }
  await requireLoginThen(async () => {
    const res = await api.post('/orders', {
      targetId: Number(route.params.id),
      targetType: FavoriteTargetType.SCENIC,
      quantity: orderForm.value.quantity,
      useDate: orderForm.value.useDate,
      ticketType: spot.value.free ? null : orderForm.value.ticketType,
      userCouponId: orderForm.value.userCouponId || undefined,
      paymentMethod: orderForm.value.paymentMethod,
      contactName: orderForm.value.contactName,
      contactPhone: orderForm.value.contactPhone
    });
    if (res.data.code === 0) {
      ElMessage.success('下单成功');
      orderDialogVisible.value = false;
    }
  });
};

onMounted(() => {
  loadDetail();
});
</script>

<style scoped>
.scenic-detail-wrapper {
  width: 70%;
  max-width: 1100px;
  margin: 24px auto 40px;
}

.scenic-detail-card {
  padding: 16px 20px 24px;
}

.scenic-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 12px;
}

.page-kicker {
  font-size: 12px;
  color: #7a8798;
  letter-spacing: 0.6px;
  text-transform: uppercase;
  margin-bottom: 6px;
}

.scenic-title-block {
  flex: 1;
}

.scenic-name {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  color: #1a1a1a;
}

.scenic-location {
  margin-top: 4px;
  font-size: 13px;
  color: #5f6368;
}

.scenic-badges {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.scenic-header-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.price-after {
  color: #e6a23c;
  font-weight: 600;
}

.cta-btn {
  min-width: 120px;
}

.scenic-gallery {
  display: flex;
  gap: 8px;
  margin: 12px 0 16px;
}

.gallery-image {
  flex: 1;
  min-width: 0;
  height: 240px;
  border-radius: 14px;
  box-shadow: 0 8px 20px rgba(30, 115, 190, 0.08);
}

.scenic-main {
  margin-top: 4px;
}

.main-grid {
  display: grid;
  grid-template-columns: 1.4fr 0.9fr;
  gap: 12px;
}

.main-panel,
.side-panel {
  border: 1px solid #eef3f8;
  border-radius: 16px;
  background: #fff;
  padding: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 800;
  margin: 0 0 10px;
  color: #1a1a1a;
}

.scenic-desc {
  margin: 0 0 12px;
  font-size: 14px;
  line-height: 1.7;
  color: #3c4043;
}

.ticket-prices {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.price-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 12px;
  background: #f7fbff;
  border: 1px solid #e4eef9;
}

.chip-label {
  color: #667085;
  font-size: 12px;
}

.chip-value {
  color: #d93025;
  font-weight: 800;
}

.price-line {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border-radius: 12px;
  background: #fff3e0;
  font-size: 13px;
  margin-top: 4px;
}

.price-label {
  color: #d08300;
  font-weight: 600;
}

.price-value {
  color: #c05a00;
}

.price-line.free {
  background: #e6f4ea;
}

.info-box {
  border-radius: 14px;
  padding: 14px;
  background: linear-gradient(135deg, #f7fbff 0%, #ffffff 100%);
  border: 1px solid #e4eef9;
}

.info-box + .info-box {
  margin-top: 12px;
}

.info-box.accent {
  background: linear-gradient(135deg, #eef6ff 0%, #ffffff 100%);
}

.info-box-title {
  font-weight: 800;
  color: #1e73be;
  margin-bottom: 6px;
}

.info-box-text {
  font-size: 13px;
  color: #50627a;
  line-height: 1.7;
}

.scenic-comments {
  margin-top: 18px;
}

.comment-list {
  margin-bottom: 8px;
}

.comment-item {
  padding: 10px 0;
  border-bottom: 1px solid #eef3f8;
}

.comment-score {
  font-size: 13px;
  font-weight: 700;
  margin-bottom: 2px;
}

.comment-content {
  font-size: 13px;
  color: #3c4043;
}

.comment-editor {
  margin-top: 8px;
  padding: 16px;
  border-radius: 16px;
  background: linear-gradient(135deg, #f8fbff 0%, #ffffff 100%);
  border: 1px solid #e4eef9;
  box-shadow: 0 10px 24px rgba(30, 115, 190, 0.05);
}
.comment-editor-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}
.comment-editor-title {
  font-size: 15px;
  font-weight: 800;
  color: #1a1a1a;
}
.comment-editor-subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: #667085;
  line-height: 1.6;
}
.comment-textarea :deep(.el-textarea__inner) {
  border-radius: 12px;
  border-color: #dbe8f5;
  min-height: 110px !important;
}
.comment-footer {
  margin-top: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px solid #eef3f8;
}
.score-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}
.score-label {
  font-size: 13px;
  color: #667085;
  font-weight: 600;
}
.score-pill {
  padding: 6px 10px;
  border-radius: 999px;
  background: #eef6ff;
  color: #1e73be;
  font-weight: 800;
  font-size: 12px;
}
.submit-comment-btn {
  min-width: 120px;
  border-radius: 10px;
}

.booking-dialog :deep(.el-dialog),
.scenic-booking-dialog :deep(.el-dialog) {
  border-radius: 18px;
  overflow: hidden;
}
.booking-dialog :deep(.el-dialog__body),
.scenic-booking-dialog :deep(.el-dialog__body) {
  max-height: 72vh;
  overflow-y: auto;
  padding-bottom: 16px;
}
.booking-dialog :deep(.el-dialog__footer),
.scenic-booking-dialog :deep(.el-dialog__footer) {
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
.full { width: 100%; }
@media (max-width: 1100px) {
  .scenic-header {
    flex-direction: column;
  }
  .main-grid {
    grid-template-columns: 1fr;
  }
  .scenic-gallery {
    flex-direction: column;
  }
  .gallery-image {
    height: 220px;
  }
  .booking-hero,
  .booking-content {
    flex-direction: column;
  }
}
</style>

