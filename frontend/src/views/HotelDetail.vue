<template>
  <div class="page-wrapper" v-if="hotel">
    <!-- 酒店详情页顶部：展示酒店名称、收藏、预订入口和核心标签 -->
    <el-card class="hero-card" :body-style="{ padding: '0' }">
      <div class="hero-top">
        <div class="hero-copy">
          <div class="kicker">酒店详情 / 预订入住</div>
          <div class="hero-title-row">
            <h2 class="hero-title">{{ hotel.name }}</h2>
            <el-tag v-if="isSpecialHotel" type="warning" effect="light">今日特价 9 折</el-tag>
            <el-tag effect="light">支持在线预订</el-tag>
          </div>
          <div class="hero-meta">{{ hotel.city?.name || '未知城市' }} · {{ hotel.address || '暂无地址' }}</div>
          <div class="tag-row">
            <el-tag type="success" effect="light">{{ favoriteCount }} 人收藏</el-tag>
            <el-tag effect="light">房型 {{ rooms.length }} 个</el-tag>
            <el-tag effect="light">支持优惠券</el-tag>
          </div>
        </div>
        <div class="hero-actions">
          <el-button type="primary" @click="toggleFavorite">
            {{ isFav ? '取消收藏' : '收藏酒店' }} ({{ favoriteCount }})
          </el-button>
          <el-button type="primary" plain @click="orderDialogVisible = true">立即预订</el-button>
          <div class="hero-note">建议提前确认入住时间与房型，节假日请尽早预订。</div>
        </div>
      </div>
      <div class="hero-media">
        <el-image
          v-if="coverImages.length"
          :src="coverImages[0]"
          :preview-src-list="coverImages"
          fit="cover"
          class="hero-image"
        />
        <div class="hero-desc-box">
          <div class="section-kicker">住宿简介</div>
          <h3 class="section-title">舒适入住，便捷出行</h3>
          <p class="desc">{{ hotel.description || '商家暂未填写详细介绍。' }}</p>
          <div v-if="priceRange" class="reserve-cta" style="margin-top:16px">
            <div>
              <div class="reserve-cta-title">价格区间</div>
              <div class="reserve-cta-text">¥{{ priceRange.min }} - ¥{{ priceRange.max }}</div>
            </div>
            <el-button type="primary" @click="orderDialogVisible = true">选择房型</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 房型信息模块：用于展示酒店房型、设施和今日价格 -->
    <el-card class="section-card rooms-card" :body-style="{ padding: '0' }">
      <div class="section-head">
        <div>
          <div class="section-kicker">房型信息</div>
          <h3 class="section-title">精选房型</h3>
        </div>
        <div class="section-sub">查看房间大小、设施和当日价格</div>
      </div>
      <div class="section-body">
        <el-table
          v-if="rooms.length"
          :data="rooms"
          size="small"
          class="rooms-table"
        >
          <el-table-column prop="roomType" label="房型" />
          <el-table-column prop="size" label="房间大小" />
          <el-table-column prop="facilities" label="设施" />
          <el-table-column prop="floor" label="楼层" width="80" />
          <el-table-column prop="todayPrice" label="今日价格" width="100" />
          <el-table-column label="图片" width="140">
            <template #default="scope">
              <el-image
                v-if="roomFirstImage(scope.row.imageUrl)"
                :src="roomFirstImage(scope.row.imageUrl)"
                :preview-src-list="roomImages(scope.row.imageUrl)"
                fit="cover"
                class="room-image"
              />
            </template>
          </el-table-column>
        </el-table>
        <div v-else class="empty-hint">暂无房型信息</div>
      </div>
    </el-card>

    <!-- 住客评价模块：用于展示评论和提交新的入住体验 -->
    <el-card class="section-card comments-card" :body-style="{ padding: '0' }">
      <div class="section-head">
        <div>
          <div class="section-kicker">住客点评</div>
          <h3 class="section-title">真实反馈</h3>
        </div>
        <div class="section-sub">分享你的入住体验，帮助更多用户参考</div>
      </div>
      <div class="section-body">
        <div v-if="comments.length" class="comment-list">
          <div v-for="c in comments" :key="c.id" class="comment-item">
            <div class="comment-top">
              <div class="comment-score">评分：{{ c.score }}</div>
            </div>
            <div class="comment-content">{{ c.content }}</div>
          </div>
        </div>
        <div v-else class="empty-hint">暂无评价</div>
        <div class="comment-editor">
          <div class="editor-title">写下你的入住体验</div>
          <el-input
            v-model="newComment"
            type="textarea"
            :rows="4"
            resize="none"
            maxlength="200"
            show-word-limit
            placeholder="写下你的入住体验和感受"
          />
          <div class="editor-footer">
            <el-rate v-model="newScore" />
            <el-button type="primary" @click="submitComment">提交评价</el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>

  <el-dialog v-model="orderDialogVisible" title="酒店下单" width="820px" class="booking-dialog">
    <div class="booking-dialog-body">
      <div class="booking-hero">
        <div>
          <div class="booking-kicker">酒店预订 / Hotel Booking</div>
          <div class="booking-title">{{ hotel?.name || '酒店预订' }}</div>
          <div class="booking-subtitle">选择房型、入住日期和优惠券，快速完成预订。</div>
        </div>
        <div class="booking-price-box">
          <div class="booking-price-label">预计总价</div>
          <div class="booking-price" v-if="bookingTotalBeforeCoupon != null">
            ¥{{ bookingTotalBeforeCoupon.toFixed(2) }}
          </div>
          <div class="booking-price-muted" v-else>请选择房型和日期</div>
        </div>
      </div>

      <el-row :gutter="16" class="booking-content">
        <el-col :span="8">
          <div class="booking-summary">
            <el-image
              v-if="coverImages.length"
              :src="coverImages[0]"
              fit="cover"
              class="booking-image"
            />
            <div class="summary-name">{{ hotel?.name }}</div>
            <div class="summary-meta">{{ hotel?.city?.name || '未知城市' }}</div>
            <div class="summary-meta">{{ priceRange ? `价格区间 ¥${priceRange.min} - ¥${priceRange.max}` : '价格信息加载中' }}</div>
            <div class="summary-tip">建议优先选择可立即入住的房型，节假日和周末房态更紧张。</div>
          </div>
        </el-col>
        <el-col :span="16">
          <div class="booking-panel">
            <div class="panel-title">填写入住信息</div>
            <el-form :model="orderForm" label-width="88px" class="booking-form">
              <el-row :gutter="12">
                <el-col :span="12">
                  <el-form-item label="房型">
                    <el-select v-model="orderForm.roomId" placeholder="选择房型" class="full">
                      <el-option
                        v-for="r in rooms"
                        :key="r.id"
                        :label="`${r.roomType}${r.todayPrice ? '（¥' + r.todayPrice + '/晚）' : ''}`"
                        :value="r.id"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="房间数">
                    <el-input-number v-model="orderForm.quantity" :min="1" class="full" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="12">
                <el-col :span="12">
                  <el-form-item label="入住日期">
                    <el-date-picker v-model="orderForm.checkIn" type="date" value-format="YYYY-MM-DD" class="full" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="离店日期">
                    <el-date-picker v-model="orderForm.checkOut" type="date" value-format="YYYY-MM-DD" class="full" />
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
              <div class="booking-tip-box" v-if="bookingTotalBeforeCoupon != null">
                <div class="tip-title">预估原价</div>
                <div class="tip-text">¥{{ bookingTotalBeforeCoupon.toFixed(2) }}</div>
              </div>
              <div class="booking-tip-box accent" v-if="estimatedTotalNumber != null && selectedCouponRate != null">
                <div class="tip-title">券后预估</div>
                <div class="tip-text">¥{{ (estimatedTotalNumber * selectedCouponRate).toFixed(2) }}</div>
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
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import api from '../api';
import { FavoriteTargetType } from '../utils';

const route = useRoute();
const router = useRouter();
const hotel = ref(null);
const favoriteCount = ref(0);
const comments = ref([]);
const isFav = ref(false);
const newComment = ref('');
const newScore = ref(5);
const rooms = ref([]);
const priceRange = ref(null);
const isSpecialHotel = ref(false);
const coverImages = ref([]);
const availableCoupons = ref([]);

const orderDialogVisible = ref(false);
const orderForm = ref({
  roomId: null,
  checkIn: '',
  checkOut: '',
  quantity: 1,
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

watch(orderDialogVisible, (visible) => {
  if (visible) {
    api.get('/coupons/my', { params: { used: false } }).then((res) => {
      if (res.data.code === 0) availableCoupons.value = res.data.data || [];
    }).catch(() => {});
  }
});

const roomImages = (value) => {
  if (!value) return [];
  return String(value).split(',').map((s) => s.trim()).filter((s) => s);
};

const roomFirstImage = (value) => {
  const list = roomImages(value);
  return list[0] || '';
};

const estimatedTotalPriceText = computed(() => {
  if (!orderForm.value.roomId || !orderForm.value.checkIn || !orderForm.value.checkOut) return '';
  const r = rooms.value.find((x) => x.id === orderForm.value.roomId);
  if (!r || !r.todayPrice) return '';
  const start = new Date(orderForm.value.checkIn);
  const end = new Date(orderForm.value.checkOut);
  const nights = Math.ceil((end - start) / (1000 * 60 * 60 * 24));
  if (!Number.isFinite(nights) || nights <= 0) return '';
  let unit = Number(r.todayPrice);
  if (isSpecialHotel.value) {
    unit = unit * 0.9;
  }
  const total = unit * Number(orderForm.value.quantity || 1) * nights;
  return isSpecialHotel.value
    ? `¥${total.toFixed(2)}（已享9折，${nights}晚）`
    : `¥${total.toFixed(2)}（${nights}晚）`;
});

const estimatedTotalNumber = computed(() => {
  if (!orderForm.value.roomId || !orderForm.value.checkIn || !orderForm.value.checkOut) return null;
  const r = rooms.value.find((x) => x.id === orderForm.value.roomId);
  if (!r || !r.todayPrice) return null;
  const start = new Date(orderForm.value.checkIn);
  const end = new Date(orderForm.value.checkOut);
  const nights = Math.ceil((end - start) / (1000 * 60 * 60 * 24));
  if (!Number.isFinite(nights) || nights <= 0) return null;
  let unit = Number(r.todayPrice);
  if (isSpecialHotel.value) unit = unit * 0.9;
  return unit * Number(orderForm.value.quantity || 1) * nights;
});

const selectedCouponRate = computed(() => {
  const id = orderForm.value.userCouponId;
  if (!id) return null;
  const c = availableCoupons.value.find((x) => x.id === id);
  return c && c.discountRate != null ? Number(c.discountRate) : null;
});

const bookingTotalBeforeCoupon = computed(() => {
  if (!orderForm.value.roomId || !orderForm.value.checkIn || !orderForm.value.checkOut) return null;
  const r = rooms.value.find((x) => x.id === orderForm.value.roomId);
  if (!r || !r.todayPrice) return null;
  const start = new Date(orderForm.value.checkIn);
  const end = new Date(orderForm.value.checkOut);
  const nights = Math.ceil((end - start) / (1000 * 60 * 60 * 24));
  if (!Number.isFinite(nights) || nights <= 0) return null;
  let unit = Number(r.todayPrice);
  if (isSpecialHotel.value) unit = unit * 0.9;
  return unit * Number(orderForm.value.quantity || 1) * nights;
});

const loadDetail = async () => {
  const res = await api.get(`/hotels/${route.params.id}`);
  if (res.data.code === 0) {
    hotel.value = res.data.data.hotel;
    favoriteCount.value = res.data.data.favoriteCount;
    comments.value = res.data.data.comments;
    // 处理多张封面图
    if (hotel.value.coverImage) {
      coverImages.value = String(hotel.value.coverImage)
        .split(',')
        .map((s) => s.trim())
        .filter((s) => s);
    } else {
      coverImages.value = [];
    }
  }
  // 先加载房型列表，再加载今日价格区间，方便前端统一展示预订信息
  const roomRes = await api.get(`/hotel-rooms/public/by-hotel/${route.params.id}`);
  if (roomRes.data.code === 0) {
    rooms.value = roomRes.data.data;
  }
  const today = new Date().toISOString().slice(0, 10);
  const priceRes = await api.get(`/hotel-room-prices/public/by-hotel/${route.params.id}`, {
    params: { date: today }
  });
  if (priceRes.data.code === 0 && priceRes.data.data) {
    priceRange.value = {
      min: priceRes.data.data.minPrice,
      max: priceRes.data.data.maxPrice
    };
    const map = priceRes.data.data.roomPrices || {};
    rooms.value = rooms.value.map((r) => ({
      ...r,
      todayPrice: map[r.id] || null
    }));
  }
  // 标记是否为今日特价酒店
  try {
    const spec = JSON.parse(localStorage.getItem('specialHotelIds') || '{}');
    const today = new Date().toISOString().slice(0, 10);
    if (spec.date === today && Array.isArray(spec.ids)) {
      isSpecialHotel.value = spec.ids.includes(Number(route.params.id));
    } else {
      isSpecialHotel.value = false;
    }
  } catch {
    isSpecialHotel.value = false;
  }

  // 记录最近浏览
  if (hotel.value) {
    const view = {
      id: hotel.value.id,
      type: 'HOTEL',
      name: hotel.value.name,
      cityName: hotel.value.city ? hotel.value.city.name : '',
      coverImage: hotel.value.coverImage
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
      params: { targetId: route.params.id, type: FavoriteTargetType.HOTEL }
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
      targetType: FavoriteTargetType.HOTEL,
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
  if (!orderForm.value.roomId) {
    ElMessage.warning('请选择房型');
    return;
  }
  if (!orderForm.value.checkIn || !orderForm.value.checkOut) {
    ElMessage.warning('请选择入住和离店日期');
    return;
  }
  if (!orderForm.value.contactName || !orderForm.value.contactPhone) {
    ElMessage.warning('请填写联系人和电话');
    return;
  }
  await requireLoginThen(async () => {
    const res = await api.post('/orders', {
      targetId: Number(route.params.id),
      targetType: FavoriteTargetType.HOTEL,
      quantity: orderForm.value.quantity,
      roomId: orderForm.value.roomId,
      checkIn: orderForm.value.checkIn,
      checkOut: orderForm.value.checkOut,
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
.page-wrapper {
  width: 100%;
}

.hero-card,
.section-card {
  border: none;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.07);
  background: #fff;
}

.section-card {
  margin-top: 16px;
}

.hero-top {
  padding: 24px;
  display: flex;
  justify-content: space-between;
  gap: 20px;
  background: linear-gradient(135deg, #f8fbff 0%, #ffffff 100%);
  border-bottom: 1px solid #eef3f8;
}

.hero-copy {
  min-width: 0;
}

.kicker,
.section-kicker {
  font-size: 12px;
  color: #7a8798;
  letter-spacing: 0.6px;
  text-transform: uppercase;
  margin-bottom: 6px;
}

.hero-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.hero-title {
  margin: 0;
  font-size: 30px;
  font-weight: 800;
  color: #1a1a1a;
}

.hero-meta {
  margin-top: 8px;
  font-size: 13px;
  color: #667085;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.hero-actions {
  min-width: 230px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-end;
  gap: 10px;
}

.hero-note {
  font-size: 12px;
  color: #7a8798;
}

.hero-media {
  padding: 0 24px 24px;
  display: grid;
  grid-template-columns: 1.3fr 1fr;
  gap: 16px;
}

.hero-image {
  width: 100%;
  height: 320px;
  border-radius: 16px;
  object-fit: cover;
}

.hero-desc-box {
  border: 1px solid #eef3f8;
  border-radius: 16px;
  padding: 18px;
  background: linear-gradient(180deg, #fbfdff 0%, #ffffff 100%);
}

.section-head {
  padding: 20px 24px 14px;
  display: flex;
  justify-content: space-between;
  align-items: end;
  gap: 12px;
  border-bottom: 1px solid #eef3f8;
  background: linear-gradient(135deg, #f8fbff 0%, #ffffff 100%);
}

.section-title {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  color: #1a1a1a;
}

.section-sub {
  color: #667085;
  font-size: 13px;
}

.section-body {
  padding: 18px 24px 24px;
}

.desc {
  margin: 10px 0 0;
  color: #3c4043;
  line-height: 1.8;
}

.rooms-table {
  margin-top: 4px;
}

.room-image {
  width: 110px;
  height: 70px;
  border-radius: 8px;
}

.empty-hint {
  font-size: 13px;
  color: #9aa0a6;
  margin-top: 4px;
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

.hotel-comments {
  margin-top: 8px;
}

.comment-list {
  margin-bottom: 8px;
}

.comment-item {
  border: 1px solid #eef3f8;
  border-radius: 14px;
  padding: 14px 16px;
  background: #fff;
  margin-bottom: 12px;
}

.comment-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.comment-score {
  font-size: 13px;
  font-weight: 800;
  color: #1e73be;
  margin-bottom: 2px;
}

.comment-content {
  margin-top: 8px;
  color: #3c4043;
  line-height: 1.7;
}

.comment-editor {
  margin-top: 14px;
  padding: 16px;
  border-radius: 16px;
  background: linear-gradient(135deg, #f8fbff 0%, #ffffff 100%);
  border: 1px solid #e4eef9;
}

.editor-title {
  font-size: 15px;
  font-weight: 800;
  color: #1a1a1a;
  margin-bottom: 12px;
}

.editor-footer {
  margin-top: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px solid #eef3f8;
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
  .hotel-header {
    flex-direction: column;
  }
  .main-grid {
    grid-template-columns: 1fr;
  }
  .hotel-gallery {
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
  .hotel-header {
    flex-direction: column;
  }
  .main-grid {
    grid-template-columns: 1fr;
  }
  .hotel-gallery {
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

