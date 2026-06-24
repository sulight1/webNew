const { orderApi } = require('../../services/api');
const auth = require('../../utils/auth');
const { orderStatusText } = require('../../utils/format');
const { copyText, openFallbackUrl } = require('../../utils/logistics');

Page({
  data: {
    order: null,
    milestones: [],
    loading: true,
    isBuyer: false,
    isSeller: false,
    isReadyMade: false,
    showShipForm: false,
    shipCompany: '',
    shipTrackingNo: '',
    logistics: null,
    logisticsLoading: false,
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
        logistics: null,
      });
      if (order.trackingNumber) {
        this.loadLogistics(true);
      }
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
      this.setData({ loading: false });
    }
  },

  goProduct() {
    const order = this.data.order;
    if (!order?.productId) return;
    wx.navigateTo({
      url: `/pages/product-detail/product-detail?id=${order.productId}&fromOrder=${order.id}`,
    });
  },

  async doPayDeposit() {
    const user = auth.getUser();
    const order = this.data.order;
    const amount = order?.depositAmount ?? order?.price ?? 0;
    const balance = Number(user?.zaowuBiBalance ?? 0);
    if (balance < Number(amount)) {
      wx.showModal({
        title: '造物币余额不足',
        content: '请先在钱包充值后再支付',
        confirmText: '去充值',
        success: (r) => {
          if (r.confirm) wx.navigateTo({ url: '/pages/wallet/wallet' });
        },
      });
      return;
    }
    try {
      await orderApi.payDeposit(this.orderId, user.id, 'ZAOWU_COIN');
      try {
        const { userApi } = require('../../services/api');
        const profile = await userApi.getProfile(user.id);
        getApp().setUser(profile);
      } catch (_) {}
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

  openShipForm() {
    this.setData({ showShipForm: true, shipCompany: '', shipTrackingNo: '' });
  },

  closeShipForm() {
    this.setData({ showShipForm: false });
  },

  onShipCompanyInput(e) {
    this.setData({ shipCompany: e.detail.value });
  },

  onShipTrackingInput(e) {
    this.setData({ shipTrackingNo: e.detail.value });
  },

  async submitShip() {
    const { shipCompany, shipTrackingNo } = this.data;
    if (!shipCompany.trim()) {
      wx.showToast({ title: '请填写物流公司', icon: 'none' });
      return;
    }
    if (!shipTrackingNo.trim()) {
      wx.showToast({ title: '请填写快递单号', icon: 'none' });
      return;
    }
    const user = auth.getUser();
    try {
      await orderApi.shipOrder(this.orderId, user.id, shipCompany.trim(), shipTrackingNo.trim(), user.username);
      wx.showToast({ title: '已发货', icon: 'success' });
      this.setData({ showShipForm: false });
      this.loadOrder();
    } catch (e) {
      wx.showToast({ title: e.message || '发货失败', icon: 'none' });
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

  copyTrackingNumber() {
    const no = this.data.order && this.data.order.trackingNumber;
    copyText(no, '单号已复制');
  },

  async loadLogistics(silent) {
    if (!this.orderId) return;
    const quiet = silent === true;
    this.setData({ logisticsLoading: true });
    try {
      const logistics = await orderApi.getLogistics(this.orderId);
      this.setData({ logistics });
      if (!quiet && logistics && logistics.apiAvailable && logistics.tracks && logistics.tracks.length) {
        wx.showToast({ title: '物流已更新', icon: 'success' });
      }
    } catch (e) {
      if (!quiet) {
        wx.showToast({ title: e.message || '查询失败', icon: 'none' });
      }
    } finally {
      this.setData({ logisticsLoading: false });
    }
  },

  openKuaidi100() {
    const url = this.data.logistics && this.data.logistics.fallbackUrl;
    openFallbackUrl(url);
  },

  stopPropagation() {},
});
