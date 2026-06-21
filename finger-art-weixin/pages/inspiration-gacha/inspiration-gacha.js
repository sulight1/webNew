const auth = require('../../utils/auth');
const { inspirationGachaApi, userApi } = require('../../services/api');
const notebook = require('../../utils/inspiration-notebook');
const inspirePayload = require('../../utils/inspire-payload');
const { formatImageUrl } = require('../../utils/format');

Page({
  data: {
    status: {
      freeAvailable: true,
      extraDrawCost: 10,
      imageGenCost: 15,
      balance: 0,
    },
    result: null,
    spinning: false,
    drawing: false,
    generatingImage: false,
    drawButtonText: '免费扭一次',
  },

  onShow() {
    if (!auth.isLoggedIn()) {
      wx.showModal({
        title: '登录后体验',
        content: '灵感扭蛋需要登录，每日首次免费',
        confirmText: '去登录',
        success: (res) => {
          if (res.confirm) wx.navigateTo({ url: '/pages/login/login' });
          else wx.navigateBack();
        },
      });
      return;
    }
    this.loadStatus();
  },

  async loadStatus() {
    try {
      const user = auth.getUser();
      const status = await inspirationGachaApi.getStatus(user.id);
      this.setData({
        status,
        drawButtonText: status.freeAvailable
          ? '免费扭一次'
          : `再扭一次 · ${status.extraDrawCost || 10} 造物币`,
      });
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
    }
  },

  async onDraw() {
    if (!auth.isLoggedIn()) return;
    const user = auth.getUser();
    const { status } = this.data;
    const useFree = !!status.freeAvailable;

    if (!useFree) {
      const cost = status.extraDrawCost || 10;
      const ok = await this.confirmModal(`再次扭蛋将消耗 ${cost} 造物币，确定吗？`);
      if (!ok) return;
    }

    this.setData({ drawing: true, spinning: true, result: null });
    try {
      const result = await inspirationGachaApi.draw(user.id, useFree);
      await this.delay(900);
      this.applyResult(result);
      await this.syncUserBalance(result.balance);
    } catch (e) {
      this.setData({ spinning: false });
      wx.showToast({ title: e.message || '扭蛋失败', icon: 'none' });
    } finally {
      this.setData({ drawing: false });
    }
  },

  async onGenerateImage() {
    const { result, status } = this.data;
    if (!result || !result.imagePrompt) return;
    const cost = status.imageGenCost || 15;
    const ok = await this.confirmModal(`生成参考图将消耗 ${cost} 造物币，可能需要 1-2 分钟，确定吗？`);
    if (!ok) return;

    this.setData({ generatingImage: true });
    try {
      wx.showLoading({ title: '万相生图中' });
      const user = auth.getUser();
      const data = await inspirationGachaApi.generateImage(user.id, result.imagePrompt);
      const imageUrl = data.imageUrl;
      this.setData({
        result: {
          ...result,
          imageUrl,
          imagePreview: formatImageUrl(imageUrl),
        },
        status: {
          ...status,
          balance: data.balance,
        },
        drawButtonText: this.data.status.freeAvailable
          ? '免费扭一次'
          : `再扭一次 · ${status.extraDrawCost || 10} 造物币`,
      });
      await this.syncUserBalance(data.balance);
      wx.showToast({ title: '参考图已生成', icon: 'success' });
    } catch (e) {
      wx.showToast({ title: e.message || '生图失败', icon: 'none' });
    } finally {
      wx.hideLoading();
      this.setData({ generatingImage: false });
    }
  },

  applyResult(result) {
    const preview = result.imageUrl ? formatImageUrl(result.imageUrl) : '';
    this.setData({
      result: { ...result, imagePreview: preview },
      spinning: false,
      status: {
        freeAvailable: result.freeAvailable,
        extraDrawCost: result.extraDrawCost,
        imageGenCost: result.imageGenCost,
        balance: result.balance,
      },
      drawButtonText: result.freeAvailable
        ? '免费扭一次'
        : `再扭一次 · ${result.extraDrawCost || 10} 造物币`,
    });
  },

  async syncUserBalance(balance) {
    try {
      const cached = auth.getUser();
      const user = await userApi.getProfile(cached.id);
      if (typeof balance === 'number') user.zaowuBiBalance = balance;
      getApp().setUser(user);
    } catch (_) {}
  },

  saveToNotebook() {
    const { result } = this.data;
    if (!result) return;
    const user = auth.getUser();
    if (!user || !user.id) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }
    notebook.save(result, user.id);
    wx.showToast({ title: '已收藏', icon: 'success' });
  },

  buildPayload() {
    const { result } = this.data;
    if (!result) return null;
    return {
      title: result.title,
      description: result.description,
      category: result.category,
      suggestedPrice: result.suggestedPrice,
      imageUrl: result.imageUrl || '',
      copy: result.copy,
      styleLabel: result.styleLabel,
      paletteName: result.paletteName,
    };
  },

  goPublishProduct() {
    const payload = this.buildPayload();
    if (!payload) return;
    inspirePayload.setPayload({ ...payload, target: 'product' });
    wx.navigateTo({ url: '/pages/publish-product/publish-product' });
  },

  goPublishRequest() {
    const payload = this.buildPayload();
    if (!payload) return;
    inspirePayload.setPayload({ ...payload, target: 'request' });
    wx.navigateTo({ url: '/pages/publish-request/publish-request' });
  },

  previewImage() {
    const url = this.data.result && this.data.result.imagePreview;
    if (!url) return;
    wx.previewImage({ urls: [url] });
  },

  confirmModal(content) {
    return new Promise((resolve) => {
      wx.showModal({
        title: '确认',
        content,
        success: (res) => resolve(!!res.confirm),
      });
    });
  },

  delay(ms) {
    return new Promise((resolve) => setTimeout(resolve, ms));
  },
});
