<template>
  <div class="page-wrapper">
    <el-card class="search-card">
      <div class="search-title">餐饮预订</div>
      <div class="toolbar">
        <el-select
          v-model="selectedCityId"
          clearable
          placeholder="定位城市"
          style="width: 220px"
          @change="loadData"
        >
          <el-option v-for="c in cities" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索商户名、地址、菜名等" @keyup.enter="loadData" />
        <el-button type="primary" @click="loadData">搜索</el-button>
      </div>
    </el-card>

    <div class="list-wrap">
      <div class="list-item" v-for="item in restaurants" :key="item.id" @click="$router.push(`/dining/${item.id}`)">
        <div class="img-wrap">
          <el-image v-if="firstImage(item.coverImage)" :src="firstImage(item.coverImage)" fit="cover" class="thumb" />
          <div v-else class="thumb empty">暂无图片</div>
        </div>
        <div class="content">
          <div class="title-row">
            <div class="title">{{ item.name }}</div>
            <div class="avg">人均 ¥{{ item.avgPrice || 0 }}</div>
          </div>
          <div class="meta">地址：{{ item.address || '暂无' }}</div>
          <div class="meta">营业时间：{{ item.openHours || '未设置' }}</div>
          <div class="desc">{{ item.description || '商家暂未填写简介。' }}</div>
          <div class="actions">
            <el-button type="primary" size="small" @click.stop="openReserve(item)">立即预订</el-button>
            <el-button size="small" @click.stop="$router.push(`/dining/${item.id}`)">查看详情</el-button>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="reserveVisible" :title="`预订：${currentRestaurant?.name || ''}`">
      <el-form :model="reserveForm" label-width="90px">
        <el-form-item label="到店时间">
          <el-date-picker v-model="reserveForm.reserveTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="人数">
          <el-input-number v-model="reserveForm.peopleCount" :min="1" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="reserveForm.contactName" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="reserveForm.contactPhone" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="reserveForm.note" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reserveVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReserve">提交预订</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import api from '../api';

const keyword = ref('');
const cities = ref([]);
const selectedCityId = ref(null);
const restaurants = ref([]);
const reserveVisible = ref(false);
const currentRestaurant = ref(null);
const reserveForm = reactive({
  reserveTime: '',
  peopleCount: 2,
  contactName: '',
  contactPhone: '',
  note: ''
});

const firstImage = (value) => (value ? String(value).split(',')[0].trim() : '');

const loadData = async () => {
  const params = {
    keyword: keyword.value || undefined,
    cityId: selectedCityId.value || undefined,
    limit: selectedCityId.value ? 50 : 20
  };
  const res = await api.get('/restaurants/search', { params });
  if (res.data.code === 0) {
    restaurants.value = res.data.data || [];
  }
};

const loadCities = async () => {
  const res = await api.get('/cities');
  if (res.data.code === 0) {
    cities.value = res.data.data || [];
  }
};

const openReserve = (row) => {
  currentRestaurant.value = row;
  reserveForm.reserveTime = '';
  reserveForm.peopleCount = 2;
  reserveForm.contactName = '';
  reserveForm.contactPhone = '';
  reserveForm.note = '';
  reserveVisible.value = true;
};

const submitReserve = async () => {
  if (!currentRestaurant.value) return;
  try {
    const res = await api.post(`/restaurants/${currentRestaurant.value.id}/reserve`, reserveForm);
    if (res.data.code === 0) {
      ElMessage.success('预订提交成功，请等待商家确认');
      reserveVisible.value = false;
    } else {
      ElMessage.error(res.data.message || '预订失败');
    }
  } catch {
    ElMessage.error('预订失败，请先登录用户账号');
  }
};

onMounted(async () => {
  await loadCities();
  await loadData();
});
</script>

<style scoped>
.page-wrapper {
  width: 100%;
}
.search-card {
  margin-bottom: 12px;
}
.search-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 10px;
}
.toolbar {
  display: flex;
  gap: 10px;
}
.list-wrap {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.list-item {
  display: flex;
  gap: 14px;
  padding: 12px;
  border: 1px solid #eee;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
}
.list-item:hover {
  box-shadow: 0 8px 18px rgba(0, 0, 0, 0.08);
}
.img-wrap {
  width: 220px;
  flex-shrink: 0;
}
.thumb {
  width: 220px;
  height: 140px;
  border-radius: 8px;
}
.thumb.empty {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  background: #f5f5f5;
}
.content {
  flex: 1;
}
.title-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 10px;
}
.title {
  font-weight: 700;
  font-size: 18px;
}
.avg {
  color: #f56c6c;
  font-weight: 600;
}
.meta {
  font-size: 13px;
  color: #666;
  margin: 6px 0;
}
.desc {
  color: #555;
  margin-top: 4px;
  margin-bottom: 10px;
}
.actions {
  display: flex;
  gap: 8px;
}
</style>

