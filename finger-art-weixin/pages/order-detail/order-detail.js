const { orderApi } = require('../../services/api');
const auth = require('../../utils/auth');
const { orderStatusText } = require('../../utils/format');

Page({
  data: {
    order: null,
    milestones: [],
    loading: true,
    isBuyer: false,
    isSeller: false,
    isReadyMade: false,
  },

  onLoad(options) {
    this.orderId = options.id;
    this.loadOrder();
  },

  async loadOrder() {
    this.setData({ loading: true });
    try {
      const order = await orderApi.getOrderById(this.orderId);
      const user = auth.getUser();
      const isReadyMade = order.productType === 'READY_MADE';
      const milestones = await orderApi.getMilestones(this.orderId).catch(() => []);
      this.setData({
        order: {
          ...order,
          statusLabel: orderStatusText(order.status, order),
        },
        milestones: milestones || [],
        isBuyer: user && user.id === order.buyerId,
        isSeller: user && user.id === order.artisanId,
        isReadyMade,
        loading: false,
      });
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
      this.setData({ loading: false });
    }
  },

  async doPayDeposit() {
    const user = auth.getUser();
    try {
      await orderApi.payDeposit(this.orderId, user.id);
      wx.showToast({ title: '支付成功', icon: 'success' });
      this.loadOrder();
    } catch (e) {
      wx.showToast({ title: e.message || '支付失败', icon: 'none' });
    }
  },

  async doPayBalance() {
    const user = auth.getUser();
    try {
      await orderApi.payBalance(this.orderId, user.id);
      wx.showToast({ title: '尾款支付成功', icon: 'success' });
      this.loadOrder();
    } catch (e) {
      wx.showToast({ title: e.message || '支付失败', icon: 'none' });
    }
  },

  async doConfirm() {
    const user = auth.getUser();
    try {
      await orderApi.confirmOrder(this.orderId, user.id);
      wx.showToast({ title: '已确认接单', icon: 'success' });
      this.loadOrder();
    } catch (e) {
      wx.showToast({ title: e.message || '操作失败', icon: 'none' });
    }
  },

  async doStatus(e) {
    const status = e.currentTarget.dataset.status;
    const user = auth.getUser();
    try {
      await orderApi.updateStatus(this.orderId, status, user.id, user.username);
      wx.showToast({ title: '状态已更新', icon: 'success' });
      this.loadOrder();
    } catch (err) {
      wx.showToast({ title: err.message || '操作失败', icon: 'none' });
    }
  },

  async doConfirmReceipt() {
    const user = auth.getUser();
    try {
      await orderApi.confirmReceipt(this.orderId, user.id);
      wx.showToast({ title: '已确认收货', icon: 'success' });
      this.loadOrder();
    } catch (e) {
      wx.showToast({ title: e.message || '操作失败', icon: 'none' });
    }
  },

  async doRequestCancel() {
    const user = auth.getUser();
    wx.showModal({
      title: '申请取消',
      editable: true,
      placeholderText: '取消原因',
      success: async (res) => {
        if (!res.confirm) return;
        try {
          await orderApi.requestCancel(this.orderId, user.id, res.content || '');
          wx.showToast({ title: '已提交取消申请', icon: 'success' });
          this.loadOrder();
        } catch (e) {
          wx.showToast({ title: e.message || '操作失败', icon: 'none' });
        }
      },
    });
  },

  async doApproveCancel() {
    const user = auth.getUser();
    try {
      await orderApi.approveCancel(this.orderId, user.id);
      wx.showToast({ title: '已同意取消', icon: 'success' });
      this.loadOrder();
    } catch (e) {
      wx.showToast({ title: e.message || '操作失败', icon: 'none' });
    }
  },

  async doRejectCancel() {
    const user = auth.getUser();
    try {
      await orderApi.rejectCancel(this.orderId, user.id, '');
      wx.showToast({ title: '已拒绝取消', icon: 'success' });
      this.loadOrder();
    } catch (e) {
      wx.showToast({ title: e.message || '操作失败', icon: 'none' });
    }
  },
});
