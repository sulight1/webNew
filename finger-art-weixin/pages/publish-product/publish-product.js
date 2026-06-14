const { productApi } = require('../../services/api');
const auth = require('../../utils/auth');
const { PRODUCT_CATEGORIES } = require('../../constants/categories');
const req = require('../../utils/request');

Page({
  data: {
    categories: PRODUCT_CATEGORIES,
    categoryIndex: 0,
    form: {
      title: '',
      price: '',
      description: '',
      type: 'READY_MADE',
      stock: '1',
    },
    imageUrl: '',
  },

  onLoad() {
    if (!auth.isLoggedIn()) {
      wx.navigateTo({ url: '/pages/login/login' });
    }
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ [`form.${field}`]: e.detail.value });
  },

  onCategoryChange(e) {
    this.setData({ categoryIndex: Number(e.detail.value) });
  },

  onTypeChange(e) {
    const type = e.detail.value;
    this.setData({
      'form.type': type,
      'form.stock': type === 'READY_MADE' ? '1' : '999',
    });
  },

  chooseImage() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      success: async (res) => {
        try {
          wx.showLoading({ title: '上传中' });
          const url = await req.uploadFile(res.tempFiles[0].tempFilePath);
          this.setData({ imageUrl: url });
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
    if (!form.title || !form.price || !imageUrl) {
      wx.showToast({ title: '请填写标题、价格并上传封面', icon: 'none' });
      return;
    }
    const user = auth.getUser();
    const stock = Number(form.stock);
    if (form.type === 'READY_MADE') {
      if (!form.stock || !Number.isInteger(stock) || stock < 1) {
        wx.showToast({ title: '请输入有效库存（至少1）', icon: 'none' });
        return;
      }
      if (stock > 99) {
        wx.showToast({ title: '成品库存最多 99', icon: 'none' });
        return;
      }
    }
    try {
      await productApi.createProduct({
        title: form.title,
        price: Number(form.price),
        description: form.description,
        type: form.type,
        category: categories[categoryIndex].key,
        image: imageUrl,
        creatorId: user.id,
        creator: user.username,
        creatorAvatar: user.avatar,
        status: 'PENDING',
        stock: form.type === 'READY_MADE' ? stock : (stock > 0 ? stock : 999),
      });
      wx.showToast({ title: '已提交审核', icon: 'success' });
      setTimeout(() => wx.navigateBack(), 500);
    } catch (e) {
      wx.showToast({ title: e.message || '发布失败', icon: 'none' });
    }
  },
});
