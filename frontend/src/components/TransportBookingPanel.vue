<template>
  <el-card class="booking-card" :body-style="{ padding: '0' }">
    <div class="booking-hero">
      <div class="hero-copy">
        <div class="booking-title">出行预订</div>
        <div class="booking-subtitle">快速查询火车票与机票，轻松规划行程</div>
      </div>
      <el-radio-group v-model="form.ticketType" size="large" class="ticket-switch">
        <el-radio-button label="TRAIN">
          <span class="switch-label"><span class="switch-icon">🚆</span>火车票</span>
        </el-radio-button>
        <el-radio-button label="FLIGHT">
          <span class="switch-label"><span class="switch-icon">✈️</span>飞机票</span>
        </el-radio-button>
      </el-radio-group>
    </div>

    <div class="booking-body">
      <div class="search-shell">
        <div class="search-panel">
          <div class="field field-city">
            <div class="label">出发城市</div>
            <el-select v-model="form.fromCity" filterable placeholder="选择城市" class="full">
              <el-option v-for="c in cities" :key="c.id" :label="c.name" :value="c.name" />
            </el-select>
          </div>

          <div class="swap-wrap">
            <el-button circle class="swap-btn" @click="swapCities">⇄</el-button>
          </div>

          <div class="field field-city">
            <div class="label">到达城市</div>
            <el-select v-model="form.toCity" filterable placeholder="选择城市" class="full">
              <el-option v-for="c in cities" :key="c.id" :label="c.name" :value="c.name" />
            </el-select>
          </div>

          <div class="field field-date">
            <div class="label">出发日期</div>
            <el-date-picker
              v-model="form.date"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="选择日期"
              class="full"
            />
          </div>

          <div class="actions">
            <el-button type="primary" class="search-btn" @click="search">搜索</el-button>
          </div>
        </div>
      </div>

      <div class="quick-row">
        <div class="quick-card train" @click="quick('TRAIN')">
          <div class="quick-icon">🚆</div>
          <div class="quick-content">
            <div class="quick-title">火车票</div>
            <div class="quick-sub">一键搜索并进入预订</div>
          </div>
        </div>
        <div class="quick-card flight" @click="quick('FLIGHT')">
          <div class="quick-icon">✈️</div>
          <div class="quick-content">
            <div class="quick-title">机票</div>
            <div class="quick-sub">一键搜索并进入预订</div>
          </div>
        </div>
      </div>

      <div class="recent-section" v-if="isUserRole">
        <el-row :gutter="12" v-if="currentRecentList.length">
          <el-col v-for="item in currentRecentList" :key="item.id" :span="12">
            <el-card class="recent-item" shadow="hover" @click="openRecent(item)">
              <div class="recent-main">
                <div class="recent-info">
                  <div class="recent-name">{{ form.ticketType === 'TRAIN' ? item.trainNo : item.flightNo }}</div>
                  <div class="recent-meta">
                    {{ item.fromCity }} → {{ item.toCity }}
                  </div>
                  <div class="recent-meta">
                    {{ formatTime(item.departTime) }} - {{ formatTime(item.arriveTime) }}
                  </div>
                </div>
                <div class="recent-icon">{{ form.ticketType === 'TRAIN' ? '🚆' : '✈️' }}</div>
              </div>
              <div class="recent-price">¥{{ item.price ?? 0 }}</div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-else description="暂无最近出行资源" />
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import api from '../api';

const router = useRouter();
const cities = ref([]);
const recentTrains = ref([]);
const recentFlights = ref([]);

const form = reactive({
  ticketType: 'TRAIN',
  fromCity: '',
  toCity: '',
  date: new Date().toISOString().slice(0, 10)
});

const loadCities = async () => {
  const res = await api.get('/cities');
  if (res.data.code === 0) {
    cities.value = res.data.data || [];
    // 默认选前两个城市，避免空状态
    if (!form.fromCity && cities.value[0]) form.fromCity = cities.value[0].name;
    if (!form.toCity && cities.value[1]) form.toCity = cities.value[1].name;
  }
};

const sortByNearestTime = (list) => {
  const now = Date.now();
  return [...list].sort((a, b) => {
    const da = Math.abs(new Date(a.departTime).getTime() - now);
    const db = Math.abs(new Date(b.departTime).getTime() - now);
    return da - db;
  });
};

const loadRecentTransport = async () => {
  try {
    const [trainRes, flightRes] = await Promise.all([
      api.get('/transport/trains'),
      api.get('/transport/flights')
    ]);
    recentTrains.value = trainRes.data.code === 0 ? sortByNearestTime(trainRes.data.data || []).slice(0, 10) : [];
    recentFlights.value = flightRes.data.code === 0 ? sortByNearestTime(flightRes.data.data || []).slice(0, 10) : [];
  } catch {
    recentTrains.value = [];
    recentFlights.value = [];
  }
};

const swapCities = () => {
  const tmp = form.fromCity;
  form.fromCity = form.toCity;
  form.toCity = tmp;
};

const buildQuery = () => ({
  from: form.fromCity?.trim() || '',
  to: form.toCity?.trim() || '',
  date: form.date || ''
});

const search = () => {
  if (!form.fromCity?.trim() || !form.toCity?.trim()) {
    ElMessage.warning('请选择出发/到达城市');
    return;
  }
  const query = buildQuery();
  router.push(form.ticketType === 'TRAIN' ? { path: '/trains', query } : { path: '/flights', query });
};

const quick = (type) => {
  form.ticketType = type;
  search();
};

const isUserRole = computed(() => localStorage.getItem('role') === 'USER');
const currentRecentList = computed(() => (form.ticketType === 'TRAIN' ? recentTrains.value : recentFlights.value));

const formatTime = (value) => {
  if (!value) return '-';
  return String(value).replace('T', ' ').slice(0, 16);
};

const openRecent = (item) => {
  if (!item || !item.id) return;
  router.push(form.ticketType === 'TRAIN' ? `/train/${item.id}` : `/flight/${item.id}`);
};

onMounted(async () => {
  await loadCities();
  await loadRecentTransport();
});
</script>

<style scoped>
.booking-card {
  border: none;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0,0,0,0.08);
  background: #fff;
}
.booking-hero {
  background: #ffffff;
  color: #1a1a1a;
  padding: 20px 24px 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  border-bottom: 1px solid #eef3f8;
}
.booking-title {
  font-size: 24px;
  font-weight: 800;
  letter-spacing: -0.3px;
  color: #1e73be;
}
.booking-subtitle {
  margin-top: 6px;
  font-size: 13px;
  color: #667085;
}
.ticket-switch :deep(.el-radio-button__inner) {
  border-color: #d9e6f5;
  padding: 10px 16px;
}
.ticket-switch :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: #1e73be;
  border-color: #1e73be;
  color: #fff;
}
.switch-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.switch-icon {
  font-size: 14px;
}
.booking-body {
  padding: 18px 20px 20px;
  background: #fff;
}
.search-shell {
  border: 1px solid #eef3f8;
  border-radius: 16px;
  padding: 16px;
  background: linear-gradient(180deg, #fbfdff 0%, #ffffff 100%);
}
.search-panel {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) auto minmax(0, 1.15fr) minmax(220px, 1fr) auto;
  gap: 14px;
  align-items: end;
}
.field {
  min-width: 0;
}
.field-date {
  min-width: 220px;
}
.label {
  font-size: 12px;
  color: #667085;
  margin-bottom: 6px;
  font-weight: 600;
}
.field + .field,
.field + .swap-wrap,
.swap-wrap + .field,
.field-date + .actions {
  position: relative;
}
.field + .field::before,
.field + .field-date::before,
.field-date + .actions::before {
  content: '';
  position: absolute;
  left: -7px;
  top: 6px;
  bottom: 6px;
  width: 1px;
  background: linear-gradient(180deg, rgba(30,115,190,0.05), rgba(30,115,190,0.18), rgba(30,115,190,0.05));
}
.swap-wrap {
  padding-bottom: 4px;
}
.swap-btn {
  border: 1px solid #d9e6f5;
  background: #f7fbff;
  color: #1e73be;
  box-shadow: 0 4px 10px rgba(30, 115, 190, 0.06);
}
.swap-btn:hover,
.swap-btn:focus {
  border-color: #bcd2ea;
  background: #eef6ff;
  color: #1e73be;
}
.actions {
  display: flex;
  justify-content: flex-end;
}
.search-btn {
  width: 128px;
  height: 40px;
  border-radius: 10px;
  white-space: nowrap;
  background: linear-gradient(135deg, #1e73be 0%, #2b8ae0 100%);
  border: none;
  box-shadow: 0 10px 18px rgba(30, 115, 190, 0.18);
}
.search-btn:hover,
.search-btn:focus {
  background: linear-gradient(135deg, #155f9e 0%, #237bc6 100%);
}
.full {
  width: 100%;
}
:deep(.field-date .el-date-editor),
:deep(.field-date .el-date-editor.el-input__wrapper) {
  width: 100%;
}
:deep(.el-input__wrapper),
:deep(.el-select__wrapper) {
  border-radius: 10px;
}
.quick-row {
  margin-top: 16px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.quick-card {
  position: relative;
  border-radius: 14px;
  padding: 16px;
  cursor: pointer;
  color: #1a1a1a;
  overflow: hidden;
  transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  border: 1px solid #e6edf5;
  box-shadow: 0 6px 18px rgba(16, 24, 40, 0.04);
}
.quick-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(30, 115, 190, 0.12);
  border-color: #cfe0f3;
}
.quick-card.train,
.quick-card.flight {
  background: #fff;
}
.quick-card.train {
  border-left: 5px solid #2a9d8f;
}
.quick-card.flight {
  border-left: 5px solid #6c63ff;
}
.quick-icon {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: #f7fbff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
  border: 1px solid #e6edf5;
}
.quick-content {
  min-width: 0;
}
.quick-title {
  font-size: 16px;
  font-weight: 800;
  color: #1a1a1a;
}
.quick-sub {
  margin-top: 6px;
  font-size: 12px;
  color: #667085;
}
:deep(.el-select .el-input__wrapper),
:deep(.el-date-editor.el-input__wrapper) {
  border-radius: 10px;
}
@media (max-width: 1100px) {
  .search-panel {
    grid-template-columns: 1fr 1fr;
  }
  .field-date {
    min-width: 0;
  }
  .swap-wrap,
  .actions {
    justify-content: flex-start;
  }
  .quick-row {
    grid-template-columns: 1fr;
  }
}
</style>

