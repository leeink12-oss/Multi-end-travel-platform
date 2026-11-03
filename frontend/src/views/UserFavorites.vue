<template>
  <div class="page-shell">
    <el-card class="page-card">
      <div class="page-header">
        <div>
          <div class="page-kicker">我的收藏 / Favorites</div>
          <h2 class="page-title">我的收藏</h2>
          <div class="page-subtitle">把喜欢的景点、酒店和餐饮店铺收藏起来，方便随时查看与预订。</div>
        </div>
      </div>

      <el-tabs v-model="activeTab" class="fav-tabs">
        <el-tab-pane label="景点" name="SCENIC">
          <div v-if="scenicFavorites.length" class="fav-grid">
            <div v-for="f in scenicFavorites" :key="f.id" class="fav-card">
              <el-image v-if="firstCoverImage(f.coverImage)" :src="firstCoverImage(f.coverImage)" fit="cover" class="fav-cover" />
              <div v-else class="fav-cover placeholder">暂无图片</div>
              <div class="fav-body">
                <div class="fav-name">{{ f.name || '景点#' + f.targetId }}</div>
                <div class="fav-meta" v-if="f.cityName">{{ f.cityName }}</div>
                <div class="fav-desc" v-if="f.description">{{ f.description }}</div>
                <div class="fav-footer">
                  <div class="fav-price" v-if="f.price != null">参考价 ¥{{ f.price }}</div>
                  <el-button size="small" type="primary" round @click="$router.push(`/scenic/${f.targetId}`)">去下单</el-button>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无收藏的景点" />
        </el-tab-pane>
        <el-tab-pane label="酒店" name="HOTEL">
          <div v-if="hotelFavorites.length" class="fav-grid">
            <div v-for="f in hotelFavorites" :key="f.id" class="fav-card">
              <el-image v-if="firstCoverImage(f.coverImage)" :src="firstCoverImage(f.coverImage)" fit="cover" class="fav-cover" />
              <div v-else class="fav-cover placeholder">暂无图片</div>
              <div class="fav-body">
                <div class="fav-name">{{ f.name || '酒店#' + f.targetId }}</div>
                <div class="fav-meta" v-if="f.cityName">{{ f.cityName }}</div>
                <div class="fav-desc" v-if="f.description">{{ f.description }}</div>
                <div class="fav-footer">
                  <div class="fav-price" v-if="f.price != null">参考价 ¥{{ f.price }}</div>
                  <el-button size="small" type="primary" round @click="$router.push(`/hotel/${f.targetId}`)">去下单</el-button>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无收藏的酒店" />
        </el-tab-pane>
        <el-tab-pane label="餐饮" name="RESTAURANT">
          <div v-if="restaurantFavorites.length" class="fav-grid">
            <div v-for="f in restaurantFavorites" :key="f.id" class="fav-card">
              <el-image v-if="firstCoverImage(f.coverImage)" :src="firstCoverImage(f.coverImage)" fit="cover" class="fav-cover" />
              <div v-else class="fav-cover placeholder">暂无图片</div>
              <div class="fav-body">
                <div class="fav-name">{{ f.name || '餐饮店铺#' + f.targetId }}</div>
                <div class="fav-meta" v-if="f.cityName">{{ f.cityName }}</div>
                <div class="fav-desc" v-if="f.description">{{ f.description }}</div>
                <div class="fav-footer">
                  <div class="fav-price" v-if="f.price != null">人均 ¥{{ f.price }}</div>
                  <el-button size="small" type="primary" round @click="$router.push(`/dining/${f.targetId}`)">去预订</el-button>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无收藏的餐饮店铺" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import api from '../api';
import { FavoriteTargetType } from '../utils';

const activeTab = ref(FavoriteTargetType.SCENIC);
const scenicFavorites = ref([]);
const hotelFavorites = ref([]);
const restaurantFavorites = ref([]);

/** 封面可能为多图逗号分隔，取第一张用于展示 */
function firstCoverImage(coverImage) {
  if (!coverImage || typeof coverImage !== 'string') return '';
  const first = coverImage.split(',')[0].trim();
  return first || '';
}

const loadFavorites = async () => {
  try {
    const sRes = await api.get('/favorites/my-with-detail', { params: { type: FavoriteTargetType.SCENIC } });
    scenicFavorites.value = (sRes.data.code === 0 ? (sRes.data.data || []) : []).filter((x) => x.targetType === FavoriteTargetType.SCENIC);
  } catch {
    scenicFavorites.value = [];
  }
  try {
    const hRes = await api.get('/favorites/my-with-detail', { params: { type: FavoriteTargetType.HOTEL } });
    hotelFavorites.value = (hRes.data.code === 0 ? (hRes.data.data || []) : []).filter((x) => x.targetType === FavoriteTargetType.HOTEL);
  } catch {
    hotelFavorites.value = [];
  }
  try {
    const rRes = await api.get('/favorites/my-with-detail', { params: { type: FavoriteTargetType.RESTAURANT } });
    restaurantFavorites.value = (rRes.data.code === 0 ? (rRes.data.data || []) : []).filter((x) => x.targetType === FavoriteTargetType.RESTAURANT);
  } catch {
    restaurantFavorites.value = [];
  }
};

onMounted(() => loadFavorites());
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
  padding: 20px 20px 8px;
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
.fav-tabs :deep(.el-tabs__nav-wrap::after) {
  background-color: #eef3f8;
}
.fav-tabs :deep(.el-tabs__item) {
  font-size: 14px;
  font-weight: 600;
}
.fav-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}
.fav-card {
  border: 1px solid #eef3f8;
  border-radius: 16px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 10px 24px rgba(30, 115, 190, 0.05);
}
.fav-cover {
  width: 100%;
  height: 160px;
  background: #f0f0f0;
}
.fav-cover.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 14px;
}
.fav-body {
  padding: 14px;
}
.fav-name {
  font-weight: 800;
  font-size: 17px;
  margin-bottom: 4px;
  color: #1a1a1a;
}
.fav-meta {
  color: #667085;
  font-size: 13px;
  margin-bottom: 6px;
}
.fav-desc {
  color: #50627a;
  font-size: 12px;
  line-height: 1.6;
  max-height: 3.2em;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 10px;
}
.fav-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}
.fav-price {
  color: #e6a23c;
  font-size: 13px;
  font-weight: 700;
}
@media (max-width: 1100px) {
  .page-shell {
    width: 92%;
  }
}
</style>

