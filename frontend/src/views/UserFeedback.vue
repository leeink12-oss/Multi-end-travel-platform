<template>
  <el-card style="max-width: 600px; margin: 40px auto">
    <h2 style="margin-bottom: 16px">评价软件</h2>
    <p style="margin-bottom: 12px; color: #666">
      欢迎留下你对“哪都去 · 旅游服务平台”的体验评价和改进建议。
    </p>
    <el-form :model="form" label-width="80px">
      <el-form-item label="评分">
        <el-rate v-model="form.score" />
      </el-form-item>
      <el-form-item label="评价内容">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="5"
          placeholder="可以从界面体验、功能逻辑、使用流畅度等方面给出评价～"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSubmit">提交</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';

const form = ref({
  score: 5,
  content: '',
});

const onSubmit = () => {
  if (!form.value.content) {
    ElMessage.warning('请先填写评价内容');
    return;
  }
  // 当前不保存到后端，仅做前端演示
  ElMessage.success('感谢你的评价，我们会不断改进体验！');
  form.value = {
    score: 5,
    content: '',
  };
};
</script>

