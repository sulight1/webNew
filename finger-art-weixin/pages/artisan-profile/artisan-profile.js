const { userApi, productApi } = require('../../services/api');
const auth = require('../../utils/auth');
const { formatImageUrl } = require('../../utils/format');

Page({
  data: {
    profile: null,
    products: [],
    isFollowing: false,
    loading: true,
  },

  onLoad(options) {
    this.userId = options.id;
    this.loadProfile();
  },

  async loadProfile() {
    try {
      const profile = await userApi.getPublicProfile(this.userId);
      profile.avatar = formatImageUrl(profile.avatar);
      const products = await productApi.getProducts({ creatorId: this.userId, scope: 'approved' });
      const user = auth.getUser();
      let isFollowing = false;
      if (user) {
        isFollowing = await userApi.checkFollowing(user.id, this.userId).catch(() => false);
      }
      this.setData({
        profile,
        products: (products || [])
          .filter((p) => p.status === 'APPROVED')
          .map((p) => ({ ...p, image: formatImageUrl(p.image) })),
        isFollowing,
        loading: false,
      });
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
    }
  },

  async toggleFollow() {
    if (!auth.isLoggedIn()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    const user = auth.getUser();
    try {
      if (this.data.isFollowing) {
        await userApi.unfollowUser(user.id, this.userId);
      } else {
        await userApi.followUser(user.id, this.userId);
      }
      this.setData({ isFollowing: !this.data.isFollowing });
    } catch (e) {
      wx.showToast({ title: e.message || '操作失败', icon: 'none' });
    }
  },

  goProduct(e) {
    wx.navigateTo({ url: `/pages/product-detail/product-detail?id=${e.currentTarget.dataset.id}` });
  },
});
