const { customRequestApi } = require('../../services/api');
const auth = require('../../utils/auth');
const { PRODUCT_CATEGORIES } = require('../../constants/categories');
const req = require('../../utils/request');
const inspirePayload = require('../../utils/inspire-payload');
const { formatImageUrl } = require('../../utils/format');

function findCategoryIndex(key) {
  const idx = PRODUCT_CATEGORIES.findIndex((c) => c.key === key);
  return idx >= 0 ? idx : 0;
}

Page({
  data: {
    categories: PRODUCT_CATEGORIES,
    categoryIndex: 0,
    form: {
      title: '',
      description: '',
      budgetMin: '',
      budgetMax: '',
      deadline: '',
    },
    imageUrl: '',
    imagePreview: '',
  },

  onLoad() {
    if (!auth.isLoggedIn()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    this.applyInspirePayload();
  },

  applyInspirePayload() {
    const payload = inspirePayload.consumePayload();
    if (!payload || payload.target !== 'request') return;
    const categoryIndex = findCategoryIndex(payload.category);
    const updates = {
      categoryIndex,
      'form.title': payload.title || '',
      'form.description': payload.description || '',
    };
    if (payload.suggestedPrice) {
      updates['form.budgetMin'] = String(Math.max(20, payload.suggestedPrice - 20));
      updates['form.budgetMax'] = String(payload.suggestedPrice + 40);
    }
    if (payload.imageUrl) {
      updates.imageUrl = payload.imageUrl;
      updates.imagePreview = formatImageUrl(payload.imageUrl);
    }
    this.setData(updates);
    wx.showToast({ title: '已填入灵感', icon: 'success' });
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ [`form.${field}`]: e.detail.value });
  },

  onCategoryChange(e) {
    this.setData({ categoryIndex: Number(e.detail.value) });
  },

  chooseImage() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      success: async (res) => {
        try {
          wx.showLoading({ title: '上传中' });
          const url = await req.uploadFile(res.tempFiles[0].tempFilePath);
          this.setData({ imageUrl: url, imagePreview: formatImageUrl(url) });
        } catch (e) {
          wx.showToast({ title: e.message || '上传失败', icon: 'none' });
        } finally {
          wx.hideLoading();
        }
      },
    });
  },

  async submit() {
    const { form, imageUrl, categoryIndex, categories } = this.data;
    if (!form.title || !form.budgetMin || !form.budgetMax) {
      wx.showToast({ title: '请填写标题和预算', icon: 'none' });
      return;
    }
    const user = auth.getUser();
    try {
      await customRequestApi.create({
        title: form.title,
        description: form.description,
        category: categories[categoryIndex].key,
        budgetMin: Number(form.budgetMin),
        budgetMax: Number(form.budgetMax),
        deadline: form.deadline || '',
        referenceImage: imageUrl || '',
        buyerId: user.id,
      });
      wx.showToast({ title: '已提交审核', icon: 'success' });
      setTimeout(() => wx.navigateBack(), 500);
    } catch (e) {
      wx.showToast({ title: e.message || '发布失败', icon: 'none' });
    }
  },
});
