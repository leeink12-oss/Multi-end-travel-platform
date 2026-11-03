<template>
  <el-row :gutter="16">
    <el-col :span="10">
      <el-card>
        <h3>我的酒店</h3>
        <el-button type="primary" size="small" @click="openHotelDialog()">新增酒店</el-button>
        <el-table
          :data="hotels"
          style="margin-top: 12px"
          @row-click="selectHotel"
          highlight-current-row
        >
          <el-table-column prop="name" label="名称" />
          <el-table-column label="封面图" width="120">
            <template #default="scope">
              <el-image
                v-if="hotelFirstImageForRow(scope.row.coverImage)"
                :src="hotelFirstImageForRow(scope.row.coverImage)"
                :preview-src-list="hotelImagesForRow(scope.row.coverImage)"
                fit="cover"
                style="width: 100px; height: 60px"
              />
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100" />
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button size="small" @click.stop="openHotelDialog(scope.row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-col>
    <el-col :span="14">
      <el-tabs v-if="currentHotel" v-model="activeTab">
        <el-tab-pane label="酒店订单" name="orders">
          <el-table :data="hotelOrders" size="small">
            <el-table-column prop="id" label="订单号" width="80" />
            <el-table-column prop="quantity" label="房间数" width="80" />
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
            <el-table-column label="入住/离店" width="220">
              <template #default="scope">
                {{ scope.row.checkInDate }} 至 {{ scope.row.checkOutDate }}
              </template>
            </el-table-column>
            <el-table-column label="入住状态" width="160">
              <template #default="scope">
                <el-tag
                  v-if="scope.row.started"
                  type="success"
                  size="small"
                  style="margin-right: 8px"
                >
                  已入住
                </el-tag>
                <el-button
                  v-if="!scope.row.started"
                  size="small"
                  type="primary"
                  @click="markStarted(scope.row, true, 'HOTEL')"
                >
                  标记已入住
                </el-button>
                <el-button
                  v-else
                  size="small"
                  type="default"
                  @click="markStarted(scope.row, false, 'HOTEL')"
                >
                  取消标记
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="用户评价" name="comments">
          <el-table :data="hotelComments" size="small">
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
                  <el-button size="small" type="primary" @click="replyHotelComment(scope.row)">
                    回复
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="房型管理" name="rooms">
          <el-button type="primary" size="small" @click="openRoomDialog()">新增房型</el-button>
          <el-table :data="rooms" size="small" style="margin-top: 12px">
            <el-table-column prop="roomType" label="房型" />
            <el-table-column prop="size" label="房间大小" />
            <el-table-column prop="facilities" label="设施" />
            <el-table-column prop="floor" label="楼层" width="80" />
            <el-table-column prop="todayPrice" label="今日价格" width="100" />
            <el-table-column label="图片" width="120">
              <template #default="scope">
                <el-image
                  v-if="roomFirstImageForRow(scope.row.imageUrl)"
                  :src="roomFirstImageForRow(scope.row.imageUrl)"
                  :preview-src-list="roomImagesForRow(scope.row.imageUrl)"
                  fit="cover"
                  style="width: 100px; height: 60px"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220">
              <template #default="scope">
                <el-button size="small" @click="openRoomDialog(scope.row)">编辑</el-button>
                <el-button size="small" type="danger" @click="deleteRoom(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
      <el-card v-else>
        <div>请在左侧选择一个酒店查看订单、评价和房型。</div>
      </el-card>
    </el-col>
  </el-row>

  <el-dialog v-model="hotelDialogVisible" title="酒店信息" width="500px">
    <el-form :model="hotelForm" label-width="80px">
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
      <el-form-item label="封面图">
        <el-upload
          class="upload-demo"
          action="/api/files/upload"
          :show-file-list="false"
          :multiple="true"
          :limit="3"
          :headers="uploadHeaders"
          :on-success="handleHotelImageSuccess"
          :on-error="handleUploadError"
        >
          <el-button size="small" type="primary">上传图片</el-button>
        </el-upload>
        <div v-if="hotelCoverImages.length" style="margin-top: 8px; display: flex; gap: 8px">
          <div
            v-for="url in hotelCoverImages"
            :key="url"
            style="position: relative"
          >
            <img
              :src="url"
              style="width: 80px; height: 60px; object-fit: cover; border-radius: 4px"
            />
            <el-icon
              style="position: absolute; top: 2px; right: 2px; cursor: pointer; color: #f56c6c"
              @click.stop="removeHotelCoverImage(url)"
            >
              <i class="el-icon-close"></i>
            </el-icon>
          </div>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="hotelDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="saveHotel">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="roomDialogVisible" title="房型信息" width="500px">
    <el-form :model="roomForm" label-width="80px">
      <el-form-item label="房型">
        <el-input v-model="roomForm.roomType" />
      </el-form-item>
      <el-form-item label="房间大小">
        <el-input v-model="roomForm.size" />
      </el-form-item>
      <el-form-item label="房间设施">
        <el-input v-model="roomForm.facilities" />
      </el-form-item>
      <el-form-item label="楼层">
        <el-input v-model="roomForm.floor" />
      </el-form-item>
      <el-form-item label="价格下限">
        <el-input v-model.number="roomForm.minPrice" />
      </el-form-item>
      <el-form-item label="价格上限">
        <el-input v-model.number="roomForm.maxPrice" />
      </el-form-item>
      <el-form-item label="房间图片">
        <el-upload
          class="upload-demo"
          action="/api/files/upload"
          :show-file-list="false"
          :multiple="true"
          :limit="3"
          :headers="uploadHeaders"
          :on-success="handleRoomImageSuccess"
          :on-error="handleUploadError"
        >
          <el-button size="small" type="primary">上传图片</el-button>
        </el-upload>
        <div v-if="roomImages.length" style="margin-top: 8px; display: flex; gap: 8px">
          <div
            v-for="url in roomImages"
            :key="url"
            style="position: relative"
          >
            <img
              :src="url"
              style="width: 80px; height: 60px; object-fit: cover; border-radius: 4px"
            />
            <el-icon
              style="position: absolute; top: 2px; right: 2px; cursor: pointer; color: #f56c6c"
              @click.stop="removeRoomImage(url)"
            >
              <i class="el-icon-close"></i>
            </el-icon>
          </div>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="roomDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="saveRoom">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import api from '../api';
import { FavoriteTargetType } from '../utils';

const hotels = ref([]);
const cities = ref([]);
const orders = ref([]);
const hotelComments = ref([]);
const rooms = ref([]);
const currentHotel = ref(null);
const activeTab = ref('orders');

const hotelDialogVisible = ref(false);
const roomDialogVisible = ref(false);
const hotelForm = reactive({});
const roomForm = reactive({});

const parseImages = (value) =>
  !value ? [] : String(value).split(',').map((s) => s.trim()).filter((s) => s);

const hotelCoverImages = computed(() => parseImages(hotelForm.coverImage));
const roomImages = computed(() => parseImages(roomForm.imageUrl));

const hotelImagesForRow = (value) => parseImages(value);
const hotelFirstImageForRow = (value) => {
  const list = hotelImagesForRow(value);
  return list[0] || '';
};

const roomImagesForRow = (value) => parseImages(value);
const roomFirstImageForRow = (value) => {
  const list = roomImagesForRow(value);
  return list[0] || '';
};

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token') || ''}`
}));

const loadData = async () => {
  const [hRes, cRes, oRes] = await Promise.all([
    api.get('/hotels/mine'),
    api.get('/cities'),
    api.get('/orders/merchant')
  ]);
  if (hRes.data.code === 0) hotels.value = hRes.data.data;
  if (cRes.data.code === 0) cities.value = cRes.data.data;
  if (oRes.data.code === 0) orders.value = oRes.data.data;
};

const hotelOrders = computed(() =>
  currentHotel.value
    ? orders.value.filter(
        (o) =>
          o.targetType === FavoriteTargetType.HOTEL &&
          o.targetId === currentHotel.value.id
      )
    : []
);

const markStarted = async (row, started, type) => {
  const label = type === 'HOTEL' ? '入住' : '出行';
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

const selectHotel = async (row) => {
  currentHotel.value = row;
  activeTab.value = 'orders';
  const [cRes, rRes] = await Promise.all([
    api.get('/comments/by-target', {
      params: { targetId: row.id, type: FavoriteTargetType.HOTEL }
    }),
    api.get(`/hotel-rooms/by-hotel/${row.id}`)
  ]);
  if (cRes.data.code === 0) hotelComments.value = cRes.data.data;
  if (rRes.data.code === 0) rooms.value = rRes.data.data;
  await loadTodayPrices();
};

const openHotelDialog = (row) => {
  Object.assign(hotelForm, row || { id: null, cityId: null, name: '', description: '', coverImage: '' });
  hotelDialogVisible.value = true;
};

const handleHotelImageSuccess = (response) => {
  if (response.code === 0) {
    const list = parseImages(hotelForm.coverImage);
    if (list.length >= 3) {
      ElMessage.warning('最多上传 3 张酒店封面图');
      return;
    }
    list.push(response.data);
    hotelForm.coverImage = list.join(',');
  }
};

const removeHotelCoverImage = (url) => {
  const list = parseImages(hotelForm.coverImage).filter((u) => u !== url);
  hotelForm.coverImage = list.join(',');
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

const openRoomDialog = (row) => {
  if (!currentHotel.value) {
    ElMessage.warning('请先选择一个酒店');
    return;
  }
  Object.assign(
    roomForm,
    row || {
      id: null,
      hotel: { id: currentHotel.value.id },
      roomType: '',
      size: '',
      facilities: '',
      floor: '',
      imageUrl: '',
      minPrice: null,
      maxPrice: null
    }
  );
  roomDialogVisible.value = true;
};

const handleRoomImageSuccess = (response) => {
  if (response.code === 0) {
    const list = parseImages(roomForm.imageUrl);
    if (list.length >= 3) {
      ElMessage.warning('最多上传 3 张房型图片');
      return;
    }
    list.push(response.data);
    roomForm.imageUrl = list.join(',');
  }
};

const removeRoomImage = (url) => {
  const list = parseImages(roomForm.imageUrl).filter((u) => u !== url);
  roomForm.imageUrl = list.join(',');
};

const handleUploadError = () => {
  ElMessage.error('图片上传失败，请确认已登录商家账号');
};

const saveRoom = async () => {
  try {
    if (roomForm.id) {
      await api.put(`/hotel-rooms/${roomForm.id}`, roomForm);
    } else {
      await api.post('/hotel-rooms', roomForm);
    }
    ElMessage.success('保存成功');
    roomDialogVisible.value = false;
    if (currentHotel.value) {
      const rRes = await api.get(`/hotel-rooms/by-hotel/${currentHotel.value.id}`);
      if (rRes.data.code === 0) rooms.value = rRes.data.data;
      await loadTodayPrices();
    }
  } catch {
    ElMessage.error('操作失败，确认已登录商家账号');
  }
};

const deleteRoom = async (row) => {
  await api.delete(`/hotel-rooms/${row.id}`);
  ElMessage.success('已删除房型');
  if (currentHotel.value) {
    const rRes = await api.get(`/hotel-rooms/by-hotel/${currentHotel.value.id}`);
    if (rRes.data.code === 0) rooms.value = rRes.data.data;
    await loadTodayPrices();
  }
};

const replyHotelComment = async (row) => {
  if (!row._replyDraft) {
    ElMessage.warning('请输入回复内容');
    return;
  }
  await api.post(`/comments/${row.id}/reply`, { replyContent: row._replyDraft });
  ElMessage.success('已回复');
  if (currentHotel.value) {
    await selectHotel(currentHotel.value);
  }
};

const loadTodayPrices = async () => {
  if (!currentHotel.value) return;
  const today = new Date().toISOString().slice(0, 10);
  const res = await api.get(`/hotel-room-prices/public/by-hotel/${currentHotel.value.id}`, {
    params: { date: today }
  });
  if (res.data.code === 0 && res.data.data && res.data.data.roomPrices) {
    const map = res.data.data.roomPrices;
    rooms.value = rooms.value.map((r) => ({
      ...r,
      todayPrice: map[r.id] || null
    }));
  }
};

onMounted(() => {
  loadData();
});
</script>

