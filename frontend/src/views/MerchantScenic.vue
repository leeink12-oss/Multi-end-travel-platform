<template>
  <el-row :gutter="16">
    <el-col :span="10">
      <el-card>
        <h3>我的景点</h3>
        <el-button type="primary" size="small" @click="openScenicDialog()">新增景点</el-button>
        <el-table
          :data="scenics"
          style="margin-top: 12px"
          @row-click="selectScenic"
          highlight-current-row
        >
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="status" label="状态" width="100" />
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button size="small" @click.stop="openScenicDialog(scope.row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-col>
    <el-col :span="14">
      <el-card v-if="currentScenic">
        <h3>景点订单 - {{ currentScenic.name }}</h3>
        <el-table :data="scenicOrders" size="small" style="margin-bottom: 16px">
          <el-table-column prop="id" label="订单号" width="80" />
          <el-table-column prop="quantity" label="人数" width="80" />
          <el-table-column prop="totalPrice" label="总价" width="100" />
          <el-table-column label="使用优惠券" width="100">
            <template #default="{ row }">{{ row.couponName || '-' }}</template>
          </el-table-column>
          <el-table-column label="券前金额" width="100">
            <template #default="{ row }">{{ row.priceBeforeCoupon != null ? '¥' + row.priceBeforeCoupon : '-' }}</template>
          </el-table-column>
          <el-table-column label="券后金额" width="100">
            <template #default="{ row }">{{ row.priceAfterCoupon != null ? '¥' + row.priceAfterCoupon : '-' }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100" />
          <el-table-column prop="useDate" label="出行日期" width="120" />
          <el-table-column label="出行状态" width="140">
            <template #default="scope">
              <el-tag
                v-if="scope.row.started"
                type="success"
                size="small"
                style="margin-right: 8px"
              >
                已出行
              </el-tag>
              <el-button
                v-if="!scope.row.started"
                size="small"
                type="primary"
                @click="markStarted(scope.row, true, 'SCENIC')"
              >
                标记已出行
              </el-button>
              <el-button
                v-else
                size="small"
                type="default"
                @click="markStarted(scope.row, false, 'SCENIC')"
              >
                取消标记
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <h3>用户评价</h3>
        <el-table :data="scenicComments" size="small">
          <el-table-column prop="score" label="评分" width="80" />
          <el-table-column prop="content" label="内容" />
          <el-table-column prop="createTime" label="时间" width="180" />
          <el-table-column label="回复" width="260">
            <template #default="scope">
              <div v-if="scope.row.replyContent">
                <div>回复：{{ scope.row.replyContent }}</div>
                <div style="font-size: 12px; color: #888">
                  {{ scope.row.replyTime }}
                </div>
              </div>
              <div v-else>
                <el-input
                  v-model="scope.row._replyDraft"
                  size="small"
                  placeholder="填写回复"
                  style="width: 150px; margin-right: 4px"
                />
                <el-button size="small" type="primary" @click="replyScenicComment(scope.row)">
                  回复
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
      <el-card v-else>
        <div>请在左侧选择一个景点查看订单和评价。</div>
      </el-card>
    </el-col>
  </el-row>

  <el-dialog v-model="scenicDialogVisible" title="景点信息" width="500px">
    <el-form :model="scenicForm" label-width="90px">
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
      <el-form-item label="收费方式">
        <el-radio-group v-model="scenicForm.free">
          <el-radio :label="true">免费景点</el-radio>
          <el-radio :label="false">收费景点</el-radio>
        </el-radio-group>
      </el-form-item>
      <template v-if="scenicForm.free === false">
        <el-form-item label="儿童票价格">
          <el-input
            v-model="scenicForm.childPrice"
            placeholder="例如：50，单位元"
            type="number"
          />
        </el-form-item>
        <el-form-item label="成人票价格">
          <el-input
            v-model="scenicForm.adultPrice"
            placeholder="例如：100，单位元"
            type="number"
          />
        </el-form-item>
        <el-form-item label="学生票价格">
          <el-input
            v-model="scenicForm.studentPrice"
            placeholder="例如：80，单位元"
            type="number"
          />
        </el-form-item>
      </template>
      <el-form-item label="封面图">
        <el-upload
          class="upload-demo"
          action="/api/files/upload"
          :show-file-list="false"
          :multiple="true"
          :limit="3"
          :headers="uploadHeaders"
          :on-success="handleScenicImageSuccess"
          :on-error="handleUploadError"
        >
          <el-button size="small" type="primary">上传图片</el-button>
        </el-upload>
        <div v-if="scenicImages.length" style="margin-top: 8px; display: flex; gap: 8px">
          <div
            v-for="url in scenicImages"
            :key="url"
            style="position: relative"
          >
            <img
              :src="url"
              style="width: 80px; height: 60px; object-fit: cover; border-radius: 4px"
            />
            <el-icon
              style="position: absolute; top: 2px; right: 2px; cursor: pointer; color: #f56c6c"
              @click.stop="removeScenicImage(url)"
            >
              <i class="el-icon-close"></i>
            </el-icon>
          </div>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="scenicDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="saveScenic">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import api from '../api';
import { FavoriteTargetType } from '../utils';

const scenics = ref([]);
const cities = ref([]);
const orders = ref([]);
const scenicComments = ref([]);
const currentScenic = ref(null);

const scenicDialogVisible = ref(false);
const scenicForm = reactive({});

const parseImages = (value) =>
  !value ? [] : String(value).split(',').map((s) => s.trim()).filter((s) => s);

const scenicImages = computed(() => parseImages(scenicForm.coverImage));

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token') || ''}`
}));

const loadData = async () => {
  const [sRes, cRes, oRes] = await Promise.all([
    api.get('/scenics/mine'),
    api.get('/cities'),
    api.get('/orders/merchant')
  ]);
  if (sRes.data.code === 0) scenics.value = sRes.data.data;
  if (cRes.data.code === 0) cities.value = cRes.data.data;
  if (oRes.data.code === 0) orders.value = oRes.data.data;
};

const scenicOrders = computed(() =>
  currentScenic.value
    ? orders.value.filter(
        (o) =>
          o.targetType === FavoriteTargetType.SCENIC &&
          o.targetId === currentScenic.value.id
      )
    : []
);

const markStarted = async (row, started, type) => {
  const label = type === 'SCENIC' ? '出行' : '入住';
  try {
    await api.put(`/orders/${row.id}/started`, null, {
      params: { started }
    });
    ElMessage.success(`已标记${label}状态`);
    await loadData();
  } catch (e) {
    ElMessage.error(`标记${label}状态失败`);
  }
};

const selectScenic = async (row) => {
  currentScenic.value = row;
  const res = await api.get('/comments/by-target', {
    params: { targetId: row.id, type: FavoriteTargetType.SCENIC }
  });
  if (res.data.code === 0) scenicComments.value = res.data.data;
};

const openScenicDialog = (row) => {
  Object.assign(
    scenicForm,
    row || {
      id: null,
      cityId: null,
      name: '',
      description: '',
      coverImage: '',
      free: false,
      childPrice: null,
      adultPrice: null,
      studentPrice: null
    }
  );
  scenicDialogVisible.value = true;
};

const handleScenicImageSuccess = (response) => {
  if (response.code === 0) {
    const list = parseImages(scenicForm.coverImage);
    if (list.length >= 3) {
      ElMessage.warning('最多上传 3 张封面图');
      return;
    }
    list.push(response.data);
    scenicForm.coverImage = list.join(',');
  }
};

const removeScenicImage = (url) => {
  const list = parseImages(scenicForm.coverImage).filter((u) => u !== url);
  scenicForm.coverImage = list.join(',');
};

const handleUploadError = () => {
  ElMessage.error('图片上传失败，请确认已登录商家账号');
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

const replyScenicComment = async (row) => {
  if (!row._replyDraft) {
    ElMessage.warning('请输入回复内容');
    return;
  }
  await api.post(`/comments/${row.id}/reply`, { replyContent: row._replyDraft });
  ElMessage.success('已回复');
  if (currentScenic.value) {
    await selectScenic(currentScenic.value);
  }
};

onMounted(() => {
  loadData();
});
</script>

