const { customRequestApi } = require('../../services/api');
const auth = require('../../utils/auth');

Page({
  data: {
    list: [],
    loading: false,
    tab: 'all',
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 2 });
    }
    this.loadList();
  },

  onPullDownRefresh() {
    this.loadList().finally(() => wx.stopPullDownRefresh());
  },

  onTabChange(e) {
    this.setData({ tab: e.currentTarget.dataset.tab });
    this.loadList();
  },

  async loadList() {
    this.setData({ loading: true });
    try {
      const user = auth.getUser();
      let list = await customRequestApi.list();
      list = (list || []).map((item) => ({
        ...item,
        buyerName: item.buyer ? item.buyer.username : '',
        buyerId: item.buyer ? item.buyer.id : null,
        budget: item.budgetMin != null && item.budgetMax != null
          ? `${item.budgetMin}-${item.budgetMax}`
          : (item.budgetMin || item.budgetMax || ''),
      }));
      if (this.data.tab === 'mine' && user) {
        list = list.filter((item) => item.buyerId === user.id);
      } else if (this.data.tab === 'open') {
        list = list.filter((item) => item.status === 'OPEN');
      }
      this.setData({ list });
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },

  goPublish() {
    if (!auth.isLoggedIn()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    wx.navigateTo({ url: '/pages/publish-request/publish-request' });
  },

  async onBid(e) {
    if (!auth.isLoggedIn()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '揭榜报价',
      editable: true,
      placeholderText: '留言说明（选填）',
      success: async (res) => {
        if (!res.confirm) return;
        try {
          const user = auth.getUser();
          await customRequestApi.submitBid(id, user.id, res.content || '小程序揭榜');
          wx.showToast({ title: '报价成功', icon: 'success' });
          this.loadList();
        } catch (err) {
          wx.showToast({ title: err.message || '报价失败', icon: 'none' });
        }
      },
    });
  },

  async onSelectBid(e) {
    const { id, bidId } = e.currentTarget.dataset;
    const user = auth.getUser();
    try {
      await customRequestApi.selectBid(id, user.id, bidId);
      wx.showToast({ title: '已选定匠人', icon: 'success' });
      this.loadList();
    } catch (err) {
      wx.showToast({ title: err.message || '操作失败', icon: 'none' });
    }
  },

  async showBids(e) {
    const id = e.currentTarget.dataset.id;
    const user = auth.getUser();
    if (!user) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    try {
      const bids = await customRequestApi.getBids(id, user.id);
      if (!bids || !bids.length) {
        wx.showToast({ title: '暂无报价', icon: 'none' });
        return;
      }
      const itemList = bids.map((b) => `${b.artisanName || '匠人'}: ${b.message || '报价'}`);
      wx.showActionSheet({
        itemList,
        success: (res) => {
          const bid = bids[res.tapIndex];
          if (bid) {
            wx.showModal({
              title: '确认选定',
              content: `选定 ${bid.artisanName || '匠人'}？`,
              success: (r) => {
                if (r.confirm) {
                  this.onSelectBid({ currentTarget: { dataset: { id, bidId: bid.id } } });
                }
              },
            });
          }
        },
      });
    } catch (err) {
      wx.showToast({ title: err.message || '加载失败', icon: 'none' });
    }
  },
});
