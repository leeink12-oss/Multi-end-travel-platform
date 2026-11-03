import { createRouter, createWebHistory } from 'vue-router';
import UserHome from '../views/UserHome.vue';
import ScenicDetail from '../views/ScenicDetail.vue';
import HotelDetail from '../views/HotelDetail.vue';
import Login from '../views/Login.vue';
import Register from '../views/Register.vue';
import MerchantDashboard from '../views/MerchantDashboard.vue';
import MerchantScenic from '../views/MerchantScenic.vue';
import MerchantHotel from '../views/MerchantHotel.vue';
import MerchantFlight from '../views/MerchantFlight.vue';
import MerchantTrain from '../views/MerchantTrain.vue';
import AdminDashboard from '../views/AdminDashboard.vue';
import UserOrders from '../views/UserOrders.vue';
import UserFavorites from '../views/UserFavorites.vue';
import UserComments from '../views/UserComments.vue';
import UserChangePassword from '../views/UserChangePassword.vue';
import UserFeedback from '../views/UserFeedback.vue';
import UserCoupons from '../views/UserCoupons.vue';
import NotificationCenter from '../views/NotificationCenter.vue';
import SupportChat from '../views/SupportChat.vue';
import FlightList from '../views/FlightList.vue';
import FlightDetail from '../views/FlightDetail.vue';
import TrainList from '../views/TrainList.vue';
import TrainDetail from '../views/TrainDetail.vue';
import TransportBooking from '../views/TransportBooking.vue';
import DiningBooking from '../views/DiningBooking.vue';
import MerchantRestaurant from '../views/MerchantRestaurant.vue';
import MerchantNotificationCenter from '../views/MerchantNotificationCenter.vue';
import RestaurantDetail from '../views/RestaurantDetail.vue';
import UserDiningOrders from '../views/UserDiningOrders.vue';

const routes = [
  { path: '/', name: 'user-home', component: UserHome },
  { path: '/dining', name: 'dining-booking', component: DiningBooking },
  { path: '/dining/:id', name: 'restaurant-detail', component: RestaurantDetail, props: true },
  { path: '/dining-orders', name: 'user-dining-orders', component: UserDiningOrders },
  { path: '/transport', name: 'transport-booking', component: TransportBooking },
  { path: '/flights', name: 'flight-list', component: FlightList },
  { path: '/trains', name: 'train-list', component: TrainList },
  { path: '/orders', name: 'user-orders', component: UserOrders },
  { path: '/favorites', name: 'user-favorites', component: UserFavorites },
  { path: '/comments', name: 'user-comments', component: UserComments },
  { path: '/user/coupons', name: 'user-coupons', component: UserCoupons },
  { path: '/notifications', name: 'notifications', component: NotificationCenter },
  { path: '/user/change-password', name: 'user-change-password', component: UserChangePassword },
  { path: '/user/feedback', name: 'user-feedback', component: UserFeedback },
  { path: '/support', name: 'support-chat', component: SupportChat },
  { path: '/login', name: 'login', component: Login },
  { path: '/register', name: 'register', component: Register },
  { path: '/scenic/:id', name: 'scenic-detail', component: ScenicDetail, props: true },
  { path: '/hotel/:id', name: 'hotel-detail', component: HotelDetail, props: true },
  { path: '/flight/:id', name: 'flight-detail', component: FlightDetail, props: true },
  { path: '/train/:id', name: 'train-detail', component: TrainDetail, props: true },
  { path: '/merchant', name: 'merchant', component: MerchantDashboard },
  { path: '/merchant-scenic', name: 'merchant-scenic', component: MerchantScenic },
  { path: '/merchant-hotel', name: 'merchant-hotel', component: MerchantHotel },
  { path: '/merchant-flight', name: 'merchant-flight', component: MerchantFlight },
  { path: '/merchant-train', name: 'merchant-train', component: MerchantTrain },
  { path: '/merchant-restaurant', name: 'merchant-restaurant', component: MerchantRestaurant },
  { path: '/merchant-notifications', name: 'merchant-notifications', component: MerchantNotificationCenter },
  { path: '/admin', name: 'admin', component: AdminDashboard }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 路由守卫：未登录时统一跳转到登录页，根据角色限制访问
router.beforeEach((to, from, next) => {
  const needAuthPaths = [
    '/merchant',
    '/merchant-scenic',
    '/merchant-hotel',
    '/merchant-flight',
    '/merchant-train',
    '/merchant-restaurant',
    '/admin',
    '/orders',
    '/favorites',
    '/comments',
    '/user/coupons',
    '/user/change-password',
    '/user/feedback',
    '/dining-orders',
  ];
  const token = localStorage.getItem('token');
  const role = localStorage.getItem('role');
  const merchantType = localStorage.getItem('merchantType');

  // 未登录且访问的不是登录/注册页时，强制跳转到登录页
  if (!token && to.path !== '/login' && to.path !== '/register') {
    next({ path: '/login', query: { redirect: to.fullPath } });
    return;
  }

  if (needAuthPaths.includes(to.path) && !token) {
    next({ path: '/login', query: { redirect: to.fullPath } });
    return;
  }

  if ((to.path === '/merchant' || to.path === '/merchant-scenic' || to.path === '/merchant-hotel' || to.path === '/merchant-flight' || to.path === '/merchant-train' || to.path === '/merchant-restaurant') && role !== 'MERCHANT') {
    next('/');
    return;
  }
  if (role === 'MERCHANT') {
    // /merchant 统一跳转到对应商家首页，避免首次登录串台
    if (to.path === '/merchant') {
      if (merchantType === 'TRANSPORT') {
        next('/merchant-flight');
        return;
      }
      if (merchantType === 'CATERING') {
        next('/merchant-restaurant');
        return;
      }
      next('/merchant-scenic');
      return;
    }

    // 按商家类型限制可访问页面，防止串台
    const travelPaths = ['/merchant-scenic', '/merchant-hotel'];
    const transportPaths = ['/merchant-flight', '/merchant-train'];
    const cateringPaths = ['/merchant-restaurant'];

    if (merchantType === 'TRANSPORT' && travelPaths.includes(to.path)) {
      next('/merchant-flight');
      return;
    }
    if (merchantType === 'CATERING' && (travelPaths.includes(to.path) || transportPaths.includes(to.path))) {
      next('/merchant-restaurant');
      return;
    }
    if ((merchantType === 'TRAVEL' || !merchantType) && (transportPaths.includes(to.path) || cateringPaths.includes(to.path))) {
      next('/merchant-scenic');
      return;
    }
  }
  if (to.path === '/admin' && role !== 'ADMIN') {
    next('/');
    return;
  } else {
    next();
  }
});

export default router;

