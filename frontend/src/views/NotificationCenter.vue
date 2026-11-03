<template>
  <div class="page-wrapper">
    <el-card>
      <div class="header-row">
        <div>
          <h3>通知中心</h3>
          <div class="sub-title">查看订单提醒、审核结果和平台消息</div>
        </div>
        <div class="header-actions">
          <el-button size="small" @click="refresh">刷新</el-button>
          <el-button size="small" @click="markAllRead">全部已读</el-button>
        </div>
      </div>

      <el-alert
        v-if="stats.total"
        :title="`共 ${stats.total} 条消息，未读 ${stats.unread} 条`"
        type="info"
        show-icon
        :closable="false"
        style="margin-top: 12px"
      />

      <el-tabs v-model="activeTab" class="msg-tabs" @tab-change="onTabChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="未读" name="unread" />
        <el-tab-pane label="已读" name="read" />
      </el-tabs>

      <div v-if="notifications.length" class="list">
        <div
          v-for="item in filteredNotifications"
          :key="item.id"
          class="item"
          :class="{ unread: !item.readFlag }"
        >
          <div class="title-line">
            <span class="title">{{ item.title }}</span>
            <div class="tag-group">
              <el-tag size="small">{{ categoryLabel(item.category) }}</el-tag>
              <el-tag v-if="!item.readFlag" type="danger" size="small">未读</el-tag>
            </div>
          </div>
          <div class="content">{{ item.content }}</div>
          <div class="time">{{ item.createTime }}</div>
          <div class="actions">
            <el-button size="small" @click.stop="openItem(item)">查看</el-button>
            <el-button size="small" type="danger" plain @click.stop="deleteItem(item)">删除</el-button>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无通知" />
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import api from '../api';

const notifications = ref([]);
const activeTab = ref('all');
const stats = ref({ total: 0, unread: 0 });

const load = async () => {
  const res = await api.get('/messages/my');
  if (res.data.code === 0) {
    notifications.value = res.data.data || [];
    stats.value = {
      total: notifications.value.length,
      unread: notifications.value.filter((n) => !n.readFlag).length,
    };
  }
};

const refresh = async () => {
  await load();
  window.dispatchEvent(new Event('notifications-changed'));
};

const filteredNotifications = computed(() => {
  if (activeTab.value === 'unread') return notifications.value.filter((n) => !n.readFlag);
  if (activeTab.value === 'read') return notifications.value.filter((n) => n.readFlag);
  return notifications.value;
});

const categoryLabel = (category) => {
  const map = {
    MERCHANT_ORDER: '商家订单',
    USER_ORDER: '用户订单',
    MERCHANT_REVIEW: '商家审核',
    SYSTEM: '系统消息',
    SUPPORT: '客服消息',
  };
  return map[category] || '消息';
};

const onTabChange = () => {};

const markAllRead = async () => {
  await api.put('/messages/read-all');
  await refresh();
  ElMessage.success('已全部标记为已读');
};

const openItem = async (item) => {
  try {
    if (!item.readFlag) {
      await api.put(`/messages/${item.id}/read`);
      item.readFlag = true;
      stats.value.unread = Math.max(0, stats.value.unread - 1);
    }
    await refresh();
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '查看失败');
  }
};

const deleteItem = async (item) => {
  try {
    await api.delete(`/messages/${item.id}`);
    notifications.value = notifications.value.filter((n) => n.id !== item.id);
    await refresh();
    ElMessage.success('已删除通知');
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '删除失败');
  }
};

onMounted(load);
</script>

<style scoped>
.page-wrapper { width: 70%; max-width: 1100px; margin: 24px auto; }
.header-row { display: flex; justify-content: space-between; align-items: flex-start; }
.sub-title { margin-top: 4px; font-size: 12px; color: #888; }
.header-actions { display: flex; gap: 8px; }
.msg-tabs { margin-top: 12px; }
.list { display: flex; flex-direction: column; gap: 10px; margin-top: 12px; }
.item { padding: 12px 14px; border: 1px solid #eee; border-radius: 8px; cursor: pointer; }
.item.unread { background: #fff7f7; border-color: #f3b1b1; }
.title-line { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
.tag-group { display: flex; gap: 6px; flex-wrap: wrap; }
.title { font-weight: 600; }
.content { margin-top: 6px; color: #555; }
.time { margin-top: 6px; font-size: 12px; color: #999; }
</style>
