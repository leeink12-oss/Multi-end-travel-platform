<template>
  <el-row :gutter="16">
    <el-col :span="12">
      <el-card>
        <h3>我的景点</h3>
        <el-button type="primary" size="small" @click="openScenicDialog()">新增景点</el-button>
        <el-table :data="scenics" style="margin-top: 12px">
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="status" label="状态" />
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <el-button size="small" @click="openScenicDialog(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteScenic(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-col>
    <el-col :span="12">
      <el-card>
        <h3>我的酒店</h3>
        <el-button type="primary" size="small" @click="openHotelDialog()">新增酒店</el-button>
        <el-table :data="hotels" style="margin-top: 12px">
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="status" label="状态" />
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <el-button size="small" @click="openHotelDialog(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteHotel(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-col>
    <el-col :span="24" style="margin-top: 16px">
      <el-card>
        <h3>我的订单</h3>
        <el-table :data="orders" style="margin-top: 12px">
          <el-table-column prop="id" label="订单号" width="80" />
          <el-table-column prop="targetType" label="类型" width="80" />
          <el-table-column prop="targetId" label="目标ID" width="90" />
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="totalPrice" label="总价" width="100" />
          <el-table-column prop="status" label="状态" width="90" />
          <el-table-column prop="useDate" label="日期" />
          <el-table-column prop="createTime" label="创建时间" />
          <el-table-column label="操作" width="220">
            <template #default="scope">
              <template v-if="scope.row.status === 'CANCEL_REQUESTED'">
                <el-button
                  size="small"
                  type="success"
                  @click="approveCancel(scope.row)"
                >同意取消</el-button>
                <el-button
                  size="small"
                  type="danger"
                  @click="rejectCancel(scope.row)"
                >拒绝取消</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-col>
  </el-row>

  <el-dialog v-model="scenicDialogVisible" title="景点信息">
    <el-form :model="scenicForm">
      <el-form-item label="城市">
        <el-select v-model="scenicForm.cityId">
          <el-option v-for="c in cities" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="名称">
        <el-input v-model="scenicForm.name" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="scenicForm.description" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="scenicDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="saveScenic">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="hotelDialogVisible" title="酒店信息">
    <el-form :model="hotelForm">
      <el-form-item label="城市">
        <el-select v-model="hotelForm.cityId">
          <el-option v-for="c in cities" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="名称">
        <el-input v-model="hotelForm.name" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="hotelForm.description" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="hotelDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="saveHotel">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import api from '../api';

const scenics = ref([]);
const hotels = ref([]);
const cities = ref([]);
const orders = ref([]);

const scenicDialogVisible = ref(false);
const hotelDialogVisible = ref(false);
const scenicForm = reactive({});
const hotelForm = reactive({});

const loadData = async () => {
  const [sRes, hRes, cRes, oRes] = await Promise.all([
    api.get('/scenics/mine'),
    api.get('/hotels/mine'),
    api.get('/cities'),
    api.get('/orders/merchant')
  ]);
  if (sRes.data.code === 0) scenics.value = sRes.data.data;
  if (hRes.data.code === 0) hotels.value = hRes.data.data;
  if (cRes.data.code === 0) cities.value = cRes.data.data;
  if (oRes.data.code === 0) orders.value = oRes.data.data;
};

const openScenicDialog = (row) => {
  Object.assign(scenicForm, row || { id: null, cityId: null, name: '', description: '' });
  scenicDialogVisible.value = true;
};

const openHotelDialog = (row) => {
  Object.assign(hotelForm, row || { id: null, cityId: null, name: '', description: '' });
  hotelDialogVisible.value = true;
};

const saveScenic = async () => {
  try {
    if (scenicForm.id) {
      await api.put(`/scenics/${scenicForm.id}`, scenicForm);
    } else {
      await api.post('/scenics', scenicForm);
    }
    ElMessage.success('保存成功');
    scenicDialogVisible.value = false;
    await loadData();
  } catch {
    ElMessage.error('操作失败，确认已登录商家账号');
  }
};

const saveHotel = async () => {
  try {
    if (hotelForm.id) {
      await api.put(`/hotels/${hotelForm.id}`, hotelForm);
    } else {
      await api.post('/hotels', hotelForm);
    }
    ElMessage.success('保存成功');
    hotelDialogVisible.value = false;
    await loadData();
  } catch {
    ElMessage.error('操作失败，确认已登录商家账号');
  }
};

const deleteScenic = async (row) => {
  await api.delete(`/scenics/${row.id}`);
  ElMessage.success('已删除');
  await loadData();
};

const deleteHotel = async (row) => {
  await api.delete(`/hotels/${row.id}`);
  ElMessage.success('已删除');
  await loadData();
};

const approveCancel = async (row) => {
  await api.put(`/orders/${row.id}/cancel-approve`);
  ElMessage.success('已同意取消');
  await loadData();
};

const rejectCancel = async (row) => {
  await api.put(`/orders/${row.id}/cancel-reject`);
  ElMessage.success('已拒绝取消');
  await loadData();
};

onMounted(() => {
  loadData();
});
</script>

