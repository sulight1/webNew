const { productApi, orderApi, userApi } = require('../../services/api');
const auth = require('../../utils/auth');
const cartUtil = require('../../utils/cart');
const { formatImageUrl, hasCompleteShippingAddress } = require('../../utils/format');

const WECHAT_CHANNEL = 'MOCK_WECHAT';

Page({
  data: {
    product: null,
    buyer: null,
    checkoutQuantity: 1,
    unitPrice: 0,
    totalAmount: 0,
    loading: true,
    submitting: false,
  },

  onLoad(options) {
    this.productId = Number(options.productId);
    const qty = Number(options.qty);
    this.initialQty = Number.isFinite(qty) && qty > 0 ? Math.floor(qty) : 1;
    this.loadCheckout();
  },

  async loadCheckout() {
    if (!auth.isLoggedIn()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    if (!this.productId) {
      wx.showToast({ title: '商品无效', icon: 'none' });
      setTimeout(() => wx.navigateBack(), 800);
      return;
    }

    this.setData({ loading: true });
    try {
      const product = await productApi.getProductById(this.productId);
      if (product.type === 'CUSTOMIZABLE') {
        wx.showToast({ title: '定制商品请直接发起定制', icon: 'none' });
        setTimeout(() => wx.redirectTo({ url: `/pages/product-detail/product-detail?id=${this.productId}` }), 800);
        return;
      }
      if ((product.stock ?? 1) <= 0) {
        wx.showToast({ title: '商品已售罄', icon: 'none' });
        setTimeout(() => wx.navigateBack(), 800);
        return;
      }
      const user = auth.getUser();
      if (product.creatorId === user.id) {
        wx.showToast({ title: '不能购买自己的作品', icon: 'none' });
        setTimeout(() => wx.navigateBack(), 800);
        return;
      }

      product.image = formatImageUrl(product.image);
      let buyer = user;
      try {
        buyer = await userApi.getProfile(user.id);
        getApp().setUser(buyer);
      } catch (_) {}

      const maxStock = Math.max(product.stock ?? 1, 1);
      const checkoutQuantity = Math.min(this.initialQty, maxStock);
      const unitPrice = Number(product.price) || 0;
      const totalAmount = Math.round(unitPrice * checkoutQuantity * 100) / 100;

      this.setData({
        product,
        buyer,
        checkoutQuantity,
        unitPrice,
        totalAmount,
      });
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

  async submitPayment() {
    const { product, buyer, checkoutQuantity, totalAmount } = this.data;
    if (!product || !buyer) return;

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

    this.setData({ submitting: true });
    try {
      wx.showLoading({ title: '支付中' });
      const user = auth.getUser();
      const order = await orderApi.createOrder({
        buyerId: user.id,
        buyerName: user.username,
        artisanId: Number(product.creatorId),
        artisanName: product.creator,
        productTitle: product.title,
        productType: product.type,
        price: totalAmount,
        quantity: checkoutQuantity,
        productId: product.id,
        requirements: `正式购买请求: ${product.title} × ${checkoutQuantity}`,
      });
      await orderApi.payDeposit(order.id, user.id, WECHAT_CHANNEL);
      cartUtil.removeItem(product.id);
      wx.hideLoading();
      wx.showModal({
        title: '支付成功',
        content: '已通过微信支付完成，请等待卖家发货',
        showCancel: false,
        success: () => {
          wx.redirectTo({ url: '/pages/orders/orders' });
        },
      });
    } catch (e) {
      wx.hideLoading();
      wx.showToast({ title: e.message || '支付失败', icon: 'none' });
    } finally {
      this.setData({ submitting: false });
    }
  },
});
