const { userApi } = require('../../services/api');
const auth = require('../../utils/auth');

Page({
  data: {
    mode: 'login',
    account: '',
    password: '',
    confirmPassword: '',
    loading: false,
  },

  switchMode() {
    this.setData({ mode: this.data.mode === 'login' ? 'register' : 'login' });
  },

  onInput(e) {
    this.setData({ [e.currentTarget.dataset.field]: e.detail.value });
  },

  async submit() {
    const { mode, account, password, confirmPassword } = this.data;
    const acc = (account || '').trim();
    if (!acc || !password) {
      wx.showToast({ title: '请填写账号密码', icon: 'none' });
      return;
    }
    if (!/^\d{6,20}$/.test(acc)) {
      wx.showToast({ title: '账号为6-20位数字', icon: 'none' });
      return;
    }
    if (mode === 'register' && password !== confirmPassword) {
      wx.showToast({ title: '两次密码不一致', icon: 'none' });
      return;
    }
    this.setData({ loading: true });
    try {
      let result;
      if (mode === 'login') {
        result = await userApi.login({ account: acc, password });
      } else {
        result = await userApi.register({ account: acc, password, confirmPassword });
      }
      if (result.user.role === 'ADMIN') {
        wx.showToast({ title: '管理员请使用 Web 端', icon: 'none' });
        return;
      }
      getApp().setUser(result.user, result.token);
      wx.showToast({
        title: mode === 'login' ? '登录成功' : `注册成功：${result.user.username}`,
        icon: 'none',
      });
      setTimeout(() => wx.navigateBack({ fail: () => wx.switchTab({ url: '/pages/account/account' }) }), 500);
    } catch (e) {
      wx.showToast({ title: e.message || '失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },
});
