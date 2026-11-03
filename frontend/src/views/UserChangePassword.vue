<template>
  <el-card style="max-width: 420px; margin: 40px auto">
    <h2 style="margin-bottom: 16px">修改密码</h2>
    <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
      <el-form-item label="原密码" prop="oldPassword">
        <el-input v-model="form.oldPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="form.newPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input v-model="form.confirmPassword" type="password" show-password />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSubmit" :loading="submitting">提交</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import api from '../api';

const router = useRouter();

const formRef = ref();
const form = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});
const submitting = ref(false);

const validateConfirm = (rule, value, callback) => {
  if (value !== form.value.newPassword) {
    callback(new Error('两次输入的新密码不一致'));
  } else {
    callback();
  }
};

const rules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' },
  ],
};

const onSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return;
    submitting.value = true;
    try {
      const res = await api.put('/users/password', null, {
        params: {
          oldPassword: form.value.oldPassword,
          newPassword: form.value.newPassword,
        },
      });
      if (res.data.code === 0) {
        ElMessage.success('密码修改成功，请重新登录');
        // 清理本地登录信息并跳转登录页
        localStorage.removeItem('token');
        localStorage.removeItem('role');
        router.push('/login');
      } else {
        ElMessage.error(res.data.message || '修改失败');
      }
    } catch (e) {
      ElMessage.error('修改失败，请稍后重试');
    } finally {
      submitting.value = false;
    }
  });
};
</script>

