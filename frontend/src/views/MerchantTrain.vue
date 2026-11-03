<template>
  <el-card>
    <h3>列车管理（出行商家）</h3>
    <el-button type="primary" size="small" @click="openDialog()">新增列车</el-button>
    <el-table :data="trains" style="margin-top: 12px">
      <el-table-column prop="trainNo" label="车次" width="120" />
      <el-table-column label="封面" width="120">
        <template #default="{ row }">
          <el-image
            v-if="row.coverImage"
            :src="row.coverImage"
            fit="cover"
            style="width: 96px; height: 54px; border-radius: 6px"
          />
        </template>
      </el-table-column>
      <el-table-column prop="fromCity" label="出发" />
      <el-table-column prop="toCity" label="到达" />
      <el-table-column prop="departTime" label="出发" width="170" />
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
    <h3>出行订单（列车）</h3>
    <el-table :data="trainOrders" size="small" style="margin-top: 12px">
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

  <el-dialog v-model="dialogVisible" title="列车信息">
    <el-form :model="form">
      <el-form-item label="车次"><el-input v-model="form.trainNo" /></el-form-item>
      <el-form-item label="出发城市"><el-input v-model="form.fromCity" /></el-form-item>
      <el-form-item label="到达城市"><el-input v-model="form.toCity" /></el-form-item>
      <el-form-item label="出发时间">
        <el-date-picker
          v-model="form.departTime"
          type="datetime"
          value-format="YYYY-MM-DDTHH:mm"
          placeholder="选择出发时间"
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
      <el-form-item label="列车类型">
        <el-radio-group v-model="form.trainType">
          <el-radio label="EMU">动车/高铁</el-radio>
          <el-radio label="TRAIN">普通火车</el-radio>
        </el-radio-group>
      </el-form-item>

      <template v-if="form.trainType === 'EMU'">
        <el-form-item label="头等座"><el-input v-model="form.emuFirstClassSeatPrice" /></el-form-item>
        <el-form-item label="一等座"><el-input v-model="form.emuFirstSeatPrice" /></el-form-item>
        <el-form-item label="二等座"><el-input v-model="form.emuSecondSeatPrice" /></el-form-item>
        <el-form-item label="无座"><el-input v-model="form.emuNoSeatPrice" /></el-form-item>
      </template>
      <template v-else>
        <el-form-item label="硬座"><el-input v-model="form.trainHardSeatPrice" /></el-form-item>
        <el-form-item label="软卧"><el-input v-model="form.trainSoftSleeperPrice" /></el-form-item>
        <el-form-item label="硬卧"><el-input v-model="form.trainHardSleeperPrice" /></el-form-item>
      </template>

      <el-form-item label="封面图URL">
        <el-input v-model="form.coverImage" placeholder="可选：图片链接" />
      </el-form-item>

      <el-form-item label="最低展示价(可选)">
        <el-input v-model="form.price" placeholder="不填则自动取席别最低价" />
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

const trains = ref([]);
const trainOrders = ref([]);
const dialogVisible = ref(false);
const form = reactive({});

const load = async () => {
  const [res, oRes] = await Promise.all([
    api.get('/transport/merchant/trains'),
    api.get('/orders/merchant')
  ]);
  if (res.data.code === 0) trains.value = res.data.data || [];
  if (oRes.data.code === 0) {
    trainOrders.value = (oRes.data.data || []).filter((o) => o.targetType === 'TRAIN');
  }
};

const openDialog = (row) => {
  Object.assign(form, row || {
    id: null,
    trainNo: '',
    fromCity: '',
    toCity: '',
    departTime: null,
    arriveTime: null,
    trainType: 'EMU',
    emuFirstClassSeatPrice: null,
    emuFirstSeatPrice: null,
    emuSecondSeatPrice: 0,
    emuNoSeatPrice: null,
    trainHardSeatPrice: 0,
    trainSoftSleeperPrice: null,
    trainHardSleeperPrice: null,
    coverImage: '',
    price: null,
    status: 'ONLINE'
  });
  dialogVisible.value = true;
};

const save = async () => {
  if (!form.departTime || !form.arriveTime) {
    ElMessage.warning('请选择出发时间和到达时间');
    return;
  }
  if (form.id) {
    await api.put(`/transport/merchant/trains/${form.id}`, form);
  } else {
    await api.post('/transport/merchant/trains', form);
  }
  ElMessage.success('保存成功');
  dialogVisible.value = false;
  await load();
};

const remove = async (row) => {
  await api.delete(`/transport/merchant/trains/${row.id}`);
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

