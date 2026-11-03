<template>
  <div class="layout">
    <el-container>
      <el-header class="header">
        <div class="header-inner">
          <div class="logo">GO anywhere.com</div>
          <div class="nav">
          <template v-if="!isLoggedIn">
            <el-button type="text" @click="goTo('/login')">登录</el-button>
            <el-button type="text" @click="goTo('/register')">注册</el-button>
          </template>
          <template v-else>
            <div class="nav-left">
              <span class="role-label">{{ roleLabel }}</span>
              <el-divider direction="vertical" />
              <template v-if="role === 'USER'">
                <el-button type="text" @click="goTo('/')">用户端</el-button>
                <el-button type="text" @click="goTo('/notifications')">
                  <OrderBadge :count="notificationCount">
                    <span>通知中心</span>
                  </OrderBadge>
                </el-button>
                <el-button type="text" @click="goTo('/dining')">餐饮预订</el-button>
                <el-button type="text" @click="goTo('/transport')">出行预定</el-button>
                <el-button type="text" @click="goTo('/support')">平台客服</el-button>
              </template>
              <template v-else-if="role === 'MERCHANT'">
        <template v-if="merchantType === 'TRANSPORT'">
          <el-button type="text" @click="goTo('/merchant-flight')">航班管理</el-button>
          <el-button type="text" @click="goTo('/merchant-train')">动车管理</el-button>
        </template>
        <template v-else-if="merchantType === 'CATERING'">
          <el-button type="text" @click="goTo('/merchant-restaurant')">餐饮管理</el-button>
          <el-button type="text" @click="goTo('/merchant-notifications')">
            <OrderBadge :count="merchantNotificationCount">
              <span>通知中心</span>
            </OrderBadge>
          </el-button>
        </template>
        <template v-else-if="merchantType === 'TRANSPORT'">
          <el-button type="text" @click="goTo('/merchant-flight')">航班管理</el-button>
          <el-button type="text" @click="goTo('/merchant-train')">动车管理</el-button>
          <el-button type="text" @click="goTo('/merchant-notifications')">
            <OrderBadge :count="merchantNotificationCount">
              <span>通知中心</span>
            </OrderBadge>
          </el-button>
        </template>
        <template v-else>
          <el-button type="text" @click="goTo('/merchant-scenic')">景点管理</el-button>
          <el-button type="text" @click="goTo('/merchant-hotel')">酒店管理</el-button>
          <el-button type="text" @click="goTo('/merchant-notifications')">
            <OrderBadge :count="merchantNotificationCount">
              <span>通知中心</span>
            </OrderBadge>
          </el-button>
        </template>
              </template>
              <template v-else-if="role === 'ADMIN'">
                <el-button type="text" @click="goTo('/admin')">管理后台</el-button>
              </template>
            </div>
            <div class="nav-right">
              <input
                ref="avatarInputRef"
                type="file"
                accept="image/*"
                style="display: none"
                @change="onAvatarFileChange"
              />
              <el-dropdown trigger="click">
                <span class="el-dropdown-link">
                  <el-avatar
                    :src="userInfo.avatar"
                    size="small"
                    style="margin-right: 6px; cursor: pointer"
                    @click.stop="onAvatarClick"
                  >
                    {{ userInitial }}
                  </el-avatar>
                  <span>{{ userInfo.username || '账号' }}</span>
                  <span class="dropdown-arrow">▼</span>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <template v-if="role === 'USER'">
                      <el-dropdown-item @click="goTo('/dining')">餐饮预订</el-dropdown-item>
                      <el-dropdown-item @click="goTo('/transport')">出行预定</el-dropdown-item>
                      <el-dropdown-item @click="goTo('/orders')">我的订单</el-dropdown-item>
                      <el-dropdown-item @click="goTo('/dining-orders')">我的餐饮预订</el-dropdown-item>
                      <el-dropdown-item @click="goTo('/favorites')">我的收藏</el-dropdown-item>
                      <el-dropdown-item @click="goTo('/user/coupons')">我的优惠券</el-dropdown-item>
                      <el-dropdown-item @click="goTo('/notifications')">
                        <OrderBadge :count="notificationCount">通知中心</OrderBadge>
                      </el-dropdown-item>
                      <el-dropdown-item @click="goTo('/comments')">我的评价</el-dropdown-item>
                      <el-dropdown-item divided />
                    </template>
                    <el-dropdown-item @click="goTo('/user/change-password')">
                      修改密码
                    </el-dropdown-item>
                    <el-dropdown-item @click="goTo('/user/feedback')">
                      评价软件
                    </el-dropdown-item>
                    <el-dropdown-item divided @click="confirmLogoutAndDelete">
                      注销账户
                    </el-dropdown-item>
                    <el-dropdown-item @click="logout">
                      退出登录
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
          </div>
        </div>
      </el-header>
      <el-main>
        <div :class="contentWrapperClass">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessageBox, ElMessage } from 'element-plus';
import api from './api';
import OrderBadge from './components/OrderBadge.vue';
import { unreadNotificationCount, getNotifications } from './utils/notifications';

const router = useRouter();
const route = useRoute();

const isLoggedIn = ref(!!localStorage.getItem('token'));
const role = ref(localStorage.getItem('role'));
const merchantType = ref(localStorage.getItem('merchantType'));
const userInfo = ref({ username: '', avatar: '' });
const avatarInputRef = ref(null);

const syncAuthFromStorage = async () => {
  isLoggedIn.value = !!localStorage.getItem('token');
  role.value = localStorage.getItem('role');
  merchantType.value = localStorage.getItem('merchantType');
  await refreshUnreadCount();
  if (isLoggedIn.value) {
    try {
      const res = await api.get('/auth/me');
      if (res.data && res.data.data) {
        userInfo.value = {
          username: res.data.data.username,
          avatar: res.data.data.avatar || '',
        };
      }
      // 每次同步都实时拉取商家类型，避免 localStorage 残留导致菜单串号
      if (role.value === 'MERCHANT') {
        try {
          const mRes = await api.get('/merchants/me');
          if (mRes.data.code === 0 && mRes.data.data?.type) {
            merchantType.value = mRes.data.data.type;
            localStorage.setItem('merchantType', mRes.data.data.type);
          } else {
            merchantType.value = 'TRAVEL';
            localStorage.setItem('merchantType', 'TRAVEL');
          }
        } catch {
          merchantType.value = 'TRAVEL';
          localStorage.setItem('merchantType', 'TRAVEL');
        }
      } else {
        merchantType.value = null;
        localStorage.removeItem('merchantType');
      }
    } catch (e) {
      // token 失效时自动退出到登录
      logout();
    }
  } else {
    userInfo.value = { username: '', avatar: '' };
  }
};

onMounted(() => {
  window.addEventListener('auth-changed', syncAuthFromStorage);
  // 首次加载时同步一次
  syncAuthFromStorage();
});

onBeforeUnmount(() => {
  window.removeEventListener('auth-changed', syncAuthFromStorage);
});

const notificationCount = ref(0);
const merchantNotificationCount = ref(0);

const refreshUnreadCount = async () => {
  try {
    const res = await api.get('/messages/unread-count');
    const count = res.data.code === 0 ? Number(res.data.data || 0) : 0;
    notificationCount.value = count;
    merchantNotificationCount.value = count;
  } catch {
    notificationCount.value = 0;
    merchantNotificationCount.value = 0;
  }
};

const roleLabel = computed(() => {
  if (role.value === 'ADMIN') return '管理员端';
  if (role.value === 'MERCHANT') return '商家端';
  if (role.value === 'USER') return '用户端';
  return '';
});

// 用户端除首页外统一居中留白（左右各约 15%），与首页内容区一致
const contentWrapperClass = computed(() => {
  const isUser = role.value === 'USER';
  const isHome = route.path === '/';
  return isUser && !isHome ? 'user-content-wrapper' : '';
});

window.addEventListener('notifications-changed', () => {
  refreshUnreadCount();
});

const userInitial = computed(() => {
  if (userInfo.value.username) {
    return userInfo.value.username.charAt(0).toUpperCase();
  }
  return 'U';
});

const goTo = (path) => {
  router.push(path);
};

const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('role');
  localStorage.removeItem('merchantType');
  isLoggedIn.value = false;
  role.value = null;
  merchantType.value = null;
  userInfo.value = { username: '', avatar: '' };
  notificationCount.value = 0;
  merchantNotificationCount.value = 0;
  router.push('/login');
};

const confirmLogoutAndDelete = () => {
  ElMessageBox.confirm('注销账户将删除当前用户及其所有订单、收藏和评价，确定继续？', '确认注销', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        await api.delete('/users/me');
        ElMessage.success('账户已注销');
        logout();
      } catch (e) {
        ElMessage.error('注销失败，请稍后重试');
      }
    })
    .catch(() => {});
};

const onAvatarClick = () => {
  if (avatarInputRef.value) {
    avatarInputRef.value.click();
  }
};

const onAvatarFileChange = async (event) => {
  const file = event.target.files && event.target.files[0];
  if (!file) return;
  const formData = new FormData();
  formData.append('file', file);
  try {
    const uploadRes = await api.post('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    if (uploadRes.data.code !== 0) {
      ElMessage.error(uploadRes.data.message || '上传失败');
      return;
    }
    const url = uploadRes.data.data;
    const avatarRes = await api.put('/users/avatar', null, {
      params: { avatarUrl: url },
    });
    if (avatarRes.data.code === 0) {
      userInfo.value = { ...userInfo.value, avatar: url };
      ElMessage.success('头像已更新');
    } else {
      ElMessage.error(avatarRes.data.message || '头像更新失败');
    }
  } catch (e) {
    ElMessage.error('上传头像失败，请稍后重试');
  } finally {
    if (event.target) {
      event.target.value = '';
    }
  }
};
</script>

<style scoped>
.layout {
  min-height: 100vh;
  background: linear-gradient(180deg, #f6f9fc 0%, #f2f6fb 100%);
}
.header {
  background: linear-gradient(135deg, #1e73be 0%, #2f8de4 100%);
  color: #fff;
  box-shadow: 0 8px 24px rgba(30, 115, 190, 0.16);
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-inner {
  max-width: 1300px;
  width: 70%;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  gap: 16px;
}
.logo {
  font-size: 30px;
  font-weight: 900;
  letter-spacing: 0.2px;
  white-space: nowrap;
}
.nav {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}
.nav-left {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}
.nav-left .el-button {
  color: #fff;
  margin-left: 10px;
  border: 0;
}
.nav-left .el-button:hover {
  opacity: 0.9;
}
.role-label {
  font-weight: 700;
  margin-right: 8px;
}
.nav-right {
  margin-left: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.nav-right span {
  color: #ffffff;
}
.el-dropdown-link {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(8px);
}
.dropdown-arrow {
  margin-left: 4px;
  font-size: 10px;
  opacity: 0.9;
}
.layout :deep(.el-main) {
  padding: 0;
}
.user-content-wrapper {
  width: 70%;
  max-width: 1100px;
  margin: 24px auto;
}
.layout :deep(.el-card) {
  border-radius: 18px;
  border: 1px solid rgba(30, 115, 190, 0.08);
  box-shadow: 0 10px 30px rgba(17, 38, 85, 0.06);
}
.layout :deep(.el-button--primary) {
  box-shadow: 0 8px 18px rgba(30, 115, 190, 0.16);
}
.layout :deep(.el-dialog) {
  border-radius: 18px;
  overflow: hidden;
}
.layout :deep(.el-table) {
  border-radius: 14px;
  overflow: hidden;
}
</style>

