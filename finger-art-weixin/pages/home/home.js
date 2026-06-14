const { statsApi, productApi, userApi } = require('../../services/api');
const { formatImageUrl } = require('../../utils/format');

Page({
  data: {
    stats: {},
    hotProducts: [],
    artisans: [],
    loading: true,
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 0 });
    }
    this.loadData();
  },

  async loadData() {
    this.setData({ loading: true });
    try {
      const app = getApp();
      const userId = app.globalData.user && app.globalData.user.id;
      const [platform, products, artisans] = await Promise.all([
        statsApi.platform().catch(() => ({})),
        productApi.getProducts({ scope: 'approved' }).catch(() => []),
        userApi.getTopArtisans(6, userId).catch(() => []),
      ]);
      const approved = (products || []).filter((p) => p.status === 'APPROVED');
      const hot = approved.slice(0, 6).map((p) => ({ ...p, image: formatImageUrl(p.image) }));
      const artisanList = (artisans || []).map((a) => ({
        ...a,
        avatar: formatImageUrl(a.avatar),
      }));
      this.setData({
        stats: {
          productCount: approved.length,
          artisanCount: platform.artisanCount || 0,
          orderCount: platform.openRequests || 0,
        },
        hotProducts: hot,
        artisans: artisanList,
      });
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },

  goMarketplace() {
    wx.switchTab({ url: '/pages/marketplace/marketplace' });
  },

  goCustomRequest() {
    wx.switchTab({ url: '/pages/custom-request/custom-request' });
  },

  goProduct(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/product-detail/product-detail?id=${id}` });
  },

  goArtisan(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/artisan-profile/artisan-profile?id=${id}` });
  },
});
