const { productApi } = require('../../services/api');
const { PRODUCT_CATEGORIES } = require('../../constants/categories');
const { formatImageUrl } = require('../../utils/format');

Page({
  data: {
    categories: [{ key: 'all', label: '全部', emoji: '✨' }, ...PRODUCT_CATEGORIES],
    activeCategory: 'all',
    keyword: '',
    products: [],
    loading: false,
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 1 });
    }
    this.loadProducts();
  },

  onPullDownRefresh() {
    this.loadProducts().finally(() => wx.stopPullDownRefresh());
  },

  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value });
  },

  onSearch() {
    this.loadProducts();
  },

  onCategoryTap(e) {
    this.setData({ activeCategory: e.currentTarget.dataset.key });
    this.loadProducts();
  },

  async loadProducts() {
    this.setData({ loading: true });
    try {
      let list = [];
      const { keyword, activeCategory } = this.data;
      if (keyword.trim()) {
        list = await productApi.searchProducts(keyword.trim());
      } else {
        const params = { scope: 'approved' };
        if (activeCategory !== 'all') params.category = activeCategory;
        list = await productApi.getProducts(params);
      }
      const products = (list || [])
        .filter((p) => p.status === 'APPROVED')
        .map((p) => ({ ...p, image: formatImageUrl(p.image) }));
      this.setData({ products });
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },

  goDetail(e) {
    wx.navigateTo({ url: `/pages/product-detail/product-detail?id=${e.currentTarget.dataset.id}` });
  },
});
