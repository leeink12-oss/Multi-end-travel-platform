<template>
  <el-card>
    <h3>航班管理（出行商家）</h3>
    <el-button type="primary" size="small" @click="openDialog()">新增航班</el-button>
    <el-table :data="flights" style="margin-top: 12px">
      <el-table-column prop="flightNo" label="航班号" width="120" />
      <el-table-column prop="fromCity" label="出发" />
      <el-table-column prop="toCity" label="到达" />
      <el-table-column prop="departTime" label="起飞" width="170" />
      <el-table-column prop="arriveTime" label="到达" width="170" />
      <el-table-column prop="price" label="价格" width="100" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-card style="margin-top: 16px">
    <h3>出行订单（航班）</h3>
    <el-table :data="flightOrders" size="small" style="margin-top: 12px">
      <el-table-column prop="id" label="订单号" width="80" />
      <el-table-column prop="quantity" label="人数" width="80" />
      <el-table-column prop="totalPrice" label="总价" width="100" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column label="检票状态" width="160">
        <template #default="scope">
          <el-tag v-if="scope.row.started" type="success" size="small" style="margin-right: 8px">已检票</el-tag>
          <el-button v-if="!scope.row.started" size="small" type="primary" @click="markChecked(scope.row, true)">标记已检票</el-button>
          <el-button v-else size="small" @click="markChecked(scope.row, false)">取消标记</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" title="航班信息">
    <el-form :model="form">
      <el-form-item label="航班号"><el-input v-model="form.flightNo" /></el-form-item>
      <el-form-item label="出发城市"><el-input v-model="form.fromCity" /></el-form-item>
      <el-form-item label="到达城市"><el-input v-model="form.toCity" /></el-form-item>
      <el-form-item label="起飞时间">
        <el-date-picker
          v-model="form.departTime"
          type="datetime"
          value-format="YYYY-MM-DDTHH:mm"
          placeholder="选择起飞时间"
        />
      </el-form-item>
      <el-form-item label="到达时间">
        <el-date-picker
          v-model="form.arriveTime"
          type="datetime"
          value-format="YYYY-MM-DDTHH:mm"
          placeholder="选择到达时间"
        />
      </el-form-item>
      <el-form-item label="经济舱票价"><el-input v-model="form.economyClassPrice" /></el-form-item>
      <el-form-item label="商务舱票价"><el-input v-model="form.businessClassPrice" /></el-form-item>
      <el-form-item label="头等舱票价"><el-input v-model="form.firstClassPrice" /></el-form-item>
      <el-form-item label="最低展示价(可选)">
        <el-input v-model="form.price" placeholder="不填则自动取舱位最低价" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="form.status">
          <el-option label="ONLINE" value="ONLINE" />
          <el-option label="OFFLINE" value="OFFLINE" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import api from '../api';

const flights = ref([]);
const flightOrders = ref([]);
const dialogVisible = ref(false);
const form = reactive({});

const load = async () => {
  const [res, oRes] = await Promise.all([
    api.get('/transport/merchant/flights'),
    api.get('/orders/merchant')
  ]);
  if (res.data.code === 0) flights.value = res.data.data || [];
  if (oRes.data.code === 0) {
    flightOrders.value = (oRes.data.data || []).filter((o) => o.targetType === 'FLIGHT');
  }
};

const openDialog = (row) => {
  Object.assign(form, row || {
    id: null,
    flightNo: '',
    fromCity: '',
    toCity: '',
    departTime: null,
    arriveTime: null,
    economyClassPrice: 0,
    businessClassPrice: null,
    firstClassPrice: null,
    price: null,
    status: 'ONLINE'
  });
  dialogVisible.value = true;
};

const save = async () => {
  if (!form.departTime || !form.arriveTime) {
    ElMessage.warning('请选择起飞时间和到达时间');
    return;
  }
  if (form.id) {
    await api.put(`/transport/merchant/flights/${form.id}`, form);
  } else {
    await api.post('/transport/merchant/flights', form);
  }
  ElMessage.success('保存成功');
  dialogVisible.value = false;
  await load();
};

const remove = async (row) => {
  await api.delete(`/transport/merchant/flights/${row.id}`);
  ElMessage.success('已删除');
  await load();
};

const markChecked = async (row, checked) => {
  await api.put(`/orders/${row.id}/started`, null, { params: { started: checked } });
  ElMessage.success('已更新检票状态');
  await load();
};

onMounted(() => load());
</script>

