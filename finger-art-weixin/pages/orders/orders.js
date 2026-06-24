const { orderApi } = require('../../services/api');
const auth = require('../../utils/auth');
const { orderStatusText } = require('../../utils/format');

Page({
  data: {
    orders: [],
    tab: 'buyer',
    loading: false,
  },

  onShow() {
    if (!auth.isLoggedIn()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    this.loadOrders();
  },

  onTabChange(e) {
    this.setData({ tab: e.currentTarget.dataset.tab });
    this.loadOrders();
  },

  async loadOrders() {
    const user = auth.getUser();
    this.setData({ loading: true });
    try {
      const orders = this.data.tab === 'buyer'
        ? await orderApi.getBuyerOrders(user.id)
        : await orderApi.getArtisanOrders(user.id);
      const list = (orders || []).map((o) => ({
        ...o,
        statusLabel: orderStatusText(o.status, o),
        quantity: o.quantity || 1,
      })).sort((a, b) => {
        const timeA = new Date(a.createTime || 0).getTime();
        const timeB = new Date(b.createTime || 0).getTime();
        return timeB - timeA;
      });
      this.setData({ orders: list });
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },

  goDetail(e) {
    wx.navigateTo({ url: `/pages/order-detail/order-detail?id=${e.currentTarget.dataset.id}` });
  },

  goProduct(e) {
    const { productId, orderId } = e.currentTarget.dataset;
    if (!productId) return;
    const query = orderId ? `?fromOrder=${orderId}` : '';
    wx.navigateTo({ url: `/pages/product-detail/product-detail?id=${productId}${query}` });
  },
});
