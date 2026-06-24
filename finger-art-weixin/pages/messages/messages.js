const { messageApi } = require('../../services/api');
const auth = require('../../utils/auth');

Page({
  data: {
    messages: [],
    loading: false,
  },

  onShow() {
    if (!auth.isLoggedIn()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    this._onRealtimeBound = (event) => this._onRealtimeEvent(event);
    getApp().registerRealtimeHandler('messages', this._onRealtimeBound);
    this.loadMessages();
  },

  onHide() {
    getApp().unregisterRealtimeHandler('messages');
  },

  _onRealtimeEvent(event) {
    if (event.type === 'CHAT_MESSAGE') {
      this.loadMessages();
    }
  },

  async loadMessages() {
    this.setData({ loading: true });
    try {
      const user = auth.getUser();
      const messages = await messageApi.list(user.id);
      this.setData({ messages: messages || [] });
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },
});
