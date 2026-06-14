const { productApi, orderApi, userApi, economyApi, statsApi } = require('../../services/api');
const auth = require('../../utils/auth');
const { formatImageUrl, orderStatusText } = require('../../utils/format');

const PRODUCT_STATUS = {
  PENDING: '审核中',
  APPROVED: '已上架',
  REJECTED: '未通过',
};

const ORDER_STATUS_LABEL = {
  PENDING_CONFIRM: '待确认',
  PENDING_PAY: '待付款',
  PRODUCING: '制作中',
  HALF_FINISHED_CONFIRM: '半成品确认',
  PENDING_SHIP: '待发货',
  PENDING_ACCEPT: '待收货',
  PENDING_BALANCE: '待付尾款',
  COMPLETED: '已完成',
  DISPUTED: '纠纷中',
  CANCELLED: '已取消',
};

function isCustomOrder(order) {
  return order.productType === 'CUSTOMIZABLE';
}

function filterProducts(products, tab) {
  if (tab === 'all') return products;
  return products.filter((p) => p.status === tab);
}

function filterOrders(orders, tab) {
  if (tab === 'all') return orders;
  if (tab === 'PENDING_SHIP') {
    return orders.filter((o) =>
      ['PENDING_SHIP', 'PENDING_ACCEPT'].includes(o.status)
      || (!isCustomOrder(o) && ['PRODUCING', 'HALF_FINISHED_CONFIRM'].includes(o.status))
    );
  }
  if (tab === 'PRODUCING') {
    return orders.filter((o) =>
      isCustomOrder(o) && ['PRODUCING', 'HALF_FINISHED_CONFIRM', 'PENDING_PAY', 'PENDING_BALANCE'].includes(o.status)
    );
  }
  return orders.filter((o) => o.status === tab);
}

Page({
  data: {
    user: null,
    isArtisan: false,
    isPending: false,
    menu: 'my-products',
    productTab: 'all',
    orderTab: 'all',
    menus: [
      { key: 'my-products', title: '我的作品库' },
      { key: 'orders', title: '店铺订单' },
      { key: 'coin-tasks', title: '任务与签到' },
      { key: 'analytics', title: '经营数据' },
    ],
    productTabs: [
      { key: 'all', label: '全部' },
      { key: 'PENDING', label: '审核中' },
      { key: 'APPROVED', label: '已上架' },
      { key: 'REJECTED', label: '未通过' },
    ],
    orderTabs: [
      { key: 'all', label: '全部' },
      { key: 'PENDING_CONFIRM', label: '待确认' },
      { key: 'PENDING_SHIP', label: '待发货' },
      { key: 'PRODUCING', label: '制作中' },
      { key: 'COMPLETED', label: '已完成' },
    ],
    products: [],
    filteredProducts: [],
    orders: [],
    filteredOrders: [],
    tasks: [],
    analytics: null,
    statCards: [],
    loading: false,
    checkingIn: false,
    claiming: '',
    applying: false,
  },

  onLoad(options) {
    if (options.menu) {
      this.setData({ menu: options.menu });
    }
  },

  onShow() {
    if (!auth.isLoggedIn()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    this.syncUserState();
  },

  async syncUserState() {
    try {
      const cached = auth.getUser();
      const user = await userApi.getProfile(cached.id);
      getApp().setUser(user);
      const isArtisan = user.role === 'ARTISAN';
      const isPending = user.artisanApplyStatus === 'PENDING';
      this.setData({
        user,
        isArtisan,
        isPending,
      });
      if (isArtisan) {
        this.loadSection();
      }
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
    }
  },

  onMenuChange(e) {
    this.setData({ menu: e.currentTarget.dataset.menu });
    this.loadSection();
  },

  onProductTabChange(e) {
    this.setData({ productTab: e.currentTarget.dataset.tab });
    this.applyProductFilter();
  },

  onOrderTabChange(e) {
    this.setData({ orderTab: e.currentTarget.dataset.tab });
    this.applyOrderFilter();
  },

  loadSection() {
    const { menu } = this.data;
    if (menu === 'my-products') this.loadProducts();
    else if (menu === 'orders') this.loadOrders();
    else if (menu === 'coin-tasks') this.loadTasks();
    else if (menu === 'analytics') this.loadAnalytics();
  },

  applyProductFilter() {
    const filteredProducts = filterProducts(this.data.products, this.data.productTab);
    this.setData({ filteredProducts });
  },

  applyOrderFilter() {
    const filteredOrders = filterOrders(this.data.orders, this.data.orderTab);
    this.setData({ filteredOrders });
  },

  async loadProducts() {
    const user = auth.getUser();
    this.setData({ loading: true });
    try {
      const list = await productApi.getProducts({ creatorId: user.id });
      const products = (list || []).map((p) => ({
        ...p,
        image: formatImageUrl(p.image),
        statusLabel: PRODUCT_STATUS[p.status] || p.status,
        isReadyMade: p.type === 'READY_MADE',
      }));
      this.setData({ products });
      this.applyProductFilter();
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },

  async loadOrders() {
    const user = auth.getUser();
    this.setData({ loading: true });
    try {
      const list = await orderApi.getArtisanOrders(user.id);
      const orders = (list || []).map((o) => ({
        ...o,
        statusLabel: orderStatusText(o.status, o),
        isCustom: isCustomOrder(o),
        typeLabel: isCustomOrder(o) ? '定制' : '成品',
      }));
      this.setData({ orders });
      this.applyOrderFilter();
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },

  async loadTasks() {
    const user = auth.getUser();
    this.setData({ loading: true });
    try {
      const tasks = await economyApi.getTasks(user.id);
      this.setData({ tasks: tasks || [] });
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },

  async loadAnalytics() {
    const user = auth.getUser();
    this.setData({ loading: true });
    try {
      const analytics = await statsApi.getArtisanAnalytics(user.id);
      const statCards = [
        { label: '造物币余额', value: analytics.coinBalance ?? 0 },
        { label: '作品数', value: analytics.productCount ?? 0 },
        { label: '已完成订单', value: analytics.completedOrderCount ?? 0 },
        { label: '累计收入', value: analytics.revenue ?? 0 },
        { label: '粉丝', value: analytics.followerCount ?? 0 },
        { label: '总获赞', value: analytics.totalLikes ?? 0 },
      ];
      const orderStatusList = Object.keys(analytics.ordersByStatus || {}).map((key) => ({
        key,
        label: ORDER_STATUS_LABEL[key] || key,
        count: analytics.ordersByStatus[key],
      }));
      const maxCount = Math.max(...orderStatusList.map((o) => o.count), 1);
      orderStatusList.forEach((o) => {
        o.percent = Math.round((o.count / maxCount) * 100);
      });
      analytics.orderStatusList = orderStatusList;
      this.setData({ analytics, statCards });
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },

  goPublish() {
    wx.navigateTo({ url: '/pages/publish-product/publish-product' });
  },

  goOrder(e) {
    wx.navigateTo({ url: `/pages/order-detail/order-detail?id=${e.currentTarget.dataset.id}` });
  },

  goProfile() {
    const user = auth.getUser();
    wx.navigateTo({ url: `/pages/artisan-profile/artisan-profile?id=${user.id}` });
  },

  async deleteProduct(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '删除作品',
      content: '确定删除该作品吗？',
      success: async (res) => {
        if (!res.confirm) return;
        try {
          await productApi.deleteProduct(id);
          wx.showToast({ title: '已删除', icon: 'success' });
          this.loadProducts();
        } catch (err) {
          wx.showToast({ title: err.message || '删除失败', icon: 'none' });
        }
      },
    });
  },

  async boostProduct(e) {
    const id = e.currentTarget.dataset.id;
    const user = auth.getUser();
    wx.showModal({
      title: '曝光推广',
      content: '花费 20 造物币，作品获得 7 天优先曝光，确认推广？',
      success: async (res) => {
        if (!res.confirm) return;
        try {
          await economyApi.boostProduct(user.id, id);
          wx.showToast({ title: '推广成功', icon: 'success' });
          await this.syncUserState();
          this.loadProducts();
        } catch (err) {
          wx.showToast({ title: err.message || '推广失败', icon: 'none' });
        }
      },
    });
  },

  editProduct(e) {
    const item = this.data.products.find((p) => p.id === e.currentTarget.dataset.id);
    if (!item) return;
    wx.showModal({
      title: '修改价格',
      editable: true,
      placeholderText: String(item.price),
      success: async (res) => {
        if (!res.confirm || !res.content) return;
        const price = Number(res.content);
        if (!price || price <= 0) {
          wx.showToast({ title: '请输入有效价格', icon: 'none' });
          return;
        }
        try {
          await productApi.updateProduct(item.id, { price });
          wx.showToast({ title: '已更新', icon: 'success' });
          this.loadProducts();
        } catch (err) {
          wx.showToast({ title: err.message || '更新失败', icon: 'none' });
        }
      },
    });
  },

  async doCheckIn() {
    const user = auth.getUser();
    this.setData({ checkingIn: true });
    try {
      const res = await economyApi.checkIn(user.id);
      wx.showToast({ title: res.message || '签到成功', icon: 'success' });
      await this.syncUserState();
      this.loadTasks();
    } catch (e) {
      wx.showToast({ title: e.message || '签到失败', icon: 'none' });
    } finally {
      this.setData({ checkingIn: false });
    }
  },

  async claimTask(e) {
    const code = e.currentTarget.dataset.code;
    const user = auth.getUser();
    this.setData({ claiming: code });
    try {
      const res = await economyApi.claimTask(user.id, code);
      wx.showToast({ title: res.message || '领取成功', icon: 'success' });
      await this.syncUserState();
      this.loadTasks();
    } catch (err) {
      wx.showToast({ title: err.message || '领取失败', icon: 'none' });
    } finally {
      this.setData({ claiming: '' });
    }
  },

  async applyArtisan() {
    const user = auth.getUser();
    this.setData({ applying: true });
    try {
      await userApi.applyArtisan(user.id);
      wx.showToast({ title: '申请已提交', icon: 'success' });
      await this.syncUserState();
    } catch (e) {
      wx.showToast({ title: e.message || '申请失败', icon: 'none' });
    } finally {
      this.setData({ applying: false });
    }
  },
});
