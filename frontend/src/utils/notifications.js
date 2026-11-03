const STORAGE_KEY = 'travel-platform-notifications';
const SNAPSHOT_KEY = 'travel-platform-order-snapshot';
const MERCHANT_SNAPSHOT_KEY = 'travel-platform-merchant-order-snapshot';

export function getNotifications() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]');
  } catch {
    return [];
  }
}

export function saveNotifications(list) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(list));
  window.dispatchEvent(new Event('notifications-changed'));
}

export function addNotification(notification) {
  const list = getNotifications();
  list.unshift({ id: `${Date.now()}-${Math.random().toString(16).slice(2)}`, read: false, createTime: new Date().toISOString(), ...notification });
  saveNotifications(list.slice(0, 100));
}

export function markAllNotificationsRead() {
  const list = getNotifications().map((item) => ({ ...item, read: true }));
  saveNotifications(list);
}

export function markNotificationRead(id) {
  const list = getNotifications().map((item) => (item.id === id ? { ...item, read: true } : item));
  saveNotifications(list);
}

export function unreadNotificationCount() {
  return getNotifications().filter((item) => !item.read).length;
}

export function readUserOrderSnapshot() {
  try {
    return JSON.parse(localStorage.getItem(SNAPSHOT_KEY) || '{}');
  } catch {
    return {};
  }
}

export function saveUserOrderSnapshot(snapshot) {
  localStorage.setItem(SNAPSHOT_KEY, JSON.stringify(snapshot));
}

export function readMerchantOrderSnapshot() {
  try {
    return JSON.parse(localStorage.getItem(MERCHANT_SNAPSHOT_KEY) || '{}');
  } catch {
    return {};
  }
}

export function saveMerchantOrderSnapshot(snapshot) {
  localStorage.setItem(MERCHANT_SNAPSHOT_KEY, JSON.stringify(snapshot));
}
