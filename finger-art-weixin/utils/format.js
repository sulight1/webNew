const { API_BASE } = require('./config');

function formatImageUrl(url) {
  if (!url) return '';
  if (url.startsWith('http://') || url.startsWith('https://')) return url;
  if (url.startsWith('/')) return API_BASE + url;
  return url;
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
  };
  return map[status] || status;
}

module.exports = {
  formatImageUrl,
  formatMoney,
  orderStatusText,
};
