const { productApi } = require('../../services/api');
const auth = require('../../utils/auth');
const cartUtil = require('../../utils/cart');
const { PRODUCT_CATEGORIES } = require('../../constants/categories');
const { formatImageUrl } = require('../../utils/format');

const SORT_TABS = [
  { value: 'recommend', label: '推荐' },
  { value: 'newest', label: '最新' },
  { value: 'price-asc', label: '价格低' },
  { value: 'price-desc', label: '价格高' },
];

const filterCategories = [
  { key: 'all', label: '全部作品' },
  ...PRODUCT_CATEGORIES.map((c) => ({ key: c.key, label: `${c.emoji} ${c.label}` })),
];

Page({
  data: {
    sortTabs: SORT_TABS,
    sortBy: 'recommend',
    categoryLabels: filterCategories.map((c) => c.label),
    categoryKeys: filterCategories.map((c) => c.key),
    categoryIndex: 0,
    activeCategory: 'all',
    keyword: '',
    allProducts: [],
    displayProducts: [],
    displayTotal: 0,
    currentPage: 1,
    pageSize: 12,
    hasMore: false,
    showSkeleton: false,
    listRefreshing: false,
    showEmpty: false,
    hasLoadedOnce: false,
    skeletonItems: [1, 2, 3, 4, 5, 6],
    cartCount: 0,
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 1 });
    }
    this.setData({ cartCount: cartUtil.getTotalCount() });
    this.loadProducts(true);
  },

  onPullDownRefresh() {
    const done = () => wx.stopPullDownRefresh();
    this.loadProducts(true).then(done).catch(done);
  },

  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value });
  },

  onSearch() {
    this.loadProducts(true);
  },

  clearSearch() {
    this.setData({ keyword: '' });
    this.loadProducts(true);
  },

  onCategoryPick(e) {
    const index = Number(e.detail.value);
    this.setData({
      categoryIndex: index,
      activeCategory: this.data.categoryKeys[index],
    });
    this.loadProducts(true);
  },

  onSortTap(e) {
    this.setData({ sortBy: e.currentTarget.dataset.value });
    this.applyDisplay(true);
  },

  loadMore() {
    this.applyDisplay(false);
  },

  sortProducts(list) {
    const sorted = [...list];
    switch (this.data.sortBy) {
      case 'newest':
        sorted.sort((a, b) => (b.id || 0) - (a.id || 0));
        break;
      case 'price-asc':
        sorted.sort((a, b) => Number(a.price) - Number(b.price));
        break;
      case 'price-desc':
        sorted.sort((a, b) => Number(b.price) - Number(a.price));
        break;
      default:
        sorted.sort((a, b) => (b.likes || 0) - (a.likes || 0));
        break;
    }
    return sorted;
  },

  applyDisplay(resetPage) {
    const sorted = this.sortProducts(this.data.allProducts);
    const page = resetPage ? 1 : this.data.currentPage + 1;
    const end = page * this.data.pageSize;
    const slice = sorted.slice(0, end);
    this.setData({
      displayProducts: slice,
      displayTotal: sorted.length,
      currentPage: page,
      hasMore: end < sorted.length,
      showEmpty: sorted.length === 0,
    });
  },

  async loadProducts(resetPage) {
    const firstLoad = !this.data.hasLoadedOnce;
    this.setData({
      showSkeleton: firstLoad,
      listRefreshing: !firstLoad,
    });
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
      const allProducts = (list || [])
        .filter((p) => p.status === 'APPROVED')
        .map((p) => ({
          ...p,
          image: formatImageUrl(p.image),
          creatorAvatar: formatImageUrl(p.creatorAvatar),
          typeLabel: p.type === 'READY_MADE' ? '成品' : '可定制',
        }));
      this.setData({ allProducts });
      this.applyDisplay(resetPage !== false);
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
      this.setData({ allProducts: [], showEmpty: true, displayProducts: [], displayTotal: 0 });
    } finally {
      this.setData({
        showSkeleton: false,
        listRefreshing: false,
        hasLoadedOnce: true,
      });
    }
  },

  goDetail(e) {
    wx.navigateTo({ url: `/pages/product-detail/product-detail?id=${e.currentTarget.dataset.id}` });
  },

  goCart() {
    if (!auth.isLoggedIn()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    wx.navigateTo({ url: '/pages/cart/cart' });
  },
});
