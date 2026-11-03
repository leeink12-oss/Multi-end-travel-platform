<template>
  <el-row justify="center" style="margin-top: 80px">
    <el-col :span="8">
      <el-card>
        <h3 style="text-align: center; margin-bottom: 16px">注册</h3>
        <el-form :model="form" @submit.prevent="onSubmit">
          <el-form-item label="注册类型">
            <el-radio-group v-model="form.type">
              <el-radio label="USER">普通用户</el-radio>
              <el-radio label="MERCHANT">商家</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="form.type === 'MERCHANT'" label="商家类型">
            <el-radio-group v-model="form.merchantType">
              <el-radio label="TRAVEL">旅游商家（酒店/景点）</el-radio>
              <el-radio label="TRANSPORT">出行商家（飞机/火车）</el-radio>
              <el-radio label="CATERING">餐饮商家（餐厅/门店）</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="用户名">
            <el-input v-model="form.username" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" />
          </el-form-item>
          <el-form-item label="昵称 / 商家名">
            <el-input v-model="form.nickname" />
          </el-form-item>
          <el-form-item v-if="form.type === 'MERCHANT'" label="联系电话">
            <el-input v-model="form.contactPhone" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="onSubmit" style="width: 100%">注册</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
import { reactive } from 'vue';
import { ElMessage } from 'element-plus';
import api from '../api';

const form = reactive({
  type: 'USER',
  username: '',
  password: '',
  nickname: '',
  name: '',
  contactPhone: '',
  merchantType: 'TRAVEL'
});

const onSubmit = async () => {
  try {
    if (form.type === 'USER') {
      const payload = {
        username: form.username,
        password: form.password,
        nickname: form.nickname
      };
      const res = await api.post('/auth/register', payload);
      if (res.data.code === 0) {
        ElMessage.success('用户注册成功，请登录');
        window.location.href = '/login';
      } else {
        ElMessage.error(res.data.message || '注册失败');
      }
    } else {
      const payload = {
        username: form.username,
        password: form.password,
        name: form.nickname || form.username,
        contactPhone: form.contactPhone,
        type: form.merchantType
      };
      const res = await api.post('/merchants/register', payload);
      if (res.data.code === 0) {
        ElMessage.success('商家注册成功，等待管理员审核');
        window.location.href = '/login';
      } else {
        ElMessage.error(res.data.message || '注册失败');
      }
    }
  } catch (e) {
    ElMessage.error('注册失败');
  }
};
</script>

