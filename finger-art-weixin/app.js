const auth = require('./utils/auth');

App({
  onLaunch() {
    this.globalData.user = auth.getUser();
    this.globalData.token = auth.getToken();
  },

  setUser(user, token) {
    auth.setLogin(user, token);
    this.globalData.user = user;
    this.globalData.token = token || auth.getToken();
  },

  clearUser() {
    auth.clearAuth();
    this.globalData.user = null;
    this.globalData.token = '';
  },

  refreshUser() {
    const user = auth.getUser();
    this.globalData.user = user;
    return user;
  },

  globalData: {
    user: null,
    token: '',
  },
});
