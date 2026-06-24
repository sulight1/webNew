const { orderApi, userApi } = require('../../services/api');
const auth = require('../../utils/auth');
const cartUtil = require('../../utils/cart');
const { formatImageUrl, hasCompleteShippingAddress } = require('../../utils/format');

const COIN_CHANNEL = 'ZAOWU_COIN';

Page({
  data: {
    items: [],
    buyer: null,
    totalAmount: '0.00',
    coinBalance: '0.00',
    loading: true,
    submitting: false,
  },

  onLoad() {
    this.loadCheckout();
  },

  loadReadyItems() {
    return cartUtil.loadItems().filter((item) => item.type !== 'CUSTOMIZABLE');
  },

  async loadCheckout() {
    if (!auth.isLoggedIn()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }

    const readyItems = this.loadReadyItems();
    if (readyItems.length <= 1) {
      if (readyItems.length === 1) {
        const item = readyItems[0];
        wx.redirectTo({ url: `/pages/checkout/checkout?productId=${item.productId}&qty=${item.quantity}` });
        return;
      }
      wx.showToast({ title: '没有可结算的成品', icon: 'none' });
      setTimeout(() => wx.navigateBack(), 800);
      return;
    }

    this.setData({ loading: true });
    try {
      const user = auth.getUser();
      let buyer = user;
      try {
        buyer = await userApi.getProfile(user.id);
        getApp().setUser(buyer);
      } catch (_) {}

      const items = readyItems.map((item) => ({
        ...item,
        image: formatImageUrl(item.image),
        subtotal: (Math.round(Number(item.price) * item.quantity * 100) / 100).toFixed(2),
      }));
      const totalAmount = cartUtil.getTotalAmount(items).toFixed(2);
      const coinBalance = Number(buyer.zaowuBiBalance ?? 0).toFixed(2);

      this.setData({ items, buyer, totalAmount, coinBalance });
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
      setTimeout(() => wx.navigateBack(), 800);
    } finally {
      this.setData({ loading: false });
    }
  },

  goEditAddress() {
    wx.switchTab({ url: '/pages/account/account' });
  },

  goWallet() {
    wx.navigateTo({ url: '/pages/wallet/wallet' });
  },

  async submitPayment() {
    const { items, buyer, totalAmount, coinBalance } = this.data;
    if (!items.length || !buyer) return;

    if (!hasCompleteShippingAddress(buyer)) {
      wx.showModal({
        title: '请先填写收货地址',
        content: '购买成品前请在「我的」页面填写收货地址',
        confirmText: '去填写',
        success: (r) => {
          if (r.confirm) this.goEditAddress();
        },
      });
      return;
    }

    if (Number(coinBalance) < Number(totalAmount)) {
      wx.showModal({
        title: '造物币余额不足',
        content: '请先在钱包充值后再购买',
        confirmText: '去充值',
        success: (r) => {
          if (r.confirm) this.goWallet();
        },
      });
      return;
    }

    this.setData({ submitting: true });
    try {
      wx.showLoading({ title: '支付中' });
      const user = auth.getUser();
      const result = await orderApi.batchCheckout({
        buyerId: user.id,
        buyerName: user.username,
        paymentChannel: COIN_CHANNEL,
        items: items.map((item) => ({
          productId: item.productId,
          quantity: item.quantity,
        })),
      });
      cartUtil.removeItems(items.map((item) => item.productId));
      try {
        const profile = await userApi.getProfile(user.id);
        getApp().setUser(profile);
      } catch (_) {}
      wx.hideLoading();
      wx.showModal({
        title: '支付成功',
        content: `已使用造物币支付 ¥${result.totalAmount}，共生成 ${result.orderCount} 笔订单`,
        showCancel: false,
        success: () => {
          wx.redirectTo({ url: '/pages/orders/orders' });
        },
      });
    } catch (e) {
      wx.hideLoading();
      const msg = e.message || '支付失败';
      if (msg.includes('余额不足')) {
        wx.showModal({
          title: '造物币余额不足',
          content: '请先在钱包充值后再购买',
          confirmText: '去充值',
          success: (r) => {
            if (r.confirm) this.goWallet();
          },
        });
        return;
      }
      wx.showToast({ title: msg, icon: 'none' });
    } finally {
      this.setData({ submitting: false });
    }
  },
});
