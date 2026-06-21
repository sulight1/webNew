const { customRequestApi } = require('../../services/api');
const auth = require('../../utils/auth');
const { formatImageUrl } = require('../../utils/format');
const { getCraftCoverImage, DEFAULT_CRAFT_COVER } = require('../../utils/craftCoverImages');
const {
  CATEGORY_FILTER_OPTIONS,
  REQUEST_SORT_TABS,
  getRequestStatusLabel,
} = require('../../constants/requestCategories');

const PAGE_SIZE = 12;

function mapRequestItem(item) {
  const status = item.status || 'OPEN';
  let statusClass = 'ended';
  if (status === 'OPEN') statusClass = 'open';
  else if (status === 'MATCHED') statusClass = 'matched';
  const coverImage = item.referenceImage
    ? formatImageUrl(item.referenceImage)
    : getCraftCoverImage(item.category);
  const user = auth.getUser();
  const buyerId = item.buyer ? item.buyer.id : item.buyerId;
  return {
    id: item.id,
    title: item.title,
    category: item.category,
    description: item.description,
    budgetMin: item.budgetMin,
    budgetMax: item.budgetMax,
    deadline: item.deadline,
    deadlineText: item.deadline ? ('期望 ' + item.deadline) : '工期待定',
    status: item.status,
    referenceImage: item.referenceImage,
    buyer: item.buyer,
    buyerName: item.buyer ? item.buyer.username : (item.buyerName || ''),
    buyerId,
    coverImage,
    statusLabel: getRequestStatusLabel(status),
    statusClass,
    isOwn: user && buyerId === user.id,
  };
}

Page({
  data: {
    scopeTabs: [
      { key: 'hall', label: '需求大厅' },
      { key: 'mine', label: '我的需求' },
    ],
    scope: 'hall',
    sortTabs: REQUEST_SORT_TABS,
    requestSort: 'latest',
    categoryLabels: CATEGORY_FILTER_OPTIONS.map((c) => c.label),
    categoryValues: CATEGORY_FILTER_OPTIONS.map((c) => c.value),
    categoryIndex: 0,
    filterCategory: 'all',
    keyword: '',
    list: [],
    total: 0,
    currentPage: 1,
    hasMore: false,
    loadingMore: false,
    showSkeleton: false,
    listRefreshing: false,
    showEmpty: false,
    hasLoadedOnce: false,
    hasActiveFilters: false,
    emptyText: '还没有定制需求',
    skeletonItems: [1, 2, 3, 4, 5, 6],
    showDetail: false,
    detailItem: null,
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 2 });
    }
    this.loadList(true);
  },

  onPullDownRefresh() {
    const done = () => wx.stopPullDownRefresh();
    this.loadList(true).then(done).catch(done);
  },

  onReachBottom() {
    if (this.data.scope === 'mine' || !this.data.hasMore || this.data.loadingMore) return;
    this.loadMore();
  },

  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value });
  },

  onSearch() {
    this.loadList(true);
  },

  onScopeChange(e) {
    this.setData({ scope: e.currentTarget.dataset.key });
    this.loadList(true);
  },

  onCategoryPick(e) {
    const index = Number(e.detail.value);
    this.setData({
      categoryIndex: index,
      filterCategory: this.data.categoryValues[index],
    });
    this.loadList(true);
  },

  onSortTap(e) {
    this.setData({ requestSort: e.currentTarget.dataset.value });
    this.loadList(true);
  },

  clearFilters() {
    this.setData({ keyword: '', filterCategory: 'all', categoryIndex: 0 });
    this.loadList(true);
  },

  onCoverError(e) {
    const index = e.currentTarget.dataset.index;
    const key = `list[${index}].coverImage`;
    this.setData({ [key]: DEFAULT_CRAFT_COVER });
  },

  async loadList(resetPage) {
    const firstLoad = !this.data.hasLoadedOnce;
    this.setData({
      showSkeleton: firstLoad,
      listRefreshing: !firstLoad && resetPage !== false,
    });
    const hasActiveFilters = this.data.filterCategory !== 'all' || !!this.data.keyword.trim();
    try {
      if (this.data.scope === 'mine') {
        await this.loadMineList();
      } else {
        await this.loadHallList(resetPage !== false);
      }
      this.setData({
        hasActiveFilters,
        emptyText: hasActiveFilters ? '未找到匹配需求' : '还没有定制需求，来发布第一个吧',
      });
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
      this.setData({ list: [], total: 0, showEmpty: true });
    } finally {
      this.setData({
        showSkeleton: false,
        listRefreshing: false,
        hasLoadedOnce: true,
      });
    }
  },

  async loadMineList() {
    const user = auth.getUser();
    if (!user) {
      this.setData({ list: [], total: 0, showEmpty: true, hasMore: false });
      return;
    }
    let raw = await customRequestApi.list();
    const list = (raw || [])
      .filter((item) => {
        const buyerId = item.buyer ? item.buyer.id : item.buyerId;
        return buyerId === user.id;
      })
      .map(mapRequestItem);
    this.setData({
      list,
      total: list.length,
      showEmpty: list.length === 0,
      hasMore: false,
    });
  },

  async loadHallList(resetPage) {
    const page = resetPage ? 1 : this.data.currentPage;
    const res = await customRequestApi.search({
      status: 'OPEN',
      category: this.data.filterCategory === 'all' ? undefined : this.data.filterCategory,
      keyword: this.data.keyword.trim() || undefined,
      sort: this.data.requestSort,
      page,
      size: PAGE_SIZE,
    });
    // 兼容旧接口返回数组
    const rawItems = Array.isArray(res) ? res : (res.items || []);
    const total = Array.isArray(res) ? rawItems.length : (res.total ?? rawItems.length);
    const items = rawItems.map(mapRequestItem);
    const list = resetPage ? items : this.data.list.concat(items);
    this.setData({
      list,
      total,
      currentPage: page,
      showEmpty: total === 0,
      hasMore: list.length < total,
    });
  },

  async loadMore() {
    if (this.data.scope === 'mine' || !this.data.hasMore) return;
    this.setData({ loadingMore: true, currentPage: this.data.currentPage + 1 });
    try {
      await this.loadHallList(false);
    } finally {
      this.setData({ loadingMore: false });
    }
  },

  openDetail(e) {
    const item = this.data.list[e.currentTarget.dataset.index];
    if (!item) return;
    this.setData({ showDetail: true, detailItem: item });
  },

  closeDetail() {
    this.setData({ showDetail: false, detailItem: null });
  },

  stopPropagation() {},

  goPublish() {
    if (!auth.isLoggedIn()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    wx.navigateTo({ url: '/pages/publish-request/publish-request' });
  },

  onBidFromDetail() {
    const item = this.data.detailItem;
    if (!item) return;
    this.closeDetail();
    this.onBid({ currentTarget: { dataset: { id: item.id } } });
  },

  showBidsFromDetail() {
    const item = this.data.detailItem;
    if (!item) return;
    this.closeDetail();
    this.showBids({ currentTarget: { dataset: { id: item.id } } });
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
          this.loadList(true);
        } catch (err) {
          wx.showToast({ title: err.message || '报价失败', icon: 'none' });
        }
      },
    });
  },

  async onSelectBid(id, bidId) {
    const user = auth.getUser();
    try {
      await customRequestApi.selectBid(id, user.id, bidId);
      wx.showToast({ title: '已选定匠人', icon: 'success' });
      this.loadList(true);
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
      const itemList = bids.map((b) => `${b.artisanUsername || b.artisanName || '匠人'}: ${b.message || '报价'}`);
      wx.showActionSheet({
        itemList,
        success: (res) => {
          const bid = bids[res.tapIndex];
          if (bid) {
            wx.showModal({
              title: '确认选定',
              content: `选定 ${bid.artisanUsername || bid.artisanName || '匠人'}？`,
              success: (r) => {
                if (r.confirm) this.onSelectBid(id, bid.id);
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
