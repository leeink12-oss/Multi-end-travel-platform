<template>
  <div class="admin-dashboard">
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card>
          <div class="section-header">
            <h3>商家审核</h3>
          </div>
          <el-table :data="merchants" v-loading="loading">
            <el-table-column prop="name" label="名称" />
            <el-table-column label="类型" width="140">
              <template #default="{ row }">
                {{ row.type === 'TRANSPORT' ? '出行商家' : (row.type === 'CATERING' ? '餐饮商家' : '旅游商家') }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" />
            <el-table-column label="操作" width="320">
              <template #default="scope">
                <el-button size="small" type="success" @click="changeStatus(scope.row, 'APPROVED')">通过</el-button>
                <el-button size="small" type="danger" @click="changeStatus(scope.row, 'DISABLED')">禁用</el-button>
                <el-button size="small" type="danger" @click="deleteMerchant(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <div class="section-header">
            <h3>城市管理</h3>
            <el-button type="primary" size="small" @click="openCityDialog()">新增城市</el-button>
          </div>
          <el-table :data="cities" v-loading="loading">
            <el-table-column prop="name" label="城市" />
            <el-table-column prop="province" label="省份" />
            <el-table-column prop="enabled" label="启用">
              <template #default="scope">
                <el-tag :type="scope.row.enabled ? 'success' : 'info'">
                  {{ scope.row.enabled ? '是' : '否' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button size="small" @click="openCityDialog(scope.row)">编辑</el-button>
                <el-button size="small" type="danger" @click="deleteCity(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card>
          <div class="section-header">
            <h3>景点审核</h3>
          </div>
          <el-table :data="scenics" v-loading="loading">
            <el-table-column prop="name" label="名称" />
            <el-table-column label="首图" width="120">
              <template #default="{ row }">
                <el-image v-if="firstImage(row.coverImage)" :src="firstImage(row.coverImage)" style="width: 90px; height: 56px" fit="cover" />
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" />
            <el-table-column label="操作" width="220">
              <template #default="scope">
                <el-button size="small" type="success" @click="changeScenicStatus(scope.row, 'ONLINE')">上线</el-button>
                <el-button size="small" type="warning" @click="changeScenicStatus(scope.row, 'OFFLINE')">下线</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <div class="section-header">
            <h3>酒店审核</h3>
          </div>
          <el-table :data="hotels" v-loading="loading">
            <el-table-column prop="name" label="名称" />
            <el-table-column label="首图" width="120">
              <template #default="{ row }">
                <el-image v-if="firstImage(row.coverImage)" :src="firstImage(row.coverImage)" style="width: 90px; height: 56px" fit="cover" />
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" />
            <el-table-column label="操作" width="220">
              <template #default="scope">
                <el-button size="small" type="success" @click="changeHotelStatus(scope.row, 'ONLINE')">上线</el-button>
                <el-button size="small" type="warning" @click="changeHotelStatus(scope.row, 'OFFLINE')">下线</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="24">
        <el-card>
          <div class="section-header">
            <h3>餐饮店铺审核</h3>
          </div>
          <el-table :data="restaurants" v-loading="loading">
            <el-table-column prop="name" label="名称" />
            <el-table-column label="首图" width="120">
              <template #default="{ row }">
                <el-image v-if="firstImage(row.coverImage)" :src="firstImage(row.coverImage)" style="width: 90px; height: 56px" fit="cover" />
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120" />
            <el-table-column label="操作" width="320">
              <template #default="scope">
                <el-button size="small" @click="openRestaurantDetail(scope.row)">查看详情</el-button>
                <el-button size="small" type="success" @click="changeRestaurantStatus(scope.row, 'ONLINE')">上线</el-button>
                <el-button size="small" type="warning" @click="changeRestaurantStatus(scope.row, 'OFFLINE')">下线</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <RestaurantDetailDialog v-model="restaurantDetailVisible" :restaurant="selectedRestaurant" />

    <el-dialog v-model="cityDialogVisible" title="城市信息">
      <el-form :model="cityForm">
        <el-form-item label="城市">
          <el-input v-model="cityForm.name" />
        </el-form-item>
        <el-form-item label="省份">
          <el-input v-model="cityForm.province" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="cityForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cityDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCity">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import api from '../api';
import RestaurantDetailDialog from '../components/RestaurantDetailDialog.vue';

const merchants = ref([]);
const cities = ref([]);
const scenics = ref([]);
const hotels = ref([]);
const restaurants = ref([]);
const loading = ref(false);
const cityDialogVisible = ref(false);
const cityForm = reactive({});
const restaurantDetailVisible = ref(false);
const selectedRestaurant = ref(null);

const firstImage = (value) => (value ? String(value).split(',').map((s) => s.trim()).filter(Boolean)[0] || '' : '');

const loadData = async () => {
  loading.value = true;
  try {
    const [mRes, cRes, sRes, hRes, rRes] = await Promise.all([
      api.get('/merchants/admin/all'),
      api.get('/cities/all'),
      api.get('/scenics/admin/all'),
      api.get('/hotels/admin/all'),
      api.get('/restaurants/admin/all')
    ]);
    if (mRes.data.code === 0) merchants.value = mRes.data.data;
    if (cRes.data.code === 0) cities.value = cRes.data.data;
    if (sRes.data.code === 0) scenics.value = sRes.data.data;
    if (hRes.data.code === 0) hotels.value = hRes.data.data;
    if (rRes.data.code === 0) restaurants.value = rRes.data.data;
  } finally {
    loading.value = false;
  }
};

const changeStatus = async (row, status) => { await api.put(`/merchants/admin/${row.id}/status`, null, { params: { status } }); ElMessage.success('状态已更新'); await loadData(); };
const deleteMerchant = async (row) => { await api.delete(`/merchants/admin/${row.id}`); ElMessage.success('商家已删除'); await loadData(); };
const openCityDialog = (row) => { Object.assign(cityForm, row || { id: null, name: '', province: '', enabled: true }); cityDialogVisible.value = true; };
const saveCity = async () => { if (cityForm.id) await api.put(`/cities/${cityForm.id}`, cityForm); else await api.post('/cities', cityForm); ElMessage.success('保存成功'); cityDialogVisible.value = false; await loadData(); };
const deleteCity = async (row) => { await api.delete(`/cities/${row.id}`); ElMessage.success('已删除'); await loadData(); };
const changeScenicStatus = async (row, status) => { await api.put(`/scenics/admin/${row.id}/status`, null, { params: { status } }); ElMessage.success('景点状态已更新'); await loadData(); };
const changeHotelStatus = async (row, status) => { await api.put(`/hotels/admin/${row.id}/status`, null, { params: { status } }); ElMessage.success('酒店状态已更新'); await loadData(); };
const openRestaurantDetail = (row) => { selectedRestaurant.value = row; restaurantDetailVisible.value = true; };
const changeRestaurantStatus = async (row, status) => { await api.put(`/restaurants/admin/${row.id}/status`, null, { params: { status } }); ElMessage.success('餐饮店铺状态已更新'); await loadData(); };

onMounted(loadData);
</script>

<style scoped>
.admin-dashboard { display: flex; flex-direction: column; gap: 16px; }
.section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
</style>
