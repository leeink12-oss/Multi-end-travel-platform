<template>
  <div class="login-page">
    <div class="login-phone">
      <div class="hero-image-wrap">
        <img
          class="hero-image"
          src="https://images.unsplash.com/photo-1469474968028-56623f02e42e?auto=format&fit=crop&w=1200&q=80"
          alt="旅行欢迎图"
        />
      </div>

      <div class="content">
        <h1 class="title">
          欢迎来到 <span class="brand">哪都去</span>
        </h1>
        <p class="subtitle">
          一站式完成酒店、景点、机票和火车票预订，开启你的下一段旅程。
        </p>

        <el-form class="login-form" :model="form" @submit.prevent="onSubmit">
          <el-form-item>
            <el-input v-model="form.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submitting" @click="onSubmit" class="submit-btn">
              立即登录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="secondary-actions">
          <el-button link type="primary" @click="$router.push('/register')">
            还没有账号？立即注册
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import api from '../api';

const route = useRoute();
const router = useRouter();

const form = reactive({
  username: '',
  password: ''
});
const submitting = ref(false);

const merchantHomePath = (type) => {
  if (type === 'TRANSPORT') return '/merchant-flight';
  if (type === 'CATERING') return '/merchant-restaurant';
  return '/merchant-scenic';
};

const onSubmit = async () => {
  try {
    if (!form.username || !form.password) {
      ElMessage.warning('请输入用户名和密码');
      return;
    }
    submitting.value = true;
    // 先清理旧商家类型缓存，避免切换账号时串号
    localStorage.removeItem('merchantType');
    const res = await api.post('/auth/login', form);
    if (res.data.code === 0) {
      localStorage.setItem('token', res.data.data);
      // 获取当前用户角色信息
      const meRes = await api.get('/auth/me');
      if (meRes.data.code === 0 && meRes.data.data) {
        const role = meRes.data.data.role;
        localStorage.setItem('role', role);
        // 商家补充拉取商家类型（旅游/出行），用于前端菜单与页面展示
        if (role === 'MERCHANT') {
          try {
            const mRes = await api.get('/merchants/me');
            if (mRes.data.code === 0 && mRes.data.data) {
              localStorage.setItem('merchantType', mRes.data.data.type);
            } else {
              localStorage.removeItem('merchantType');
            }
          } catch {
            localStorage.removeItem('merchantType');
          }
        } else {
          localStorage.removeItem('merchantType');
        }
        // 通知根组件更新右上角显示
        window.dispatchEvent(new Event('auth-changed'));
        ElMessage.success('登录成功');
        // 根据角色跳转不同端
        if (role === 'ADMIN') {
          router.push('/admin');
        } else if (role === 'MERCHANT') {
          const type = localStorage.getItem('merchantType');
          router.push(merchantHomePath(type));
        } else {
          const redirect = route.query.redirect || '/';
          router.push(redirect);
        }
      } else {
        ElMessage.error('获取用户信息失败');
      }
    } else {
      ElMessage.error(res.data.message || '登录失败');
    }
  } catch (e) {
    ElMessage.error('登录失败');
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.login-page {
  min-height: calc(100vh - 120px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: #f5f7fb;
}

.login-phone {
  width: 100%;
  max-width: 420px;
  min-height: 760px;
  border-radius: 28px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.12);
  border: 1px solid #eceef4;
  display: flex;
  flex-direction: column;
}

.hero-image-wrap {
  width: 100%;
  overflow: hidden;
}

.hero-image {
  width: 100%;
  height: 280px;
  object-fit: cover;
  clip-path: ellipse(100% 65% at 50% 35%);
}

.content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 24px 28px 30px;
}

.title {
  margin: 0;
  text-align: center;
  font-size: 30px;
  font-weight: 700;
  color: #111827;
}

.brand {
  color: #1e73be;
}

.subtitle {
  margin: 12px auto 24px;
  text-align: center;
  color: #6b7280;
  font-size: 14px;
  line-height: 1.7;
  max-width: 320px;
}

.login-form {
  margin-top: 8px;
}

.submit-btn {
  width: 100%;
  height: 42px;
  border-radius: 10px;
}

.secondary-actions {
  margin-top: 8px;
  text-align: center;
}
</style>

