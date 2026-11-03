<template>
  <div class="page-shell">
    <el-card class="page-card" :body-style="{ padding: '0' }">
      <div class="page-header">
        <div>
          <div class="page-kicker">我的评价 / Reviews</div>
          <h2 class="page-title">我的评价</h2>
          <div class="page-subtitle">查看你在酒店、景点、餐饮和出行中的评价记录，以及商家回复。</div>
        </div>
        <div class="header-badge">{{ comments.length }} 条评价</div>
      </div>

      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-label">总评价数</div>
          <div class="stat-value">{{ comments.length }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">可管理评价</div>
          <div class="stat-value">{{ comments.filter((c) => !c.replyContent).length }}</div>
        </div>
        <div class="stat-card accent">
          <div class="stat-label">已回复</div>
          <div class="stat-value">{{ comments.filter((c) => c.replyContent).length }}</div>
        </div>
      </div>

      <div class="table-shell" v-if="comments.length">
        <el-table :data="comments" class="comment-table" stripe>
          <el-table-column label="评价对象" min-width="180">
            <template #default="scope">
              <div class="target-cell">
                <div class="target-type">{{ typeLabel(scope.row.targetType) }}</div>
                <div class="target-id">目标 ID：{{ scope.row.targetId }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="评分" width="100">
            <template #default="scope">
              <span class="score-pill">{{ scope.row.score || '-' }} 分</span>
            </template>
          </el-table-column>
          <el-table-column label="内容" min-width="260">
            <template #default="scope">
              <div class="content-cell">{{ scope.row.content }}</div>
            </template>
          </el-table-column>
          <el-table-column label="商家回复" min-width="260">
            <template #default="scope">
              <div v-if="scope.row.replyContent" class="reply-box">
                {{ scope.row.replyContent }}
              </div>
              <div v-else class="empty-reply">暂无回复</div>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="时间" width="180" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="scope">
              <el-button size="small" type="danger" plain @click="deleteComment(scope.row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-else class="empty-state">
        <div class="empty-title">暂无评价记录</div>
        <div class="empty-text">你可以在酒店、景点、餐饮等详情页提交评价后，这里会自动展示记录。</div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import api from '../api';

const comments = ref([]);

const typeLabelMap = {
  HOTEL: '酒店',
  SCENIC: '景点',
  RESTAURANT: '餐饮',
  FLIGHT: '航班',
  TRAIN: '火车'
};

const typeLabel = (type) => typeLabelMap[type] || type || '其他';

const repliedCount = computed(() => comments.value.filter((c) => c.replyContent).length);
const pendingCount = computed(() => comments.value.filter((c) => !c.replyContent).length);

const loadComments = async () => {
  try {
    const res = await api.get('/comments/my');
    if (res.data.code === 0) {
      comments.value = res.data.data;
    }
  } catch (e) {
    if (e?.response?.status === 401) {
      ElMessage.error('登录已失效，请重新登录');
    }
  }
};

const deleteComment = async (row) => {
  try {
    await api.delete(`/comments/my/${row.id}`);
    ElMessage.success('已删除评价');
    await loadComments();
  } catch {
    ElMessage.error('删除失败');
  }
};

onMounted(() => {
  loadComments();
});
</script>

<style scoped>
.page-shell {
  width: 70%;
  max-width: 1100px;
  margin: 24px auto 40px;
}
.page-card {
  border: none;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0,0,0,0.08);
}
.page-header {
  padding: 20px 20px 16px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  background: linear-gradient(135deg, #f7fbff 0%, #ffffff 100%);
  border-bottom: 1px solid #eef3f8;
}
.page-kicker {
  font-size: 12px;
  color: #7a8798;
  letter-spacing: 0.6px;
  text-transform: uppercase;
  margin-bottom: 6px;
}
.page-title {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  color: #1a1a1a;
}
.page-subtitle {
  margin-top: 6px;
  color: #667085;
  font-size: 13px;
  line-height: 1.7;
}
.header-badge {
  padding: 8px 12px;
  border-radius: 999px;
  background: #1e73be;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
}
.stats-grid {
  padding: 16px 20px 0;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}
.stat-card {
  border: 1px solid #eef3f8;
  border-radius: 14px;
  padding: 14px;
  background: #fff;
}
.stat-card.accent {
  background: linear-gradient(135deg, #eef6ff 0%, #ffffff 100%);
}
.stat-label {
  font-size: 12px;
  color: #7a8798;
}
.stat-value {
  margin-top: 8px;
  font-size: 24px;
  font-weight: 800;
  color: #1a1a1a;
}
.table-shell {
  padding: 12px 12px 18px;
}
.comment-table {
  margin: 8px 0 4px;
}
.comment-table :deep(.el-table__header th) {
  background: #f7fbff;
  color: #1a1a1a;
  font-weight: 700;
}
.target-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.target-type {
  font-weight: 700;
  color: #1e73be;
}
.target-id {
  font-size: 12px;
  color: #7a8798;
}
.score-pill {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 999px;
  background: #eef6ff;
  color: #1e73be;
  font-weight: 700;
}
.content-cell {
  color: #1a1a1a;
  line-height: 1.7;
}
.reply-box {
  padding: 10px 12px;
  border-radius: 12px;
  background: #f7fbff;
  border: 1px solid #e4eef9;
  color: #50627a;
  line-height: 1.7;
}
.empty-reply {
  color: #9aa0a6;
}
.empty-state {
  padding: 34px 20px 40px;
  text-align: center;
}
.empty-title {
  font-size: 18px;
  font-weight: 800;
  color: #1a1a1a;
}
.empty-text {
  margin-top: 6px;
  color: #667085;
  font-size: 13px;
}
@media (max-width: 1100px) {
  .page-shell {
    width: 92%;
  }
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>

