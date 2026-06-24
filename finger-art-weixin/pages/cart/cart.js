const auth = require('../../utils/auth');
const cartUtil = require('../../utils/cart');
const { formatImageUrl } = require('../../utils/format');

Page({
  data: {
    items: [],
    totalCount: 0,
    totalAmount: 0,
  },

  onShow() {
    if (!auth.isLoggedIn()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    this.refreshCart();
  },

  refreshCart() {
    const items = cartUtil.loadItems().map((item) => ({
      ...item,
      image: formatImageUrl(item.image),
      subtotal: (Math.round(Number(item.price) * item.quantity * 100) / 100).toFixed(2),
    }));
    this.setData({
      items,
      totalCount: cartUtil.getTotalCount(items),
      totalAmount: cartUtil.getTotalAmount(items).toFixed(2),
    });
  },

  goProduct(e) {
    wx.navigateTo({ url: `/pages/product-detail/product-detail?id=${e.currentTarget.dataset.id}` });
  },

  onMinus(e) {
    const { id, qty } = e.currentTarget.dataset;
    cartUtil.updateQuantity(id, qty - 1);
    this.refreshCart();
  },

  onPlus(e) {
    const { id, qty, stock } = e.currentTarget.dataset;
    if (qty >= stock) {
      wx.showToast({ title: '已达库存上限', icon: 'none' });
      return;
    }
    cartUtil.updateQuantity(id, qty + 1);
    this.refreshCart();
  },

  onRemove(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '移除商品',
      content: '确定从购物车移除该商品吗？',
      success: (res) => {
        if (!res.confirm) return;
        cartUtil.removeItem(id);
        this.refreshCart();
      },
    });
  },

  onClear() {
    if (!this.data.items.length) return;
    wx.showModal({
      title: '清空购物车',
      content: '确定移除购物车中的所有商品吗？',
      success: (res) => {
        if (!res.confirm) return;
        cartUtil.clear();
        this.refreshCart();
      },
    });
  },

  goCheckout() {
    const allItems = cartUtil.loadItems();
    const readyItems = allItems.filter((item) => item.type !== 'CUSTOMIZABLE');
    const customCount = allItems.length - readyItems.length;

    if (!readyItems.length) {
      wx.showToast({ title: '没有可结算的成品', icon: 'none' });
      return;
    }

    if (customCount > 0) {
      wx.showToast({ title: `已跳过${customCount}件定制商品`, icon: 'none' });
    }

    if (readyItems.length === 1) {
      const item = readyItems[0];
      wx.navigateTo({ url: `/pages/checkout/checkout?productId=${item.productId}&qty=${item.quantity}` });
      return;
    }

    wx.navigateTo({ url: '/pages/cart-checkout/cart-checkout' });
  },

  goMarketplace() {
    wx.switchTab({ url: '/pages/marketplace/marketplace' });
  },
});
