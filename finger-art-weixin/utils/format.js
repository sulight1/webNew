const config = require('./config');

function getMediaBase() {
  return config.resolveMediaBase().replace(/\/$/, '');
}

/** 对路径各段做 encodeURIComponent，避免中文文件名在真机 <image> 白屏 */
function encodeUrlPath(url) {
  try {
    const qIndex = url.indexOf('?');
    const hIndex = url.indexOf('#');
    let cut = url.length;
    if (qIndex >= 0) cut = Math.min(cut, qIndex);
    if (hIndex >= 0) cut = Math.min(cut, hIndex);
    const basePart = url.slice(0, cut);
    const suffix = url.slice(cut);
    const m = basePart.match(/^(https?:\/\/[^/]+)(\/.*)?$/);
    if (!m) return url;
    const path = m[2] || '';
    const encoded = path
      .split('/')
      .map((seg) => {
        if (!seg) return seg;
        try {
          return encodeURIComponent(decodeURIComponent(seg));
        } catch (e) {
          return encodeURIComponent(seg);
        }
      })
      .join('/');
    return m[1] + encoded + suffix;
  } catch (e) {
    return url;
  }
}

function formatImageUrl(url) {
  if (!url || typeof url !== 'string') return '';
  try {
    const base = getMediaBase();
    let resolved = url;
    if (url.startsWith('http://localhost') || url.startsWith('http://127.0.0.1')) {
      resolved = url.replace(/^http:\/\/(localhost|127\.0\.0\.1)(:\d+)?/, base);
    } else if (url.startsWith('http://') || url.startsWith('https://')) {
      resolved = url;
    } else if (url.startsWith('/')) {
      resolved = base + url;
    } else if (url.startsWith('data:')) {
      return url;
    } else {
      return url;
    }
    return encodeUrlPath(resolved);
  } catch (e) {
    return url;
  }
}

function formatMoney(v) {
  const n = Number(v);
  if (Number.isNaN(n)) return '0.00';
  return (Math.round(n * 100) / 100).toFixed(2);
}

function orderStatusText(status, record) {
  if (record && record.cancelRequestStatus === 'PENDING') return '取消待确认';
  const ready = record && record.productType === 'READY_MADE';
  const map = {
    PENDING_CONFIRM: '待确认',
    PENDING_PAY: ready ? '待付全款' : '待付定金',
    PRODUCING: ready ? '待发货' : '制作中',
    PENDING_SHIP: '待发货',
    PENDING_ACCEPT: '待收货',
    PENDING_BALANCE: '待付尾款',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    DISPUTED: '纠纷中',
    HALF_FINISHED_CONFIRM: ready ? '待发货' : '半成品待确认',
    RECEIVED: '待验收',
  };
  return map[status] || status;
}

const ARTISAN_ORDER_STATUS_ORDER = [
  'PENDING_CONFIRM',
  'PENDING_PAY',
  'PRODUCING',
  'HALF_FINISHED_CONFIRM',
  'PENDING_SHIP',
  'PENDING_ACCEPT',
  'PENDING_BALANCE',
  'COMPLETED',
  'DISPUTED',
  'CANCELLED',
  'RECEIVED',
];

const ARTISAN_ANALYTICS_STATUS_LABEL = {
  PENDING_CONFIRM: '待确认接单',
  PENDING_PAY: '待买家付款',
  PRODUCING: '制作中',
  HALF_FINISHED_CONFIRM: '待确认半成品',
  PENDING_SHIP: '待发货',
  PENDING_ACCEPT: '待买家收货',
  PENDING_BALANCE: '待买家付尾款',
  COMPLETED: '已完成',
  DISPUTED: '纠纷中',
  CANCELLED: '已取消',
  RECEIVED: '待买家验收',
};

const ORDER_STATUS_VISUAL = {
  PENDING_CONFIRM: { icon: '📋', color: '#ad6882', bg: '#fdf2f6' },
  PENDING_PAY: { icon: '💳', color: '#c45c8a', bg: '#fff0f6' },
  PRODUCING: { icon: '🎨', color: '#eb2f96', bg: '#fff0f6' },
  HALF_FINISHED_CONFIRM: { icon: '🧵', color: '#d946a6', bg: '#fdf2f8' },
  PENDING_SHIP: { icon: '📦', color: '#db2777', bg: '#fce7f3' },
  PENDING_ACCEPT: { icon: '🚚', color: '#be185d', bg: '#fdf2f8' },
  PENDING_BALANCE: { icon: '💰', color: '#9d174d', bg: '#fce7f3' },
  RECEIVED: { icon: '🔍', color: '#831843', bg: '#fdf2f8' },
  COMPLETED: { icon: '✅', color: '#5a9a7a', bg: '#f0faf4' },
  DISPUTED: { icon: '⚠️', color: '#c2410c', bg: '#fff7ed' },
  CANCELLED: { icon: '⏸', color: '#8c8c8c', bg: '#fafafa' },
};

function getOrderStatusVisual(status) {
  return ORDER_STATUS_VISUAL[status] || { icon: '📌', color: '#eb2f96', bg: '#fff0f6' };
}

function artisanOrderStatusText(status, record) {
  if (record && record.cancelRequestStatus === 'PENDING') return '取消待确认';
  const ready = record && record.productType === 'READY_MADE';
  const map = {
    PENDING_CONFIRM: '待确认接单',
    PENDING_PAY: ready ? '待买家付全款' : '待买家付定金',
    PRODUCING: ready ? '待发货' : '制作中',
    HALF_FINISHED_CONFIRM: '待确认半成品',
    PENDING_SHIP: '待发货',
    PENDING_ACCEPT: ready ? '待买家收货' : '待买家验收',
    PENDING_BALANCE: '待买家付尾款',
    COMPLETED: '已完成',
    DISPUTED: '纠纷中',
    CANCELLED: '已取消',
    RECEIVED: '待买家验收',
  };
  return map[status] || ARTISAN_ANALYTICS_STATUS_LABEL[status] || status;
}

function buildOrderStatusDistribution(ordersByStatus) {
  const entries = Object.keys(ordersByStatus || {})
    .filter((key) => Number(ordersByStatus[key]) > 0)
    .map((key) => {
      const visual = getOrderStatusVisual(key);
      return {
        key,
        label: ARTISAN_ANALYTICS_STATUS_LABEL[key] || key,
        count: Number(ordersByStatus[key]),
        color: visual.color,
        bg: visual.bg,
        icon: visual.icon,
        percent: 0,
      };
    });

  entries.sort((a, b) => {
    const ia = ARTISAN_ORDER_STATUS_ORDER.indexOf(a.key);
    const ib = ARTISAN_ORDER_STATUS_ORDER.indexOf(b.key);
    return (ia === -1 ? 999 : ia) - (ib === -1 ? 999 : ib);
  });

  const maxCount = Math.max(...entries.map((o) => o.count), 1);
  entries.forEach((o) => {
    o.percent = Math.round((o.count / maxCount) * 100);
  });
  return entries;
}

function hasCompleteShippingAddress(user) {
  if (!user) return false;
  return !!(String(user.shippingName || '').trim()
    && String(user.shippingPhone || '').trim()
    && String(user.shippingAddress || '').trim());
}

module.exports = {
  formatImageUrl,
  formatMoney,
  orderStatusText,
  artisanOrderStatusText,
  buildOrderStatusDistribution,
  hasCompleteShippingAddress,
};
