<template>
  <el-dialog v-model="visible" title="餐饮店铺审核详情" width="760px">
    <el-descriptions v-if="restaurant" :column="2" border>
      <el-descriptions-item label="店铺名称">{{ restaurant.name || '-' }}</el-descriptions-item>
      <el-descriptions-item label="所属商家">{{ restaurant.merchant?.name || '-' }}</el-descriptions-item>
      <el-descriptions-item label="联系电话">{{ restaurant.merchant?.contactPhone || '-' }}</el-descriptions-item>
      <el-descriptions-item label="商家账号">{{ restaurant.merchant?.user?.username || '-' }}</el-descriptions-item>
      <el-descriptions-item label="上传图片" :span="2">
        <div style="display: flex; gap: 12px; flex-wrap: wrap">
          <el-image
            v-for="img in images"
            :key="img"
            :src="img"
            style="width: 120px; height: 80px; border-radius: 8px"
            fit="cover"
            :preview-src-list="images"
            preview-teleported
          />
        </div>
      </el-descriptions-item>
    </el-descriptions>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue';

const visible = defineModel({ type: Boolean, default: false });
const props = defineProps({ restaurant: { type: Object, default: null } });

const images = computed(() => {
  const value = props.restaurant?.coverImage;
  if (!value) return [];
  return String(value).split(',').map((s) => s.trim()).filter(Boolean);
});
</script>
