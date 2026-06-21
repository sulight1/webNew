const { productApi } = require('../../services/api');
const auth = require('../../utils/auth');
const { formatImageUrl } = require('../../utils/format');

Page({
  data: {
    favorites: [],
    loading: false,
  },

  onShow() {
    if (!auth.isLoggedIn()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    this.loadFavorites();
  },

  async loadFavorites() {
    this.setData({ loading: true });
    try {
      const list = await productApi.getFavoriteProducts();
      const favorites = (list || []).map((p) => ({
        ...p,
        image: formatImageUrl(p.image),
        unavailable: p.status !== 'APPROVED',
      }));
      this.setData({ favorites });
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },

  goDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/product-detail/product-detail?id=${id}` });
  },

  async onUnfavorite(e) {
    const id = e.currentTarget.dataset.id;
    try {
      const result = await productApi.favoriteProduct(id);
      const favorited = !!(result && (result.favorited ?? result.isFavorited));
      if (!favorited) {
        this.setData({
          favorites: this.data.favorites.filter((p) => p.id !== id),
        });
        wx.showToast({ title: '已取消收藏', icon: 'none' });
      }
    } catch (err) {
      wx.showToast({ title: err.message || '操作失败', icon: 'none' });
    }
  },
});
