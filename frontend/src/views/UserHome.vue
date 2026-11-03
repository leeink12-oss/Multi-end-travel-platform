<template>
  <!-- 首页顶部：用于承载目的地搜索和站点主入口，是用户进入系统后的第一屏 -->
  <div class="home-section">
    <div class="home-wrapper">
      <el-card class="box-card">
        <div class="hero">
          <div class="hero-text">
            <div class="hero-title">寻找下一目的地</div>
            <div class="hero-subtitle">发现全球优惠酒店、民宿与景点</div>
          </div>
        </div>
        <div class="search-panel">
          <div class="toolbar">
            <div class="toolbar-item">
              <label class="toolbar-label">目的地</label>
              <el-select
                v-model="selectedCity"
                placeholder="选择城市"
                class="full-width"
                @change="onCityChange"
              >
                <el-option
                  v-for="c in cities"
                  :key="c.id"
                  :label="c.name"
                  :value="c.id"
                />
              </el-select>
            </div>
            <div class="toolbar-item">
              <label class="toolbar-label">搜索</label>
              <el-input
                v-model="keyword"
                placeholder="搜索景点 / 酒店"
                class="full-width search-input"
                @keyup.enter="searchAll"
              >
                <template #append>
                  <el-button type="primary" @click="searchAll">搜索</el-button>
                </template>
              </el-input>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>

  <!-- 下方内容区域：背景为白色，只保留中间 70% 内容 -->
  <div class="home-wrapper">
    <div class="why-wrapper">
      <div class="why-title">为什么选择哪都去？</div>
      <el-row :gutter="16" class="why-cards">
        <el-col :span="6">
          <el-card class="why-card">
            <div class="why-card-title">现在预订，到店付款</div>
            <div class="why-card-sub">多数客房可免费取消</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="why-card">
            <div class="why-card-title">机票酒店餐饮，吃住行一应俱全</div>
            <div class="why-card-sub">解决多平台来回切换的烦恼</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="why-card">
            <div class="why-card-title">超过200万商家入驻范围遍布全球</div>
            <div class="why-card-sub">酒店、旅馆、公寓及更多……</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="why-card">
            <div class="why-card-title">7x24小时值得信赖的客服</div>
            <div class="why-card-sub">随时为你提供帮助</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div v-if="recommendations.restaurants.length || recommendations.hotels.length || recommendations.scenics.length" class="section">
      <h3>猜你喜欢</h3>
      <el-tabs>
        <el-tab-pane label="餐饮" name="restaurant">
          <el-row :gutter="12" style="margin-top: 10px">
            <el-col v-for="r in recommendations.restaurants" :key="'rec-r-' + r.id" :span="6">
              <el-card class="item-card" @click="openRestaurant(r)">
                <el-image
                  v-if="firstImage(r.coverImage)"
                  :src="firstImage(r.coverImage)"
                  :preview-src-list="[firstImage(r.coverImage)]"
                  fit="cover"
                  class="thumb"
                />
                <div class="title">{{ r.name }}</div>
                <div class="meta">地址：{{ r.address || '暂无' }}</div>
                <div class="price">人均：{{ r.avgPrice || 0 }}</div>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>
        <el-tab-pane label="景点" name="scenic">
          <el-row :gutter="12" style="margin-top: 10px">
            <el-col v-for="s in recommendations.scenics" :key="'rec-s-' + s.id" :span="6">
              <el-card class="item-card" @click="$router.push(`/scenic/${s.id}`)">
                <el-image
                  v-if="firstImage(s.coverImage)"
                  :src="firstImage(s.coverImage)"
                  :preview-src-list="[firstImage(s.coverImage)]"
                  fit="cover"
                  class="thumb"
                />
                <div class="title">{{ s.name }}</div>
                <div class="meta">所在地：{{ (s.city && s.city.name) || '未知' }}</div>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>
        <el-tab-pane label="酒店" name="hotel">
          <el-row :gutter="12" style="margin-top: 10px">
            <el-col v-for="h in recommendations.hotels" :key="'rec-h-' + h.id" :span="6">
              <el-card class="item-card" @click="$router.push(`/hotel/${h.id}`)">
                <el-image
                  v-if="firstImage(h.coverImage)"
                  :src="firstImage(h.coverImage)"
                  :preview-src-list="[firstImage(h.coverImage)]"
                  fit="cover"
                  class="thumb"
                />
                <div class="title">{{ h.name }}</div>
                <div class="meta">所在地：{{ (h.city && h.city.name) || '未知' }}</div>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>
      </el-tabs>
    </div>

    <div v-if="recentViews.length" class="section">
      <h3>最近浏览</h3>
      <el-row :gutter="12">
        <el-col
          v-for="item in recentViews"
          :key="item.type + '-' + item.id"
          :span="8"
        >
          <div
            class="recent-item"
            @click="openRecentItem(item)"
          >
            <div class="recent-image">
              <el-image
                v-if="firstImage(item.coverImage)"
                :src="firstImage(item.coverImage)"
                :preview-src-list="[firstImage(item.coverImage)]"
                fit="cover"
              />
            </div>
            <div class="recent-info">
              <div class="recent-name">{{ item.name }}</div>
              <div class="recent-meta">
                {{ recentTypeLabel(item.type) }} ·
                {{ item.cityName || '未知城市' }}
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="section">
      <h3>餐饮推荐</h3>
      <el-row :gutter="12" style="margin-top: 10px">
        <el-col v-for="r in restaurants" :key="r.id" :span="6">
          <el-card class="item-card" @click="openRestaurant(r)">
            <el-image
              v-if="firstImage(r.coverImage)"
              :src="firstImage(r.coverImage)"
              :preview-src-list="[firstImage(r.coverImage)]"
              fit="cover"
              class="thumb"
            />
            <div class="title">{{ r.name }}</div>
            <div class="meta">地址：{{ r.address || '暂无' }}</div>
            <div class="price">人均：{{ r.avgPrice || 0 }}</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="section">
      <div class="section-head">
        <h3>出行预定</h3>
        <el-button link type="primary" @click="$router.push('/transport')">查看全部</el-button>
      </div>
      <TransportBookingPanel style="margin-top: 10px" />
    </div>

    <div v-if="specialHotels.length" class="section">
      <h3>特价酒店 · 今日9折</h3>
      <el-row :gutter="12">
        <el-col
          v-for="h in specialHotels"
          :key="h.id"
          :span="8"
        >
          <el-card class="item-card" @click="$router.push(`/hotel/${h.id}`)">
            <el-image
              v-if="firstImage(h.coverImage)"
              :src="firstImage(h.coverImage)"
              :preview-src-list="[firstImage(h.coverImage)]"
              fit="cover"
              class="thumb"
            />
            <div class="title">{{ h.name }}</div>
            <div class="meta">
              所在地：{{ (h.city && h.city.name) || '未知' }}
            </div>
            <div
              v-if="specialHotelDiscountRanges[h.id]"
              class="price"
            >
              9折价区间：{{ specialHotelDiscountRanges[h.id].min }} -
              {{ specialHotelDiscountRanges[h.id].max }}
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <h3>景点</h3>
        <el-row :gutter="12">
          <el-col
            v-for="s in scenics"
            :key="s.id"
            :span="12"
          >
            <el-card class="item-card" @click="$router.push(`/scenic/${s.id}`)">
              <el-image
                v-if="firstImage(s.coverImage)"
                :src="firstImage(s.coverImage)"
                :preview-src-list="[firstImage(s.coverImage)]"
                fit="cover"
                class="thumb"
              />
              <div class="title">{{ s.name }}</div>
              <div class="meta">
                所在地：{{ (s.city && s.city.name) || '未知' }}
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-col>
      <el-col :span="12">
        <h3>酒店</h3>
        <el-row :gutter="12">
          <el-col
            v-for="h in hotels"
            :key="h.id"
            :span="12"
          >
            <el-card class="item-card" @click="$router.push(`/hotel/${h.id}`)">
              <el-image
                v-if="firstImage(h.coverImage)"
                :src="firstImage(h.coverImage)"
                :preview-src-list="[firstImage(h.coverImage)]"
                fit="cover"
                class="thumb"
              />
              <div class="title">{{ h.name }}</div>
              <div class="meta">
                所在地：{{ (h.city && h.city.name) || '未知' }}
              </div>
              <div
                v-if="hotelPriceRanges[h.id]"
                class="price"
              >
                价格区间：{{ hotelPriceRanges[h.id].min }} -
                {{ hotelPriceRanges[h.id].max }}
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-col>
    </el-row>
  </div>

  <!-- 右下角跟随滚动的 AI 旅行助手 -->
  <div class="ai-float">
    <div v-if="!aiOpen" class="ai-toggle" @click="aiOpen = true">
      智能行程助手
    </div>
    <el-card v-else class="ai-panel">
      <div class="ai-header">
        <span>智能行程助手</span>
        <el-button link type="primary" size="small" @click="aiOpen = false">
          关闭
        </el-button>
      </div>
      <div class="ai-chat-window" ref="aiChatRef">
        <div
          v-for="(m, idx) in aiMessages"
          :key="idx"
          class="ai-msg-row"
          :class="m.role === 'user' ? 'from-user' : 'from-bot'"
        >
          <div class="ai-msg-bubble">
            <div class="ai-msg-role">
              {{ m.role === 'user' ? '我' : '助手' }}
            </div>
            <div class="ai-msg-text">
              {{ m.content }}
            </div>
          </div>
        </div>
      </div>
      <div class="ai-input-area">
        <el-input
          v-model="aiQuestion"
          type="textarea"
          :rows="2"
          placeholder="例如：帮我推荐上海两晚三天的酒店和景点行程，只用你们平台上的资源。"
          @keyup.enter.exact.prevent="sendAiQuestion"
        />
        <div class="ai-actions">
          <el-button
            size="small"
            :loading="aiLoading"
            type="primary"
            @click="sendAiQuestion"
          >
            发送
          </el-button>
          <el-button size="small" @click="aiOpen = false">关闭助手</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import api from '../api';
import TransportBookingPanel from '../components/TransportBookingPanel.vue';

const router = useRouter();
const cities = ref([]);
const selectedCity = ref(null);
const scenics = ref([]);
const hotels = ref([]);
const hotelPriceRanges = ref({}); // { [hotelId]: { min, max } }
const keyword = ref('');
const searching = ref(false);
const recentViews = ref([]);
const recommendations = ref({ scenics: [], hotels: [], restaurants: [] });
const specialHotels = ref([]);
const specialHotelDiscountRanges = ref({}); // { [hotelId]: { min, max } }
const restaurants = ref([]);
const quickTags = ['热门酒店', '热门景点', '周末出行', '高评分餐饮'];

// AI 浮窗状态
const aiOpen = ref(false);
const aiQuestion = ref('');
const aiLoading = ref(false);
const aiMessages = ref([]);
const aiChatRef = ref(null);

const firstImage = (value) => {
  if (!value) return '';
  const arr = String(value).split(',').map((s) => s.trim()).filter((s) => s);
  return arr[0] || '';
};

const recentTypeLabel = (type) => {
  switch (type) {
    case 'HOTEL': return '酒店';
    case 'SCENIC': return '景点';
    case 'RESTAURANT': return '餐饮';
    case 'FLIGHT': return '航班';
    case 'TRAIN': return '火车';
    default: return '资源';
  }
};

const addRecentView = (item) => {
  try {
    const key = 'recentViews';
    const data = JSON.parse(localStorage.getItem(key) || '[]');
    const filtered = Array.isArray(data)
      ? data.filter((x) => !(x.type === item.type && x.id === item.id))
      : [];
    filtered.unshift(item);
    localStorage.setItem(key, JSON.stringify(filtered.slice(0, 3)));
    loadRecentViews();
  } catch {
    // ignore
  }
};

const openRestaurant = (restaurant) => {
  if (!restaurant || !restaurant.id) return;
  addRecentView({
    id: restaurant.id,
    type: 'RESTAURANT',
    name: restaurant.name,
    cityName: restaurant.city?.name || restaurant.cityName || '',
    coverImage: restaurant.coverImage
  });
  router.push(`/dining/${restaurant.id}`);
};

const openRecentItem = (item) => {
  if (!item || !item.id) return;
  const routeMap = {
    RESTAURANT: `/dining/${item.id}`,
    HOTEL: `/hotel/${item.id}`,
    SCENIC: `/scenic/${item.id}`,
    FLIGHT: `/flight/${item.id}`,
    TRAIN: `/train/${item.id}`
  };
  if (routeMap[item.type]) {
    router.push(routeMap[item.type]);
  }
};

const loadCities = async () => {
  const res = await api.get('/cities');
  if (res.data.code === 0) {
    cities.value = res.data.data;
    await loadRecommend();
  }
};

const loadRecommend = async () => {
  const params = selectedCity.value ? { params: { cityId: selectedCity.value } } : {};
  const [sRes, hRes] = await Promise.all([
    api.get('/scenics/recommend', params),
    api.get('/hotels/recommend', params)
  ]);
  if (sRes.data.code === 0) {
    scenics.value = sRes.data.data;
  }
  if (hRes.data.code === 0) {
    hotels.value = hRes.data.data;
    await loadHotelPriceRanges();
    await loadSpecialHotels();
  }
  const rRes = await api.get('/restaurants/recommend', { params: { limit: 8 } });
  if (rRes.data.code === 0) {
    restaurants.value = rRes.data.data || [];
  }
  try {
    const recRes = await api.get('/recommendations/home');
    if (recRes.data.code === 0 && recRes.data.data) {
      recommendations.value = recRes.data.data;
    }
  } catch {
    recommendations.value = {
      scenics: scenics.value,
      hotels: hotels.value,
      restaurants: restaurants.value
    };
  }
};

const onCityChange = async () => {
  await loadRecommend();
};


const applyQuickTag = async (tag) => {
  keyword.value = tag;
  await searchAll();
};

const searchAll = async () => {
  searching.value = true;
  try {
    // 关键字为空时，回到推荐列表
    if (!keyword.value) {
      await loadRecommend();
      return;
    }

    const [sRes, hRes, rRes] = await Promise.all([
      api.get('/scenics/search', { params: { keyword: keyword.value } }),
      api.get('/hotels/search', { params: { keyword: keyword.value } }),
      api.get('/restaurants/search', { params: { keyword: keyword.value } })
    ]);

    if (sRes.data.code === 0) {
      scenics.value = sRes.data.data || [];
    }

    if (hRes.data.code === 0) {
      hotels.value = hRes.data.data || [];
      await loadHotelPriceRanges();
      await loadSpecialHotels();
    }

    if (rRes.data.code === 0) {
      restaurants.value = rRes.data.data || [];
    }
  } finally {
    searching.value = false;
  }
};

// 为当前酒店列表加载价格区间信息
const loadHotelPriceRanges = async () => {
  const today = new Date().toISOString().slice(0, 10);
  const map = {};
  await Promise.all(
    hotels.value.map(async (h) => {
      try {
        const res = await api.get(`/hotel-room-prices/public/by-hotel/${h.id}`, {
          params: { date: today }
        });
        if (res.data.code === 0 && res.data.data) {
          map[h.id] = {
            min: res.data.data.minPrice,
            max: res.data.data.maxPrice
          };
        }
      } catch {
        // 忽略单个酒店价格错误，保持其他正常
      }
    })
  );
  hotelPriceRanges.value = map;
};

// 最近浏览（从 localStorage 读取最近 3 条）
const loadRecentViews = () => {
  try {
    const data = JSON.parse(localStorage.getItem('recentViews') || '[]');
    recentViews.value = Array.isArray(data) ? data.slice(0, 3) : [];
  } catch {
    recentViews.value = [];
  }
};

// 特价酒店：在当前酒店列表中按城市分组，每个城市最多选一家，并按 9 折计算展示价
const loadSpecialHotels = async () => {
  if (!hotels.value.length) {
    specialHotels.value = [];
    specialHotelDiscountRanges.value = {};
    return;
  }
  const today = new Date().toISOString().slice(0, 10);
  // 按城市分组，每个城市随机挑选 1 家
  const groups = {};
  hotels.value.forEach((h) => {
    const cityId = h.city && h.city.id ? h.city.id : 'unknown';
    if (!groups[cityId]) {
      groups[cityId] = [];
    }
    groups[cityId].push(h);
  });
  const picked = [];
  Object.values(groups).forEach((list) => {
    if (list.length === 0) return;
    const shuffled = [...list].sort(() => Math.random() - 0.5);
    picked.push(shuffled[0]);
  });
  const discountMap = {};
  await Promise.all(
    picked.map(async (h) => {
      try {
        const res = await api.get(`/hotel-room-prices/public/by-hotel/${h.id}`, {
          params: { date: today }
        });
        if (res.data.code === 0 && res.data.data) {
          const min = res.data.data.minPrice;
          const max = res.data.data.maxPrice;
          if (min != null && max != null) {
            // 前端按 9 折展示
            const toNumber = (v) => Number(v);
            const dMin = (toNumber(min) * 0.9).toFixed(2);
            const dMax = (toNumber(max) * 0.9).toFixed(2);
            discountMap[h.id] = { min: dMin, max: dMax };
          }
        }
      } catch {
        // 单个酒店失败忽略
      }
    })
  );
  specialHotels.value = picked;
  specialHotelDiscountRanges.value = discountMap;
  // 记录今日特价酒店，用于详情页判断
  const ids = picked.map((h) => h.id);
  localStorage.setItem('specialHotelIds', JSON.stringify({ date: today, ids }));
};

const scrollAiToBottom = async () => {
  await nextTick();
  if (aiChatRef.value) {
    aiChatRef.value.scrollTop = aiChatRef.value.scrollHeight;
  }
};

// 发送问题给平台客服 AI，只推荐系统内酒店/景点的逻辑由后端控制
const sendAiQuestion = async () => {
  if (!aiQuestion.value.trim()) {
    return;
  }
  const content = aiQuestion.value.trim();
  aiMessages.value.push({ role: 'user', content });
  aiQuestion.value = '';
  aiLoading.value = true;
  try {
    const res = await api.post('/support/ask', {
      question: content
    });
    if (res.data.code === 0) {
      aiMessages.value.push({
        role: 'assistant',
        content: res.data.data || '（暂时没有合适的推荐，请稍后再试）'
      });
    } else {
      aiMessages.value.push({
        role: 'assistant',
        content: res.data.message || '助手暂时不可用，请稍后再试。'
      });
    }
  } catch (e) {
    aiMessages.value.push({
      role: 'assistant',
      content: '调用智能助手失败，请检查网络或稍后再试。'
    });
  } finally {
    aiLoading.value = false;
    scrollAiToBottom();
  }
};

onMounted(() => {
  loadCities();
  loadRecentViews();
});
</script>

<style scoped>
/* ========== 基础布局 ========== */
.home-wrapper {
  width: 70%;
  max-width: 1300px;
  margin: 0 auto 40px;
}
.home-section {
  width: 100%;
  background-color: #1e73be;
  padding-bottom: 24px;
  position: relative;
}

/* ========== Hero 区域 ========== */
.toolbar {
  display: flex;
  gap: 12px;
}
.box-card {
  border: none !important;
  background: transparent;
  padding: 24px 28px 40px;
  color: #ffffff;
  border-radius: 0 0 var(--brand-radius) var(--brand-radius);
  margin-top: 0;
  box-shadow: none !important;
  margin-top: -1px;
}
.hero {
  margin: 4px auto 12px;
  text-align: center;
  position: relative;
}
.hero-title {
  font-size: 38px;
  font-weight: 800;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
  text-shadow: 0 2px 20px rgba(0,0,0,0.15);
  animation: fadeInUp 0.6s ease-out;
}
.hero-subtitle {
  font-size: 16px;
  opacity: 0.95;
  font-weight: 500;
  animation: fadeInUp 0.6s ease-out 0.1s both;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.toolbar-item {
  flex: 1;
}
.full-width {
  width: 100%;
}

/* ========== 搜索面板 ========== */
.search-panel {
  margin-top: 12px;
  background: #ffffff;
  border-radius: 16px;
  padding: 14px 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  animation: fadeInUp 0.6s ease-out 0.2s both;
}
.search-panel:focus-within {
  box-shadow: 0 12px 40px rgba(30, 115, 190, 0.25);
}
.toolbar-label {
  display: block;
  font-size: 12px;
  color: #ffffff;
  margin-bottom: 4px;
  font-weight: 600;
  transition: color 0.3s ease;
}
.search-panel:focus-within .toolbar-label {
  color: #ffffff;
}

/* 搜索输入框样式 */
::deep(.search-panel .el-input__wrapper) {
  border-radius: 10px;
  transition: all 0.3s ease;
  box-shadow: 0 0 0 1px #e0e0e0;
}
::deep(.search-panel .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #1e73be;
}
::deep(.search-panel .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(30, 115, 190, 0.3), 0 2px 8px rgba(30, 115, 190, 0.15);
}
::deep(.search-panel .el-input__inner),
::deep(.search-panel .el-select .el-input__inner) {
  color: #ffffff;
}

::deep(.search-panel .el-input__inner::placeholder),
::deep(.search-panel .el-select .el-input__inner::placeholder) {
  color: #ffffff;
  opacity: 0.7;
}

::deep(.search-panel .el-input__inner::placeholder),
::deep(.search-panel .el-select .el-input__inner::placeholder) {
  color: #ffffff;
  opacity: 0.7;
}

/* 下拉箭头白色 */
::deep(.search-panel .el-select .el-input__suffix) {
  color: #ffffff;
}
::deep(.search-panel .el-select .el-input__suffix .el-select__caret) {
  color: #ffffff;
}
::deep(.search-panel .el-select:hover .el-input__suffix .el-select__caret) {
  color: #1e73be;
}

/* 搜索按钮样式 */
::deep(.search-panel .el-input-group__append) {
  background: #ffffff;
  border: none;
  border-radius: 10px;
  color: #1e73be;
  font-weight: 600;
  transition: all 0.3s ease;
}
::deep(.search-panel .el-input-group__append:hover) {
  background: #f0f0f0;
}
::deep(.search-panel .el-input-group__append .el-button) {
  color: #1e73be;
  background: #ffffff;
  border: none;
}

/* ========== 功能卡片样式 ========== */
.item-card {
  margin-top: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: none !important;
  border-radius: 16px !important;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.item-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(30, 115, 190, 0.2);
}
.thumb {
  width: 100%;
  height: 190px;
  border-radius: 12px 12px 0 0;
  margin-bottom: 8px;
  transition: transform 0.3s ease;
}
.item-card:hover .thumb {
  transform: scale(1.03);
}

/* ========== 标题与描述 ========== */
.title {
  font-weight: 600;
  margin-bottom: 4px;
  font-size: 15px;
  color: #1a1a1a;
}
.meta {
  font-size: 12px;
  color: #888;
  line-height: 1.5;
}
.price {
  margin-top: 6px;
  font-size: 13px;
  color: #e74c3c;
  font-weight: 600;
}

/* ========== 为什么选择板块 ========== */
.why-wrapper {
  margin-top: 24px;
  padding: 28px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.04);
}
.why-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 20px;
  text-align: center;
  color: #1a1a1a;
  position: relative;
}
.why-title::after {
  content: '';
  display: block;
  width: 60px;
  height: 3px;
  background: linear-gradient(90deg, #1e73be, #2a9d8f);
  margin: 10px auto 0;
  border-radius: 2px;
}
.why-cards {
  margin-top: 8px;
}
.why-cards .why-card {
  text-align: center;
  border: none !important;
  border-radius: 16px !important;
  padding: 16px 12px;
  transition: all 0.3s ease;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
.why-cards .why-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(30, 115, 190, 0.15);
}
.why-card-title {
  font-size: 14px;
  font-weight: 700;
  margin-bottom: 6px;
  color: #1e73be;
  line-height: 1.4;
}
.why-card-sub {
  font-size: 12px;
  color: #666;
  line-height: 1.5;
}

/* ========== 区块通用样式 ========== */
.section {
  margin-top: 32px;
  position: relative;
}
.section h3 {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 16px;
  color: #1a1a1a;
  position: relative;
  display: inline-block;
}
.section h3::before {
  content: '';
  position: absolute;
  left: -12px;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 20px;
  background: linear-gradient(180deg, #1e73be, #2a9d8f);
  border-radius: 2px;
}

/* ========== 最近浏览 ========== */
.recent-item {
  display: flex;
  border: none;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  height: 90px;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  transition: all 0.3s ease;
}
.recent-item:hover {
  transform: translateX(4px);
  box-shadow: 0 8px 24px rgba(30, 115, 190, 0.15);
}
.recent-image {
  flex: 1;
  min-width: 90px;
  max-width: 90px;
}
.recent-image .el-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.recent-info {
  flex: 1;
  padding: 10px 14px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.recent-name {
  font-weight: 600;
  margin-bottom: 4px;
  font-size: 14px;
  color: #1a1a1a;
}
.recent-meta {
  font-size: 12px;
  color: #888;
}

/* ========== 右下角浮动 AI 助手 ========== */
.ai-float {
  position: fixed;
  right: 32px;
  bottom: 40px;
  z-index: 999;
}
.ai-toggle {
  background: linear-gradient(135deg, #1e73be, #2a9d8f);
  color: #fff;
  padding: 12px 18px;
  border-radius: 999px;
  box-shadow: 0 4px 20px rgba(30, 115, 190, 0.4);
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
}
.ai-toggle:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 28px rgba(30, 115, 190, 0.5);
}
.ai-panel {
  width: 340px;
  max-height: 500px;
  display: flex;
  flex-direction: column;
  border-radius: 16px !important;
  overflow: hidden;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.15) !important;
}
.ai-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 700;
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
  color: #1e73be;
}
.ai-close {
  cursor: pointer;
}
.ai-chat-window {
  border: 1px solid #eee;
  border-radius: 12px;
  padding: 10px;
  height: 280px;
  overflow-y: auto;
  background: linear-gradient(180deg, #f8f9fa 0%, #ffffff 100%);
  margin-bottom: 8px;
}
.ai-msg-row {
  display: flex;
  margin-bottom: 10px;
  animation: fadeInUp 0.3s ease-out;
}
.ai-msg-row.from-user {
  justify-content: flex-end;
}
.ai-msg-row.from-bot {
  justify-content: flex-start;
}
.ai-msg-bubble {
  max-width: 85%;
  padding: 10px 12px;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  font-size: 13px;
  line-height: 1.5;
}
.from-user .ai-msg-bubble {
  background: linear-gradient(135deg, #1e73be, #2a9d8f);
  color: #fff;
  border-bottom-right-radius: 4px;
}
.from-bot .ai-msg-bubble {
  border-bottom-left-radius: 4px;
}
.ai-msg-role {
  font-size: 11px;
  opacity: 0.7;
  margin-bottom: 4px;
}
.from-user .ai-msg-role {
  opacity: 0.9;
}
.ai-msg-text {
  white-space: pre-wrap;
  word-break: break-word;
}
.ai-input-area {
  margin-top: 4px;
}
.ai-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
}

/* ========== 滚动条美化 ========== */
.ai-chat-window::-webkit-scrollbar {
  width: 6px;
}
.ai-chat-window::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}
.ai-chat-window::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}
.ai-chat-window::-webkit-scrollbar-thumb:hover {
  background: #aaa;
}
</style>

