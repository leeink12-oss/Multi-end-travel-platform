<template>
  <div class="page-wrapper">
    <el-card>
      <div class="header-row">
        <h3>商家消息</h3>
        <el-button size="small" @click="markAllRead">全部已读</el-button>
      </div>

      <el-table :data="messages" style="margin-top: 12px">
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag v-if="!row.readFlag" type="danger" size="small">未读</el-tag>
            <el-tag v-else type="success" size="small">已读</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="content" label="内容" />
        <el-table-column prop="createTime" label="时间" width="180" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button size="small" @click="markRead(row)" v-if="!row.readFlag">已读</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import api from '../api';

const messages = ref([]);

const load = async () => {
  const res = await api.get('/messages/my');
  if (res.data.code === 0) messages.value = res.data.data || [];
};

const markRead = async (row) => {
  await api.put(`/messages/${row.id}/read`);
  ElMessage.success('已标记已读');
  await load();
};

const markAllRead = async () => {
  await api.put('/messages/read-all');
  ElMessage.success('全部已读');
  await load();
};

onMounted(load);
</script>

<style scoped>
.page-wrapper { width: 70%; max-width: 1100px; margin: 24px auto; }
.header-row { display: flex; justify-content: space-between; align-items: center; }
</style>
