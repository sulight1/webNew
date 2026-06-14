const auth = require('../../utils/auth');
const { userApi, notificationApi } = require('../../services/api');
const { formatImageUrl } = require('../../utils/format');

Page({
  data: {
    user: null,
    unreadCount: 0,
    menuItems: [
      { key: 'orders', title: '我的订单', url: '/pages/orders/orders' },
      { key: 'wallet', title: '造物币钱包', url: '/pages/wallet/wallet' },
      { key: 'messages', title: '私信咨询', url: '/pages/messages/messages' },
      { key: 'studio', title: '手作达人工作台', url: '/pages/studio/studio' },
    ],
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 4 });
    }
    this.refreshProfile();
  },

  async refreshProfile() {
    if (!auth.isLoggedIn()) {
      this.setData({ user: null, unreadCount: 0 });
      return;
    }
    try {
      const cached = auth.getUser();
      const user = await userApi.getProfile(cached.id);
      getApp().setUser(user);
      user.avatar = formatImageUrl(user.avatar);
      let unreadCount = 0;
      try {
        const notes = await notificationApi.list(user.id);
        unreadCount = (notes || []).filter((n) => !n.read).length;
      } catch (_) {}
      const menuItems = this.data.menuItems.map((item) => {
        if (item.key === 'studio') {
          return {
            ...item,
            title: user.role === 'ARTISAN' ? '手作达人工作台' : '成为手作达人',
          };
        }
        return item;
      });
      this.setData({ user, unreadCount, menuItems });
    } catch (e) {
      this.setData({ user: auth.getUser() });
    }
  },

  goLogin() {
    wx.navigateTo({ url: '/pages/login/login' });
  },

  logout() {
    wx.showModal({
      title: '退出登录',
      content: '确定退出？',
      success: (res) => {
        if (res.confirm) {
          getApp().clearUser();
          this.setData({ user: null });
          wx.showToast({ title: '已退出', icon: 'success' });
        }
      },
    });
  },

  onMenuTap(e) {
    if (!auth.isLoggedIn()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    const url = e.currentTarget.dataset.url;
    wx.navigateTo({ url });
  },

  goPublicProfile() {
    if (!auth.isLoggedIn()) return;
    const user = auth.getUser();
    wx.navigateTo({ url: `/pages/artisan-profile/artisan-profile?id=${user.id}` });
  },

  editProfile() {
    if (!auth.isLoggedIn()) return;
    wx.showModal({
      title: '编辑简介',
      editable: true,
      placeholderText: this.data.user.bio || '个人简介',
      success: async (res) => {
        if (!res.confirm) return;
        try {
          const user = auth.getUser();
          const updated = await userApi.updateUser(user.id, { bio: res.content || '' });
          getApp().setUser(updated);
          this.refreshProfile();
        } catch (e) {
          wx.showToast({ title: e.message || '保存失败', icon: 'none' });
        }
      },
    });
  },

  chooseAvatar() {
    if (!auth.isLoggedIn()) return;
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      success: async (res) => {
        const filePath = res.tempFiles[0].tempFilePath;
        try {
          wx.showLoading({ title: '上传中' });
          const req = require('../../utils/request');
          const url = await req.uploadFile(filePath);
          const user = auth.getUser();
          const updated = await userApi.updateUser(user.id, { avatar: url });
          getApp().setUser(updated);
          this.refreshProfile();
        } catch (e) {
          wx.showToast({ title: e.message || '上传失败', icon: 'none' });
        } finally {
          wx.hideLoading();
        }
      },
    });
  },
});
