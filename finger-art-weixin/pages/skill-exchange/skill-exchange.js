const { skillApi } = require('../../services/api');
const auth = require('../../utils/auth');
const { formatImageUrl } = require('../../utils/format');
const { getCraftCoverImage } = require('../../utils/craftCoverImages');
const { SKILL_CATEGORIES, SKILL_FILTER_OPTIONS, SKILL_SORT_TABS } = require('../../constants/skillCategories');

function mapSkillItem(s) {
  return {
    id: s.id,
    userId: s.userId,
    username: s.username,
    avatar: formatImageUrl(s.avatar),
    title: s.title,
    description: s.description,
    category: s.category,
    duration: s.duration,
    zaowuBiCost: s.zaowuBiCost,
    rating: s.rating,
    credit: s.credit,
    exchangeCount: s.exchangeCount,
    coverImage: getCraftCoverImage(s.category),
  };
}

Page({
  data: {
    skills: [],
    displaySkills: [],
    sortTabs: SKILL_SORT_TABS,
    skillSort: 'recommend',
    skillFilter: 'all',
    categoryLabels: SKILL_FILTER_OPTIONS.map((c) => c.label),
    categoryValues: SKILL_FILTER_OPTIONS.map((c) => c.value),
    categoryIndex: 0,
    publishCategoryLabels: SKILL_CATEGORIES.map((c) => c.label),
    publishCategoryValues: SKILL_CATEGORIES.map((c) => c.value),
    publishCategoryIndex: 0,
    keyword: '',
    loading: false,
    showSkeleton: false,
    listRefreshing: false,
    showEmpty: false,
    hasLoadedOnce: false,
    skeletonItems: [1, 2, 3, 4, 5, 6],
    showForm: false,
    form: { title: '', description: '', category: '钩织', zaowuBiCost: '10' },
    showExchanges: false,
    exchanges: [],
    showDetail: false,
    detailSkill: null,
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 3 });
    }
    this.loadSkills();
  },

  onPullDownRefresh() {
    const done = () => wx.stopPullDownRefresh();
    this.loadSkills().then(done).catch(done);
  },

  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value });
    this.applyFilter();
  },

  onSearch() {
    this.applyFilter();
  },

  clearSearch() {
    this.setData({ keyword: '' });
    this.applyFilter();
  },

  onCategoryPick(e) {
    const index = Number(e.detail.value);
    this.setData({
      categoryIndex: index,
      skillFilter: this.data.categoryValues[index],
    });
    this.loadSkills();
  },

  onCategoryFormPick(e) {
    const index = Number(e.detail.value);
    this.setData({
      publishCategoryIndex: index,
      'form.category': this.data.publishCategoryValues[index],
    });
  },

  onSortTap(e) {
    this.setData({ skillSort: e.currentTarget.dataset.value });
    this.sortAndDisplay();
  },

  sortSkills(list) {
    const sorted = [...list];
    switch (this.data.skillSort) {
      case 'credit':
        sorted.sort((a, b) => (b.credit || 100) - (a.credit || 100));
        break;
      case 'rating':
        sorted.sort((a, b) => (b.rating || 5) - (a.rating || 5));
        break;
      case 'latest':
        sorted.sort((a, b) => (b.id || 0) - (a.id || 0));
        break;
      default:
        sorted.sort((a, b) => {
          const diff = (b.exchangeCount || 0) - (a.exchangeCount || 0);
          if (diff !== 0) return diff;
          return (b.credit || 100) - (a.credit || 100);
        });
        break;
    }
    return sorted;
  },

  applyFilter() {
    const q = (this.data.keyword || '').trim().toLowerCase();
    let list = this.data.skills;
    if (q) {
      list = list.filter(
        (item) =>
          (item.title || '').toLowerCase().includes(q)
          || (item.description || '').toLowerCase().includes(q)
          || (item.username || '').toLowerCase().includes(q)
          || (item.category || '').toLowerCase().includes(q),
      );
    }
    this.setData({
      displaySkills: list,
      showEmpty: this.data.hasLoadedOnce && !this.data.listRefreshing && list.length === 0,
    });
  },

  sortAndDisplay() {
    const sorted = this.sortSkills(this.data.skills);
    this.setData({ skills: sorted }, () => this.applyFilter());
  },

  async loadSkills() {
    const firstLoad = !this.data.hasLoadedOnce;
    this.setData({
      showSkeleton: firstLoad,
      listRefreshing: !firstLoad,
    });
    try {
      const category = this.data.skillFilter === 'all' ? undefined : this.data.skillFilter;
      const params = { status: 'APPROVED' };
      if (category) params.category = category;
      const raw = await skillApi.getSkills(params);
      const skills = this.sortSkills((raw || []).map(mapSkillItem));
      this.setData({ skills });
      this.applyFilter();
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
      this.setData({ skills: [], displaySkills: [], showEmpty: true });
    } finally {
      this.setData({
        showSkeleton: false,
        listRefreshing: false,
        hasLoadedOnce: true,
      }, () => this.applyFilter());
    }
  },

  toggleForm() {
    if (!this.data.showForm && !auth.isLoggedIn()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    this.setData({ showForm: !this.data.showForm });
  },

  goUserProfile(e) {
    const userId = e.currentTarget.dataset.userid;
    if (!userId) return;
    wx.navigateTo({ url: `/pages/artisan-profile/artisan-profile?id=${userId}` });
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ [`form.${field}`]: e.detail.value });
  },

  async submitForm() {
    const { form } = this.data;
    if (!form.title || !form.description) {
      wx.showToast({ title: '请填写完整', icon: 'none' });
      return;
    }
    try {
      const user = auth.getUser();
      await skillApi.addSkill({
        userId: user.id,
        username: user.username,
        title: form.title,
        description: form.description,
        category: form.category,
        zaowuBiCost: Number(form.zaowuBiCost) || 10,
        duration: '1小时',
        status: 'PENDING',
      });
      wx.showToast({ title: '已提交审核', icon: 'success' });
      this.setData({
        showForm: false,
        form: { title: '', description: '', category: '钩织', zaowuBiCost: '10' },
        publishCategoryIndex: 0,
      });
    } catch (e) {
      wx.showToast({ title: e.message || '提交失败', icon: 'none' });
    }
  },

  openDetail(e) {
    const item = this.data.displaySkills[e.currentTarget.dataset.index];
    if (!item) return;
    this.setData({ showDetail: true, detailSkill: item });
  },

  closeDetail() {
    this.setData({ showDetail: false, detailSkill: null });
  },

  onExchangeFromDetail() {
    const item = this.data.detailSkill;
    if (!item) return;
    this.closeDetail();
    this.onExchange({ currentTarget: { dataset: { id: item.id } } });
  },

  async onExchange(e) {
    if (!auth.isLoggedIn()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    const skillId = e.currentTarget.dataset.id;
    const skill = this.data.skills.find((s) => s.id === skillId) || this.data.displaySkills.find((s) => s.id === skillId);
    if (!skill) return;
    const user = auth.getUser();
    if (skill.userId === user.id) {
      wx.showToast({ title: '不能交换自己的技能', icon: 'none' });
      return;
    }
    wx.showModal({
      title: '申请交换',
      content: `向 ${skill.username} 申请交换「${skill.title}」？`,
      success: async (res) => {
        if (!res.confirm) return;
        try {
          await skillApi.requestExchange({
            userAId: user.id,
            userBId: skill.userId,
            zaowuBiCost: skill.zaowuBiCost,
            description: `申请交换：${skill.title}`,
          });
          wx.showToast({ title: '已发送申请', icon: 'success' });
        } catch (err) {
          wx.showToast({ title: err.message || '申请失败', icon: 'none' });
        }
      },
    });
  },

  async showMyExchanges() {
    if (!auth.isLoggedIn()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    try {
      const user = auth.getUser();
      const exchanges = await skillApi.getMyExchanges(user.id);
      this.setData({ showExchanges: true, exchanges: exchanges || [] });
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
    }
  },

  hideExchanges() {
    this.setData({ showExchanges: false });
  },

  stopPropagation() {},
});
