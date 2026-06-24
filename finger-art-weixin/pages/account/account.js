const auth = require('../../utils/auth');
const cartUtil = require('../../utils/cart');
const { userApi, notificationApi } = require('../../services/api');
const { formatImageUrl } = require('../../utils/format');

Page({
  data: {
    user: null,
    unreadCount: 0,
    shippingName: '',
    shippingPhone: '',
    shippingAddress: '',
    savingAddress: false,
    cartCount: 0,
    menuItems: [
      { key: 'cart', title: '购物车', url: '/pages/cart/cart' },
      { key: 'orders', title: '我的订单', url: '/pages/orders/orders' },
      { key: 'my-requests', title: '我的定制需求', action: 'customMine' },
      { key: 'my-exchanges', title: '我的技能交换', url: '/pages/my-skills/my-skills' },
      { key: 'favorites', title: '我的收藏', url: '/pages/favorites/favorites' },
      { key: 'inspiration', title: '我的灵感本', url: '/pages/inspiration-notebook/inspiration-notebook' },
      { key: 'wallet', title: '造物币钱包', url: '/pages/wallet/wallet' },
      { key: 'messages', title: '私信咨询', url: '/pages/messages/messages' },
      { key: 'studio', title: '手作达人工作台', url: '/pages/studio/studio' },
    ],
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 4 });
    }
    this._onRealtimeBound = (event) => this._onRealtimeEvent(event);
    getApp().registerRealtimeHandler('account', this._onRealtimeBound);
    this.refreshProfile();
  },

  onHide() {
    getApp().unregisterRealtimeHandler('account');
  },

  _onRealtimeEvent(event) {
    if (event.type === 'NOTIFICATION') {
      this.setData({ unreadCount: (this.data.unreadCount || 0) + 1 });
    }
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
        unreadCount = (notes || []).filter((n) => !(n.isRead || n.read)).length;
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
      this.setData({ user, unreadCount, menuItems, cartCount: cartUtil.getTotalCount() });
      this.syncShippingForm(user);
    } catch (e) {
      this.setData({ user: auth.getUser(), cartCount: cartUtil.getTotalCount() });
      this.syncShippingForm(auth.getUser());
    }
  },

  syncShippingForm(user) {
    if (!user) return;
    this.setData({
      shippingName: user.shippingName || '',
      shippingPhone: user.shippingPhone || '',
      shippingAddress: user.shippingAddress || '',
    });
  },

  onShippingNameInput(e) {
    this.setData({ shippingName: e.detail.value });
  },

  onShippingPhoneInput(e) {
    this.setData({ shippingPhone: e.detail.value });
  },

  onShippingAddressInput(e) {
    this.setData({ shippingAddress: e.detail.value });
  },

  async saveShippingAddress() {
    if (!auth.isLoggedIn()) return;
    const { shippingName, shippingPhone, shippingAddress } = this.data;
    const phone = (shippingPhone || '').trim();
    if (phone && !/^1\d{10}$/.test(phone)) {
      wx.showToast({ title: '请输入正确手机号', icon: 'none' });
      return;
    }
    this.setData({ savingAddress: true });
    try {
      const user = auth.getUser();
      const updated = await userApi.updateUser(user.id, {
        shippingName: (shippingName || '').trim(),
        shippingPhone: phone,
        shippingAddress: (shippingAddress || '').trim(),
      });
      getApp().setUser(updated);
      this.refreshProfile();
      wx.showToast({ title: '已保存', icon: 'success' });
    } catch (e) {
      wx.showToast({ title: e.message || '保存失败', icon: 'none' });
    } finally {
      this.setData({ savingAddress: false });
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
    const action = e.currentTarget.dataset.action;
    if (action === 'customMine') {
      getApp().globalData.pendingCustomScope = 'mine';
      wx.switchTab({ url: '/pages/custom-request/custom-request' });
      return;
    }
    const url = e.currentTarget.dataset.url;
    if (url) wx.navigateTo({ url });
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
