const { walletApi, userApi } = require('../../services/api');
const auth = require('../../utils/auth');

Page({
  data: {
    user: null,
    transactions: [],
    amount: '',
    loading: false,
  },

  onShow() {
    if (!auth.isLoggedIn()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    this.loadWallet();
  },

  async loadWallet() {
    this.setData({ loading: true });
    try {
      const cached = auth.getUser();
      const user = await userApi.getProfile(cached.id);
      getApp().setUser(user);
      const txPage = await walletApi.transactions(user.id);
      const transactions = (txPage && txPage.content) ? txPage.content : (txPage || []);
      this.setData({ user, transactions });
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },

  onAmountInput(e) {
    this.setData({ amount: e.detail.value });
  },

  async recharge() {
    const amount = Number(this.data.amount);
    if (!amount || amount <= 0) {
      wx.showToast({ title: '请输入有效金额', icon: 'none' });
      return;
    }
    try {
      const user = auth.getUser();
      await walletApi.recharge(user.id, amount);
      wx.showToast({ title: '充值成功', icon: 'success' });
      this.setData({ amount: '' });
      this.loadWallet();
    } catch (e) {
      wx.showToast({ title: e.message || '充值失败', icon: 'none' });
    }
  },
});
