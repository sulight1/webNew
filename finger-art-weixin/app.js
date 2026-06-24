const auth = require('./utils/auth');
const realtime = require('./utils/realtime');

App({
  onLaunch() {
    this.globalData.user = auth.getUser();
    this.globalData.token = auth.getToken();
    this.globalData.realtimeHandlers = {};

    realtime.onEvent((event) => this.dispatchRealtimeEvent(event));

    const user = auth.getUser();
    if (user && user.id) {
      realtime.connect(user.id);
    }
  },

  dispatchRealtimeEvent(event) {
    const { type, payload } = event || {};
    if (type === 'NOTIFICATION' && payload) {
      // 私信会额外推 CHAT_MESSAGE，避免重复 toast
      if (payload.type !== 'MESSAGE') {
        wx.showToast({
          title: payload.title || '新通知',
          icon: 'none',
          duration: 2500,
        });
      }
    } else if (type === 'CHAT_MESSAGE') {
      wx.showToast({
        title: '收到新私信',
        icon: 'none',
        duration: 2000,
      });
    }

    const handlers = this.globalData.realtimeHandlers || {};
    Object.keys(handlers).forEach((key) => {
      try {
        handlers[key](event);
      } catch (e) {
        // ignore page handler errors
      }
    });
  },

  registerRealtimeHandler(key, handler) {
    if (!this.globalData.realtimeHandlers) {
      this.globalData.realtimeHandlers = {};
    }
    this.globalData.realtimeHandlers[key] = handler;
  },

  unregisterRealtimeHandler(key) {
    if (this.globalData.realtimeHandlers) {
      delete this.globalData.realtimeHandlers[key];
    }
  },

  setUser(user, token) {
    auth.setLogin(user, token);
    this.globalData.user = user;
    this.globalData.token = token || auth.getToken();
    if (user && user.id) {
      realtime.connect(user.id);
    }
  },

  clearUser() {
    auth.clearAuth();
    this.globalData.user = null;
    this.globalData.token = '';
    realtime.disconnect();
  },

  refreshUser() {
    const user = auth.getUser();
    this.globalData.user = user;
    return user;
  },

  globalData: {
    user: null,
    token: '',
    realtimeHandlers: {},
    pendingCustomScope: null,
    pendingShowExchanges: false,
    pendingSkillHubTab: null,
  },
});
