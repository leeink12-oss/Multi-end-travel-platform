<template>
  <el-card>
    <h3>我的餐饮店铺</h3>
    <el-button type="primary" size="small" @click="openDialog()">新增店铺</el-button>
    <el-table :data="restaurants" style="margin-top: 12px">
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="avgPrice" label="人均" width="100" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteItem(row)">删除</el-button>
          <el-button size="small" type="primary" @click="toggleReservations(row)">
            {{ selectedRestaurant && selectedRestaurant.id === row.id ? '收起' : '预订' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-card style="margin-top: 12px" v-if="selectedRestaurant">
    <h3>店铺预订：{{ selectedRestaurant.name }}</h3>
    <el-table :data="reservations">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="contactName" label="联系人" />
      <el-table-column prop="contactPhone" label="电话" />
      <el-table-column prop="peopleCount" label="人数" width="80" />
      <el-table-column prop="reserveTime" label="到店时间" width="170" />
      <el-table-column prop="status" label="状态" width="110" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="changeReservationStatus(row, 'CONFIRMED')">确认</el-button>
          <el-button size="small" type="danger" @click="changeReservationStatus(row, 'CANCELLED')">取消</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-card style="margin-top: 12px" v-if="selectedRestaurant">
    <div style="display:flex;justify-content:space-between;align-items:center">
      <h3>团购套餐：{{ selectedRestaurant.name }}</h3>
      <el-button type="primary" size="small" @click="openPackageDialog()">新增套餐</el-button>
    </div>
    <el-table :data="grouponPackages" style="margin-top: 8px">
      <el-table-column prop="title" label="套餐名称" />
      <el-table-column prop="groupPrice" label="团购价" width="100" />
      <el-table-column prop="originalPrice" label="原价" width="100" />
      <el-table-column prop="stock" label="库存" width="90" />
      <el-table-column prop="enabled" label="启用" width="80">
        <template #default="{ row }">{{ row.enabled ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="openPackageDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deletePackage(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-card style="margin-top: 12px" v-if="selectedRestaurant">
    <div style="display:flex;justify-content:space-between;align-items:center">
      <h3>网友推荐菜：{{ selectedRestaurant.name }}</h3>
      <el-button type="primary" size="small" @click="openDishDialog()">新增推荐菜</el-button>
    </div>
    <el-table :data="recommendedDishes" style="margin-top: 8px">
      <el-table-column prop="name" label="菜名" />
      <el-table-column label="图片" width="110">
        <template #default="{ row }">
          <el-image v-if="row.imageUrl" :src="row.imageUrl" style="width: 80px; height: 56px" fit="cover" />
        </template>
      </el-table-column>
      <el-table-column prop="likeCount" label="点赞数" width="90" />
      <el-table-column prop="description" label="描述" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="openDishDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteDish(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-card style="margin-top: 12px" v-if="selectedRestaurant">
    <h3>用户评论：{{ selectedRestaurant.name }}</h3>
    <el-table :data="restaurantComments">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="用户" width="140">
        <template #default="{ row }">{{ row.user?.nickname || row.user?.username }}</template>
      </el-table-column>
      <el-table-column prop="score" label="评分" width="90" />
      <el-table-column prop="content" label="评论内容" />
      <el-table-column prop="replyContent" label="商家回复" />
      <el-table-column label="操作" width="130">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openReplyDialog(row)">回复评论</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-card style="margin-top: 12px" v-if="selectedRestaurant">
    <h3>团购订单：{{ selectedRestaurant.name }}</h3>
    <el-table :data="grouponOrders">
      <el-table-column prop="id" label="订单号" width="90" />
      <el-table-column label="套餐" width="220">
        <template #default="{ row }">{{ row.grouponPackage?.title }}</template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" width="90" />
      <el-table-column prop="totalPrice" label="总价" width="100" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="verifyCode" label="核销码" width="100" />
      <el-table-column prop="createTime" label="创建时间" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <template v-if="row.status === 'UNUSED'">
            <el-input
              v-model="verifyCodeMap[row.id]"
              size="small"
              placeholder="输入核销码"
              style="width: 90px; margin-right: 6px"
            />
            <el-button size="small" type="success" @click="verifyOrder(row)">核销</el-button>
          </template>
          <span v-else-if="row.status === 'USED'" style="color: #67c23a">已核销</span>
          <span v-else-if="row.status === 'REFUNDED'" style="color: #909399">已退款</span>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" title="餐饮店铺">
    <el-form :model="form" label-width="80px">
      <el-form-item label="城市">
        <el-select v-model="form.cityId">
          <el-option v-for="c in cities" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="描述"><el-input v-model="form.description" /></el-form-item>
      <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
      <el-form-item label="人均"><el-input v-model="form.avgPrice" /></el-form-item>
      <el-form-item label="营业"><el-input v-model="form.openHours" placeholder="例：10:00-22:00" /></el-form-item>
      <el-form-item label="封面图">
        <el-upload
          class="upload-demo"
          action="/api/files/upload"
          :show-file-list="false"
          :headers="uploadHeaders"
          :on-success="handleRestaurantImageSuccess"
          :on-error="handleUploadError"
        >
          <el-button size="small" type="primary">上传图片</el-button>
        </el-upload>
        <div v-if="form.coverImage" style="margin-top: 8px; position: relative; width: 90px">
          <img :src="form.coverImage" style="width: 90px; height: 70px; object-fit: cover; border-radius: 4px" />
          <el-icon
            style="position:absolute;top:2px;right:2px;cursor:pointer;color:#f56c6c"
            @click.stop="form.coverImage = ''"
          >
            <i class="el-icon-close"></i>
          </el-icon>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="save">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="packageDialogVisible" title="团购套餐">
    <el-form :model="packageForm" label-width="90px">
      <el-form-item label="套餐名称"><el-input v-model="packageForm.title" /></el-form-item>
      <el-form-item label="套餐描述"><el-input v-model="packageForm.description" type="textarea" :rows="2" /></el-form-item>
      <el-form-item label="原价"><el-input v-model="packageForm.originalPrice" /></el-form-item>
      <el-form-item label="团购价"><el-input v-model="packageForm.groupPrice" /></el-form-item>
      <el-form-item label="库存"><el-input-number v-model="packageForm.stock" :min="0" /></el-form-item>
      <el-form-item label="套餐图片">
        <el-upload
          class="upload-demo"
          action="/api/files/upload"
          :show-file-list="false"
          :headers="uploadHeaders"
          :on-success="handlePackageImageSuccess"
          :on-error="handleUploadError"
        >
          <el-button size="small" type="primary">上传图片</el-button>
        </el-upload>
        <div v-if="packageForm.imageUrl" style="margin-top: 8px; position: relative; width: 90px">
          <img :src="packageForm.imageUrl" style="width: 90px; height: 70px; object-fit: cover; border-radius: 4px" />
          <el-icon
            style="position:absolute;top:2px;right:2px;cursor:pointer;color:#f56c6c"
            @click.stop="packageForm.imageUrl = ''"
          >
            <i class="el-icon-close"></i>
          </el-icon>
        </div>
      </el-form-item>
      <el-form-item label="启用"><el-switch v-model="packageForm.enabled" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="packageDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="savePackage">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="replyDialogVisible" title="回复评论">
    <el-form :model="replyForm" label-width="90px">
      <el-form-item label="回复内容">
        <el-input v-model="replyForm.replyContent" type="textarea" :rows="4" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="replyDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitReply">保存回复</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="dishDialogVisible" title="推荐菜">
    <el-form :model="dishForm" label-width="90px">
      <el-form-item label="菜名"><el-input v-model="dishForm.name" /></el-form-item>
      <el-form-item label="描述"><el-input v-model="dishForm.description" type="textarea" :rows="2" /></el-form-item>
      <el-form-item label="菜品图片">
        <el-upload
          class="upload-demo"
          action="/api/files/upload"
          :show-file-list="false"
          :headers="uploadHeaders"
          :on-success="handleDishImageSuccess"
          :on-error="handleUploadError"
        >
          <el-button size="small" type="primary">上传图片</el-button>
        </el-upload>
        <div v-if="dishForm.imageUrl" style="margin-top: 8px; position: relative; width: 90px">
          <img :src="dishForm.imageUrl" style="width: 90px; height: 70px; object-fit: cover; border-radius: 4px" />
          <el-icon
            style="position:absolute;top:2px;right:2px;cursor:pointer;color:#f56c6c"
            @click.stop="dishForm.imageUrl = ''"
          >
            <i class="el-icon-close"></i>
          </el-icon>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dishDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="saveDish">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import api from '../api';

const restaurants = ref([]);
const cities = ref([]);
const dialogVisible = ref(false);
const form = reactive({});
const selectedRestaurant = ref(null);
const reservations = ref([]);
const grouponPackages = ref([]);
const grouponOrders = ref([]);
const restaurantComments = ref([]);
const packageDialogVisible = ref(false);
const packageForm = reactive({});
const verifyCodeMap = ref({});
const recommendedDishes = ref([]);
const dishDialogVisible = ref(false);
const dishForm = reactive({});
const replyDialogVisible = ref(false);
const replyForm = reactive({ commentId: null, replyContent: '' });

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token') || ''}`
}));

const loadData = async () => {
  const [rRes, cRes] = await Promise.all([api.get('/restaurants/mine'), api.get('/cities')]);
  if (rRes.data.code === 0) restaurants.value = rRes.data.data || [];
  if (cRes.data.code === 0) cities.value = cRes.data.data || [];
};

const openDialog = (row) => {
  Object.assign(form, row || {
    id: null,
    cityId: null,
    name: '',
    description: '',
    address: '',
    avgPrice: '',
    openHours: '',
    coverImage: ''
  });
  if (row?.city?.id) {
    form.cityId = row.city.id;
  }
  dialogVisible.value = true;
};

const save = async () => {
  try {
    if (form.id) {
      await api.put(`/restaurants/${form.id}`, form);
    } else {
      await api.post('/restaurants', form);
    }
    ElMessage.success('保存成功');
    dialogVisible.value = false;
    await loadData();
  } catch {
    ElMessage.error('操作失败，请确认你是餐饮商家');
  }
};

const deleteItem = async (row) => {
  await api.delete(`/restaurants/${row.id}`);
  ElMessage.success('已删除');
  await loadData();
};

const loadReservations = async (row) => {
  selectedRestaurant.value = row;
  const [res, pkgRes, ordRes, cRes] = await Promise.all([
    api.get(`/restaurants/${row.id}/reservations`),
    api.get('/restaurants/groupon-packages', { params: { restaurantId: row.id } }),
    api.get('/restaurants/groupon-orders', { params: { restaurantId: row.id } }),
    api.get('/comments/by-target', { params: { targetId: row.id, type: 'RESTAURANT' } })
  ]);
  if (res.data.code === 0) reservations.value = res.data.data || [];
  if (pkgRes.data.code === 0) grouponPackages.value = pkgRes.data.data || [];
  if (ordRes.data.code === 0) grouponOrders.value = ordRes.data.data || [];
  if (cRes.data.code === 0) restaurantComments.value = cRes.data.data || [];
  const dRes = await api.get('/restaurants/recommended-dishes', { params: { restaurantId: row.id } });
  if (dRes.data.code === 0) recommendedDishes.value = dRes.data.data || [];
};

const toggleReservations = async (row) => {
  if (selectedRestaurant.value && selectedRestaurant.value.id === row.id) {
    selectedRestaurant.value = null;
    reservations.value = [];
    grouponPackages.value = [];
    grouponOrders.value = [];
    recommendedDishes.value = [];
    return;
  }
  await loadReservations(row);
};

const changeReservationStatus = async (row, status) => {
  await api.put(`/restaurants/reservations/${row.id}/status`, null, { params: { status } });
  ElMessage.success('状态已更新');
  if (selectedRestaurant.value) {
    await loadReservations(selectedRestaurant.value);
  }
};

const openPackageDialog = (row) => {
  Object.assign(packageForm, row || {
    id: null,
    title: '',
    description: '',
    originalPrice: '',
    groupPrice: '',
    imageUrl: '',
    stock: 0,
    enabled: true
  });
  packageDialogVisible.value = true;
};

const savePackage = async () => {
  if (!selectedRestaurant.value) return;
  const payload = { ...packageForm, restaurantId: selectedRestaurant.value.id };
  if (packageForm.id) {
    await api.put(`/restaurants/groupon-packages/${packageForm.id}`, payload);
  } else {
    await api.post('/restaurants/groupon-packages', payload);
  }
  ElMessage.success('套餐已保存');
  packageDialogVisible.value = false;
  await loadReservations(selectedRestaurant.value);
};

const deletePackage = async (row) => {
  await api.delete(`/restaurants/groupon-packages/${row.id}`);
  ElMessage.success('套餐已删除');
  if (selectedRestaurant.value) {
    await loadReservations(selectedRestaurant.value);
  }
};

const handleRestaurantImageSuccess = (response) => {
  if (response.code === 0) {
    form.coverImage = response.data;
  }
};

const handlePackageImageSuccess = (response) => {
  if (response.code === 0) {
    packageForm.imageUrl = response.data;
  }
};

const handleUploadError = () => {
  ElMessage.error('图片上传失败，请确认登录状态');
};

const verifyOrder = async (row) => {
  const code = verifyCodeMap.value[row.id] || '';
  if (!code) return ElMessage.warning('请输入核销码');
  const res = await api.put(`/restaurants/groupon-orders/${row.id}/verify`, null, { params: { code } });
  if (res.data.code === 0) {
    ElMessage.success('核销成功');
    if (selectedRestaurant.value) await loadReservations(selectedRestaurant.value);
  } else {
    ElMessage.error(res.data.message || '核销失败');
  }
};

const openReplyDialog = (row) => {
  replyForm.commentId = row.id;
  replyForm.replyContent = row.replyContent || '';
  replyDialogVisible.value = true;
};

const submitReply = async () => {
  if (!replyForm.commentId) return;
  if (!String(replyForm.replyContent || '').trim()) {
    ElMessage.warning('请输入回复内容');
    return;
  }
  try {
    const res = await api.post(`/comments/${replyForm.commentId}/reply`, { replyContent: replyForm.replyContent });
    if (res.data.code === 0) {
      ElMessage.success('回复成功');
      replyDialogVisible.value = false;
      if (selectedRestaurant.value) await loadReservations(selectedRestaurant.value);
    } else {
      ElMessage.error(res.data.message || '回复失败');
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '回复失败');
  }
};

const openDishDialog = (row) => {
  Object.assign(dishForm, row || { id: null, name: '', description: '', imageUrl: '' });
  dishDialogVisible.value = true;
};

const handleDishImageSuccess = (response) => {
  if (response.code === 0) {
    dishForm.imageUrl = response.data;
  }
};

const saveDish = async () => {
  if (!selectedRestaurant.value) return;
  if (!dishForm.name || !String(dishForm.name).trim()) {
    ElMessage.warning('请先填写推荐菜名称');
    return;
  }
  const payload = { ...dishForm, restaurantId: selectedRestaurant.value.id };
  payload.name = String(payload.name || '').trim();
  payload.description = payload.description == null ? '' : String(payload.description);
  payload.restaurantId = Number(payload.restaurantId);
  try {
    let res;
    if (dishForm.id) {
      res = await api.put(`/restaurants/recommended-dishes/${dishForm.id}`, payload);
    } else {
      res = await api.post('/restaurants/recommended-dishes', payload);
    }
    if (res.data.code === 0) {
      ElMessage.success('推荐菜已保存');
      dishDialogVisible.value = false;
      await loadReservations(selectedRestaurant.value);
    } else {
      ElMessage.error(res.data.message || '保存失败');
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '保存失败，请稍后再试');
  }
};

const deleteDish = async (row) => {
  await api.delete(`/restaurants/recommended-dishes/${row.id}`);
  ElMessage.success('推荐菜已删除');
  if (selectedRestaurant.value) await loadReservations(selectedRestaurant.value);
};

onMounted(loadData);
</script>

