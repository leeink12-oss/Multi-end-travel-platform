<template>
  <div class="page-wrapper" v-if="restaurant">
    <!-- 餐饮详情页顶部：展示店铺信息、收藏与订座入口 -->
    <el-card class="hero-card" :body-style="{ padding: '0' }">
      <div class="hero-top">
        <div class="hero-copy">
          <div class="kicker">餐饮预订 / Restaurant Booking</div>
          <div class="hero-title-row">
            <h2 class="hero-title">{{ restaurant.name }}</h2>
            <el-tag v-if="restaurant.openHours" type="success" effect="light">营业中</el-tag>
          </div>
          <div class="hero-meta">{{ restaurant.city?.name }} · {{ restaurant.address || '暂无地址' }}</div>
          <div class="hero-meta">营业时间：{{ restaurant.openHours || '未设置' }}</div>
          <div class="tag-row">
            <el-tag effect="light">收藏 {{ favoriteCount }}</el-tag>
            <el-tag effect="light">套餐 {{ grouponPackages.length }}</el-tag>
            <el-tag effect="light">推荐菜 {{ recommendedDishes.length }}</el-tag>
          </div>
        </div>
        <div class="hero-actions">
          <el-button :type="isFav ? 'success' : 'primary'" plain @click="toggleFavorite">
            {{ isFav ? '已收藏' : '收藏店铺' }}
          </el-button>
          <el-button type="primary" @click="reserveVisible = true">立即订座</el-button>
          <div class="hero-note">建议提前 30 分钟预订，便于商家安排座位</div>
        </div>
      </div>
      <div class="hero-media">
        <el-image
          v-if="firstImage(restaurant.coverImage)"
          :src="firstImage(restaurant.coverImage)"
          class="hero-image"
          fit="cover"
        />
        <div class="hero-desc-box">
          <div class="section-title">店铺简介</div>
          <p class="desc">{{ restaurant.description || '商家暂未填写店铺介绍。' }}</p>
        </div>
      </div>
    </el-card>

    <!-- 团购套餐模块：用于展示套餐、价格和下单按钮 -->
    <el-card class="section-card" :body-style="{ padding: '0' }">
      <div class="section-head">
        <div>
          <div class="section-kicker">Groupon Deals</div>
          <h3 class="section-title">团购套餐</h3>
        </div>
        <div class="section-sub">精选套餐支持一键购买，价格更实惠</div>
      </div>
      <div class="section-body">
        <el-row :gutter="16">
          <el-col v-for="pkg in grouponPackages" :key="pkg.id" :xs="24" :sm="12" :md="8">
            <div class="deal-card">
              <el-image v-if="pkg.imageUrl" :src="pkg.imageUrl" class="deal-image" fit="cover" />
              <div class="deal-content">
                <div class="deal-title">{{ pkg.title }}</div>
                <div class="deal-desc">{{ pkg.description }}</div>
                <div class="deal-price-row">
                  <span class="group-price">¥{{ pkg.groupPrice }}</span>
                  <span class="origin-price">¥{{ pkg.originalPrice }}</span>
                </div>
                <div class="deal-foot">
                  <el-input-number v-model="packageQty[pkg.id]" :min="1" :max="Math.max(1, pkg.stock || 1)" />
                  <el-button type="primary" @click="openPackageOrder(pkg)">购买套餐</el-button>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 推荐菜模块：用于展示热门菜品和点赞数据 -->
    <el-card class="section-card" :body-style="{ padding: '0' }">
      <div class="section-head">
        <div>
          <div class="section-kicker">Recommended Dishes</div>
          <h3 class="section-title">网友推荐菜</h3>
        </div>
        <div class="section-sub">看看其他用户最喜欢的菜品</div>
      </div>
      <div class="section-body">
        <el-row :gutter="16">
          <el-col v-for="dish in recommendedDishes" :key="dish.id" :xs="24" :sm="12" :md="8">
            <div class="dish-card">
              <el-image v-if="dish.imageUrl" :src="dish.imageUrl" class="dish-image" fit="cover" />
              <div class="dish-content">
                <div class="dish-title">{{ dish.name }}</div>
                <div class="dish-desc">{{ dish.description || '暂无描述' }}</div>
                <div class="dish-foot">
                  <span class="like-pill">点赞 {{ dish.likeCount || 0 }}</span>
                  <el-button
                    size="small"
                    :type="isDishLiked(dish.id) ? 'success' : 'primary'"
                    :plain="!isDishLiked(dish.id)"
                    @click="likeDish(dish)"
                  >
                    {{ isDishLiked(dish.id) ? '已点赞' : '点赞' }}
                  </el-button>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 订座模块：填写到店时间和人数后提交预约 -->
    <el-card class="section-card" :body-style="{ padding: '0' }">
      <div class="section-head">
        <div>
          <div class="section-kicker">Reservation</div>
          <h3 class="section-title">就餐订座</h3>
        </div>
        <div class="section-sub">填写到店时间和人数，提交后等待商家确认</div>
      </div>
      <div class="section-body">
        <div class="reserve-cta">
          <div>
            <div class="reserve-cta-title">{{ restaurant.name }}</div>
            <div class="reserve-cta-text">支持订座、备注模板、包间需求与特殊说明填写</div>
          </div>
          <el-button type="primary" size="large" @click="reserveVisible = true">立即订座</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="section-card comments-card" :body-style="{ padding: '0' }">
      <div class="section-head">
        <div>
          <div class="section-kicker">Reviews</div>
          <h3 class="section-title">用户评价</h3>
        </div>
        <div class="section-sub">分享你的真实体验，帮助更多人做选择</div>
      </div>
      <div class="section-body">
        <div v-for="c in comments" :key="c.id" class="comment-item">
          <div class="comment-top">
            <div class="comment-score">评分：{{ c.score || '-' }}</div>
          </div>
          <div class="comment-content">{{ c.content }}</div>
        </div>
        <div class="comment-editor">
          <div class="editor-title">写下你的评价</div>
          <el-input v-model="newComment" type="textarea" :rows="4" placeholder="请输入评价内容" class="comment-textarea" />
          <div class="editor-footer">
            <el-rate v-model="newScore" />
            <el-button type="primary" @click="submitComment">提交评价</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="packageOrderVisible" title="团购套餐下单" width="760px" class="package-dialog">
      <div class="package-dialog-body">
        <div class="package-hero">
          <div class="package-hero-left">
            <div class="package-kicker">团购套餐 / Group Deal</div>
            <div class="package-title">{{ selectedPackage?.title || '套餐预订' }}</div>
            <div class="package-subtitle">选择数量与支付方式后即可提交订单</div>
          </div>
          <div class="package-price-box">
            <div class="package-price-label">团购价</div>
            <div class="package-price">¥{{ selectedPackage?.groupPrice || '0.00' }}</div>
            <div class="package-origin">门市价 ¥{{ selectedPackage?.originalPrice || '0.00' }}</div>
          </div>
        </div>

        <el-row :gutter="16" class="package-content">
          <el-col :span="9">
            <div class="package-summary">
              <el-image
                v-if="selectedPackage?.imageUrl"
                :src="selectedPackage.imageUrl"
                fit="cover"
                class="package-image"
              />
              <div class="summary-name">{{ selectedPackage?.title || '团购套餐' }}</div>
              <div class="summary-meta">{{ restaurant.name }}</div>
              <div class="summary-meta">库存：{{ selectedPackage?.stock ?? 0 }}</div>
              <div class="summary-tip">套餐下单后可在“我的餐饮预订”中查看订单和状态。</div>
            </div>
          </el-col>
          <el-col :span="15">
            <div class="package-panel">
              <div class="panel-title">填写购买信息</div>
              <el-form :model="packageOrderForm" label-width="86px" class="package-form">
                <el-row :gutter="12">
                  <el-col :span="12">
                    <el-form-item label="数量">
                      <el-input-number v-model="packageOrderForm.quantity" :min="1" :max="Math.max(1, selectedPackage?.stock || 1)" class="full" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="付款方式">
                      <el-select v-model="packageOrderForm.paymentMethod" class="full">
                        <el-option label="支付宝" value="ALIPAY" />
                        <el-option label="微信" value="WECHAT" />
                        <el-option label="云闪付" value="UNIONPAY" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-form-item label="备注">
                  <el-input
                    v-model="packageOrderNote"
                    type="textarea"
                    :rows="4"
                    placeholder="可填写到店时间、忌口、特殊说明等"
                    class="full"
                  />
                </el-form-item>
                <div class="package-tip-box">
                  <div class="tip-title">订单说明</div>
                  <div class="tip-text">下单后系统会生成团购订单并保留核销码，建议到店前确认餐厅营业状态。</div>
                </div>
                <div class="amount-row">
                  <span>预估金额</span>
                  <span class="price-after">¥{{ packageOrderAmount }}</span>
                </div>
              </el-form>
            </div>
          </el-col>
        </el-row>
      </div>
      <template #footer>
        <div class="footer-bar">
          <div class="footer-actions">
            <el-button @click="packageOrderVisible = false">取消</el-button>
            <el-button type="primary" @click="submitPackageOrder">立即支付</el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="reserveVisible" title="就餐订座" width="820px" class="reserve-dialog">
      <div class="reserve-dialog-body">
        <div class="reserve-hero">
          <div class="reserve-hero-left">
            <div class="reserve-kicker">餐厅订座 / Reservation</div>
            <div class="reserve-title">{{ restaurant.name }}</div>
            <div class="reserve-subtitle">填写到店时间与人数，提交后等待商家确认</div>
          </div>
          <div class="reserve-hero-right">
            <div class="reserve-badge">订座中</div>
            <div class="reserve-mini-note">建议提前 30 分钟提交</div>
          </div>
        </div>

        <el-row :gutter="16" class="reserve-content">
          <el-col :span="8">
            <div class="reserve-summary">
              <el-image
                v-if="firstImage(restaurant.coverImage)"
                :src="firstImage(restaurant.coverImage)"
                fit="cover"
                class="reserve-image"
              />
              <div class="summary-name">{{ restaurant.name }}</div>
              <div class="summary-meta">{{ restaurant.city?.name || '未知城市' }}</div>
              <div class="summary-meta">{{ restaurant.address || '暂无地址' }}</div>
              <div class="summary-meta">营业时间：{{ restaurant.openHours || '未设置' }}</div>
              <div class="summary-tip">建议提前 30 分钟提交订座信息，方便商家安排座位。</div>
            </div>
          </el-col>
          <el-col :span="16">
            <div class="reserve-panel">
              <div class="panel-title">填写订座信息</div>
              <el-form :model="reserveForm" label-width="86px" class="reserve-form">
                <el-form-item label="到店时间">
                  <el-date-picker
                    v-model="reserveForm.reserveTime"
                    type="datetime"
                    value-format="YYYY-MM-DDTHH:mm:ss"
                    placeholder="请选择到店时间"
                    class="full"
                  />
                </el-form-item>
                <el-row :gutter="12">
                  <el-col :span="12">
                    <el-form-item label="人数">
                      <el-input-number v-model="reserveForm.peopleCount" :min="1" :max="20" class="full" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="备注模板">
                      <el-select v-model="reserveQuickNote" placeholder="选择常用备注" class="full" @change="applyQuickReserveNote">
                        <el-option label="无备注" value="" />
                        <el-option label="靠窗座位" value="靠窗座位" />
                        <el-option label="安静区域" value="安静区域" />
                        <el-option label="儿童座椅" value="儿童座椅" />
                        <el-option label="生日聚餐" value="生日聚餐" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="12">
                  <el-col :span="12">
                    <el-form-item label="联系人">
                      <el-input v-model="reserveForm.contactName" placeholder="请输入联系人姓名" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="联系电话">
                      <el-input v-model="reserveForm.contactPhone" placeholder="请输入联系电话" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-form-item label="详细备注">
                  <el-input
                    v-model="reserveForm.note"
                    type="textarea"
                    :rows="4"
                    placeholder="可填写忌口、包间需求、儿童餐等说明"
                    class="full"
                  />
                </el-form-item>
                <div class="reserve-tip-box">
                  <div class="tip-title">温馨提示</div>
                  <div class="tip-text">提交后商家会尽快确认订座信息。若有包间、忌口或儿童座椅等需求，请尽量填写在备注中。</div>
                </div>
              </el-form>
            </div>
          </el-col>
        </el-row>
      </div>
      <template #footer>
        <div class="footer-bar">
          <div class="footer-actions">
            <el-button @click="reserveVisible = false">取消</el-button>
            <el-button type="primary" @click="submitReserve">提交订座</el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import api from '../api';
import { FavoriteTargetType } from '../utils';

const route = useRoute();
const router = useRouter();
const restaurant = ref(null);
const favoriteCount = ref(0);
const comments = ref([]);
const grouponPackages = ref([]);
const recommendedDishes = ref([]);
const likedDishIds = ref([]);
const packageQty = ref({});
const isFav = ref(false);
const newComment = ref('');
const newScore = ref(5);
const packageOrderVisible = ref(false);
const selectedPackage = ref(null);
const packageOrderForm = reactive({
  quantity: 1,
  paymentMethod: 'ALIPAY'
});
const packageOrderNote = ref('');

const reserveVisible = ref(false);
const reserveForm = reactive({
  reserveTime: '',
  peopleCount: 2,
  contactName: '',
  contactPhone: '',
  note: ''
});
const reserveQuickNote = ref('');

const firstImage = (value) => (value ? String(value).split(',')[0].trim() : '');

const loadDetail = async () => {
  // 一次性加载餐厅详情、收藏数、评论、团购套餐和推荐菜，减少页面多次请求
  const res = await api.get(`/restaurants/${route.params.id}`);
  if (res.data.code === 0) {
    restaurant.value = res.data.data.restaurant;
    favoriteCount.value = res.data.data.favoriteCount || 0;
    comments.value = res.data.data.comments || [];
    grouponPackages.value = res.data.data.grouponPackages || [];
    recommendedDishes.value = res.data.data.recommendedDishes || [];
    likedDishIds.value = res.data.data.likedDishIds || [];
    const map = {};
    grouponPackages.value.forEach((p) => { map[p.id] = 1; });
    packageQty.value = map;
  }
};

const toggleFavorite = async () => {
  try {
    const res = await api.post('/favorites/toggle', null, {
      params: { targetId: Number(route.params.id), type: FavoriteTargetType.RESTAURANT }
    });
    if (res.data.code === 0) {
      isFav.value = res.data.data;
      favoriteCount.value += isFav.value ? 1 : -1;
    }
  } catch {
    ElMessage.error('请先登录再收藏');
  }
};

const submitComment = async () => {
  if (!newComment.value.trim()) return ElMessage.warning('请输入评价内容');
  try {
    const res = await api.post('/comments', {
      targetId: Number(route.params.id),
      targetType: FavoriteTargetType.RESTAURANT,
      content: newComment.value,
      score: newScore.value
    });
    if (res.data.code === 0) {
      ElMessage.success('评价成功');
      newComment.value = '';
      await loadDetail();
    }
  } catch {
    ElMessage.error('请先登录再评价');
  }
};

const openPackageOrder = (pkg) => {
  selectedPackage.value = pkg;
  packageOrderForm.quantity = packageQty.value[pkg.id] || 1;
  packageOrderForm.paymentMethod = 'ALIPAY';
  packageOrderNote.value = '';
  packageOrderVisible.value = true;
};

const packageOrderAmount = computed(() => {
  if (!selectedPackage.value) return '0.00';
  return (Number(selectedPackage.value.groupPrice || 0) * Number(packageOrderForm.quantity || 1)).toFixed(2);
});

const submitPackageOrder = async () => {
  if (!selectedPackage.value) return;
  try {
    const res = await api.post(`/restaurants/groupon-packages/${selectedPackage.value.id}/order`, {
      quantity: packageOrderForm.quantity,
      paymentMethod: packageOrderForm.paymentMethod,
      note: packageOrderNote.value
    });
    if (res.data.code === 0) {
      ElMessage.success('套餐下单成功');
      packageOrderVisible.value = false;
      packageOrderNote.value = '';
      await loadDetail();
    } else {
      ElMessage.error(res.data.message || '下单失败');
    }
  } catch {
    ElMessage.error('请先登录用户账号');
  }
};

const applyQuickReserveNote = (value) => {
  reserveForm.note = value || '';
};

const submitReserve = async () => {
  if (!reserveForm.reserveTime || !reserveForm.contactName || !reserveForm.contactPhone) {
    ElMessage.warning('请填写订座信息');
    return;
  }
  try {
    const res = await api.post(`/restaurants/${route.params.id}/reserve`, {
      reserveTime: reserveForm.reserveTime,
      peopleCount: reserveForm.peopleCount,
      contactName: reserveForm.contactName,
      contactPhone: reserveForm.contactPhone,
      note: reserveForm.note
    });
    if (res.data.code === 0) {
      ElMessage.success('订座提交成功，请等待商家确认');
      reserveVisible.value = false;
      reserveForm.reserveTime = '';
      reserveForm.peopleCount = 2;
      reserveForm.contactName = '';
      reserveForm.contactPhone = '';
      reserveForm.note = '';
      reserveQuickNote.value = '';
    } else {
      ElMessage.error(res.data.message || '订座失败');
    }
  } catch {
    ElMessage.error('请先登录用户账号');
  }
};

const likeDish = async (dish) => {
  try {
    const res = await api.post(`/restaurants/recommended-dishes/${dish.id}/like`);
    if (res.data.code === 0) {
      dish.likeCount = res.data.data.likeCount;
      const liked = !!res.data.data.liked;
      if (liked) {
        if (!likedDishIds.value.includes(dish.id)) likedDishIds.value.push(dish.id);
      } else {
        likedDishIds.value = likedDishIds.value.filter((id) => id !== dish.id);
      }
      // 重新按点赞数排序，保证高赞在前
      recommendedDishes.value = [...recommendedDishes.value].sort((a, b) => (b.likeCount || 0) - (a.likeCount || 0));
    } else {
      ElMessage.error(res.data.message || '点赞失败');
    }
  } catch {
    ElMessage.error('请先登录后点赞');
  }
};

const isDishLiked = (dishId) => likedDishIds.value.includes(dishId);

onMounted(loadDetail);
</script>

<style scoped>
.page-wrapper {
  width: 70%;
  max-width: 1120px;
  margin: 24px auto 40px;
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
.deal-card,
.dish-card {
  border: 1px solid #eef3f8;
  border-radius: 16px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 8px 24px rgba(30, 115, 190, 0.05);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  margin-bottom: 16px;
}
.deal-card:hover,
.dish-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(30, 115, 190, 0.10);
}
.deal-image,
.dish-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
}
.deal-content,
.dish-content {
  padding: 14px;
}
.deal-title,
.dish-title {
  font-size: 16px;
  font-weight: 800;
  color: #1a1a1a;
}
.deal-desc,
.dish-desc {
  margin-top: 8px;
  font-size: 13px;
  color: #667085;
  line-height: 1.7;
  min-height: 44px;
}
.deal-price-row {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.group-price {
  color: #d93025;
  font-weight: 800;
  font-size: 18px;
}
.origin-price {
  color: #98a2b3;
  text-decoration: line-through;
  font-size: 12px;
}
.deal-foot,
.dish-foot {
  margin-top: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}
.like-pill {
  display: inline-flex;
  align-items: center;
  padding: 6px 10px;
  border-radius: 999px;
  background: #f7fbff;
  color: #1e73be;
  font-size: 12px;
  font-weight: 700;
}
.reserve-cta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 18px 20px;
  border-radius: 16px;
  background: linear-gradient(135deg, #f7fbff 0%, #edf6ff 100%);
  border: 1px solid #e4eef9;
}
.reserve-cta-title {
  font-size: 18px;
  font-weight: 800;
  color: #1a1a1a;
}
.reserve-cta-text {
  margin-top: 6px;
  font-size: 13px;
  color: #667085;
}
.comments-card .section-body {
  padding-bottom: 20px;
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
  font-weight: 800;
  color: #1e73be;
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
.package-dialog :deep(.el-dialog),
.reserve-dialog :deep(.el-dialog) {
  border-radius: 18px;
  overflow: hidden;
}
.package-dialog :deep(.el-dialog__body),
.reserve-dialog :deep(.el-dialog__body) {
  max-height: 72vh;
  overflow-y: auto;
  padding: 20px 20px 28px;
}
.package-dialog :deep(.el-dialog__footer),
.reserve-dialog :deep(.el-dialog__footer) {
  padding: 18px 20px 32px;
  border-top: 1px solid #eef3f8;
  position: relative;
  z-index: 2;
}
.package-dialog-body,
.reserve-dialog-body {
  padding: 0 4px 4px;
}
.package-hero,
.reserve-hero {
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
.package-kicker,
.reserve-kicker {
  font-size: 12px;
  color: #7a8798;
  letter-spacing: 0.6px;
  text-transform: uppercase;
}
.package-title,
.reserve-title {
  font-size: 20px;
  font-weight: 800;
  color: #1e73be;
  margin-top: 4px;
}
.package-subtitle,
.reserve-subtitle {
  margin-top: 4px;
  font-size: 13px;
  color: #667085;
}
.package-price-box,
.reserve-hero-right {
  min-width: 160px;
  text-align: right;
}
.package-price-label {
  font-size: 12px;
  color: #7a8798;
}
.package-price {
  font-size: 28px;
  font-weight: 800;
  color: #d93025;
  margin-top: 4px;
}
.package-origin {
  margin-top: 4px;
  color: #7a8798;
  font-size: 12px;
  text-decoration: line-through;
}
.package-content,
.reserve-content { align-items: stretch; }
.package-summary,
.reserve-summary {
  height: 100%;
  border: 1px solid #eef3f8;
  border-radius: 16px;
  background: #fff;
  padding: 14px;
  box-shadow: 0 10px 24px rgba(30, 115, 190, 0.05);
}
.package-image,
.reserve-image {
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
.package-panel,
.reserve-panel {
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
.package-form :deep(.el-form-item),
.reserve-form :deep(.el-form-item) {
  margin-bottom: 16px;
}
.package-tip-box,
.reserve-tip-box {
  margin-top: 8px;
  padding: 12px 14px;
  border-radius: 14px;
  background: linear-gradient(135deg, #f7fbff 0%, #eef6ff 100%);
  border: 1px solid #e4eef9;
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
.amount-row {
  margin-top: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px dashed #e4eef9;
  font-weight: 700;
}
.footer-bar {
  display: flex;
  justify-content: flex-end;
  padding-top: 8px;
}
.footer-actions {
  display: flex;
  gap: 10px;
  margin-top: 8px;
}
.full { width: 100%; }
@media (max-width: 1100px) {
  .page-wrapper {
    width: 92%;
  }
  .hero-top,
  .hero-media,
  .reserve-cta {
    grid-template-columns: 1fr;
    flex-direction: column;
    align-items: flex-start;
  }
  .hero-actions {
    align-items: flex-start;
    min-width: 0;
  }
  .hero-media {
    padding: 0 24px 24px;
  }
}
</style>

