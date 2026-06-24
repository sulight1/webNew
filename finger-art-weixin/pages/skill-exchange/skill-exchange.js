const { skillApi, userApi } = require('../../services/api');
const auth = require('../../utils/auth');
const { formatImageUrl, exchangeStatusText } = require('../../utils/format');
const { getCraftCoverImage, getCraftCoverFallback, inferCraftCategoryFromText } = require('../../utils/craftCoverImages');
const { SKILL_CATEGORIES, SKILL_FILTER_OPTIONS, SKILL_SORT_TABS } = require('../../constants/skillCategories');

function todayStr() {
  const d = new Date();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${m}-${day}`;
}

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
    coverImage: getCraftCoverImage(s.category, s.title, s.description),
    coverCategory: inferCraftCategoryFromText(s.category, s.title, s.description),
  };
}

function sameUserId(a, b) {
  if (a == null || b == null || a === '' || b === '') return false;
  return String(a) === String(b);
}

function isTruthyFlag(val) {
  return val === true || val === 1 || val === '1' || val === 'true';
}

function resolveExchangeUserId(user, fallbackId) {
  if (user && user.id != null) return user.id;
  if (fallbackId != null) return fallbackId;
  return null;
}

function mapExchange(ex, currentUserId) {
  const status = String(ex.status || '').toUpperCase();
  const userA = ex.userA || {};
  const userB = ex.userB || {};
  const userAId = resolveExchangeUserId(userA, ex.userAId);
  const userBId = resolveExchangeUserId(userB, ex.userBId);
  const isUserA = sameUserId(userAId, currentUserId);
  const isUserB = sameUserId(userBId, currentUserId);
  const partner = isUserA ? userB : (isUserB ? userA : (userB.id ? userB : userA));
  const today = todayStr();
  const userAConfirmed = isTruthyFlag(ex.userAConfirmed);
  const userBConfirmed = isTruthyFlag(ex.userBConfirmed);
  const userAReviewed = isTruthyFlag(ex.userAReviewed);
  const userBReviewed = isTruthyFlag(ex.userBReviewed);

  const canAccept = status === 'REQUESTED' && isUserB;
  const canConfirm = ['ACCEPTED', 'CONFIRMED'].includes(status) && (
    (isUserA && !userAConfirmed) || (isUserB && !userBConfirmed)
  );
  const canComplete = status === 'CONFIRMED' && isUserB;
  const canReview = status === 'COMPLETED' && (
    (isUserA && !userAReviewed) || (isUserB && !userBReviewed)
  );
  const canReportNoShow = ['ACCEPTED', 'CONFIRMED'].includes(status)
    && ex.scheduleDate
    && String(ex.scheduleDate) < today;

  let waitHint = '';
  if (status === 'REQUESTED' && isUserA) {
    waitHint = '等待技能方接受请求';
  } else if (status === 'REQUESTED' && isUserB) {
    waitHint = '对方已发起交换，请点「接受请求」';
  } else if (status === 'ACCEPTED' && isUserA && userAConfirmed && !userBConfirmed) {
    waitHint = '你已确认预约，等待对方确认';
  } else if (status === 'ACCEPTED' && isUserB && userBConfirmed && !userAConfirmed) {
    waitHint = '你已确认预约，等待对方确认';
  } else if (status === 'CONFIRMED' && isUserA) {
    waitHint = '预约已锁定，等待技能方确认完成';
  } else if (status === 'CONFIRMED' && isUserB) {
    waitHint = '预约已锁定，线下完成后请点「确认完成并收款」';
  }

  return {
    id: ex.id,
    status,
    description: ex.description,
    zaowuBiCost: ex.zaowuBiCost,
    scheduleDate: ex.scheduleDate,
    userAId,
    userBId,
    userAConfirmed,
    userBConfirmed,
    partnerId: partner.id,
    partnerName: partner.username || '—',
    statusLabel: exchangeStatusText(status),
    scheduleText: ex.scheduleDate ? `预约 ${ex.scheduleDate}` : '',
    confirmText: `确认进度 A${userAConfirmed ? '✓' : '○'} / B${userBConfirmed ? '✓' : '○'}`,
    costText: ex.zaowuBiCost != null ? ex.zaowuBiCost : 10,
    canAccept: !!canAccept,
    canConfirm: !!canConfirm,
    canComplete: !!canComplete,
    canReview: !!canReview,
    canReportNoShow: !!canReportNoShow,
    isCompleted: status === 'COMPLETED',
    waitHint,
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
    exchangesLoading: false,
    actionLoadingId: null,
    showDetail: false,
    detailSkill: null,
    showExchangeBook: false,
    exchangeTarget: null,
    bookScheduleDate: '',
    bookMessage: '',
    minScheduleDate: todayStr(),
    bookSubmitting: false,
    showReview: false,
    reviewExchangeId: null,
    reviewToUserId: null,
    reviewScore: 5,
    reviewContent: '',
    reviewSubmitting: false,
    reviewStars: [1, 2, 3, 4, 5],
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 3 });
    }
    const app = getApp();
    let shouldLoadExchanges = this.data.showExchanges;
    if (app.globalData.pendingShowExchanges) {
      app.globalData.pendingShowExchanges = false;
      shouldLoadExchanges = true;
      this.setData({ showExchanges: true });
    }
    this.loadSkills();
    if (shouldLoadExchanges) {
      this.loadExchanges();
    }
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

  onCoverError(e) {
    const index = e.currentTarget.dataset.index;
    const category = e.currentTarget.dataset.category || '其它';
    const title = e.currentTarget.dataset.title || '';
    const description = e.currentTarget.dataset.description || '';
    if (index == null) return;
    const item = this.data.displaySkills[index];
    const stage = ((item && item.coverFallbackStage) || 0) + 1;
    this.setData({
      [`displaySkills[${index}].coverImage`]: getCraftCoverFallback(category, stage, title, description),
      [`displaySkills[${index}].coverFallbackStage`]: stage,
    });
  },

  onDetailCoverError() {
    if (!this.data.detailSkill) return;
    const { category, title, description } = this.data.detailSkill;
    const stage = (this.data.detailSkill.coverFallbackStage || 0) + 1;
    this.setData({
      'detailSkill.coverImage': getCraftCoverFallback(category, stage, title, description),
      'detailSkill.coverFallbackStage': stage,
    });
  },

  openExchangeBook() {
    if (!auth.isLoggedIn()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    const skill = this.data.detailSkill;
    if (!skill) return;
    const user = auth.getUser();
    if (skill.userId === user.id) {
      wx.showToast({ title: '不能交换自己的技能', icon: 'none' });
      return;
    }
    this.setData({
      showExchangeBook: true,
      exchangeTarget: skill,
      bookScheduleDate: '',
      bookMessage: `申请交换：${skill.title}`,
      minScheduleDate: todayStr(),
    });
  },

  closeExchangeBook() {
    this.setData({ showExchangeBook: false, exchangeTarget: null, bookSubmitting: false });
  },

  onBookDateChange(e) {
    this.setData({ bookScheduleDate: e.detail.value });
  },

  onBookMessageInput(e) {
    this.setData({ bookMessage: e.detail.value });
  },

  async submitExchangeBook() {
    const skill = this.data.exchangeTarget;
    if (!skill) return;
    const user = auth.getUser();
    this.setData({ bookSubmitting: true });
    try {
      const payload = {
        userAId: user.id,
        userBId: skill.userId,
        zaowuBiCost: skill.zaowuBiCost,
        description: (this.data.bookMessage || '').trim() || `申请交换：${skill.title}`,
      };
      if (this.data.bookScheduleDate) {
        payload.scheduleDate = this.data.bookScheduleDate;
      }
      await skillApi.requestExchange(payload);
      wx.showToast({ title: '已发送申请', icon: 'success' });
      this.closeExchangeBook();
      this.closeDetail();
    } catch (err) {
      wx.showToast({ title: err.message || '申请失败', icon: 'none' });
    } finally {
      this.setData({ bookSubmitting: false });
    }
  },

  async showMyExchanges() {
    if (!auth.isLoggedIn()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    this.setData({ showExchanges: true });
    await this.loadExchanges();
  },

  async loadExchanges() {
    const user = auth.getUser();
    const currentUserId = user && (user.id != null ? user.id : user.userId);
    if (!currentUserId) return;
    this.setData({ exchangesLoading: true });
    try {
      const raw = await skillApi.getMyExchanges(currentUserId);
      const exchanges = (raw || []).map((ex) => mapExchange(ex, currentUserId));
      this.setData({ exchanges });
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
      this.setData({ exchanges: [] });
    } finally {
      this.setData({ exchangesLoading: false });
    }
  },

  hideExchanges() {
    this.setData({ showExchanges: false, actionLoadingId: null });
  },

  async onExchangeAction(e) {
    const { id, action } = e.currentTarget.dataset;
    const user = auth.getUser();
    if (!user || !id) return;

    const actionMap = {
      accept: {
        fn: () => skillApi.acceptExchange(id, user.id),
        title: '接受交换',
        content: '确认接受对方的技能交换请求？',
        success: '已接受，请与对方确认预约',
      },
      confirm: {
        fn: () => skillApi.confirmExchange(id, user.id),
        title: '确认预约',
        content: '确认本次技能交换的预约时间？',
        success: '预约确认成功',
      },
      complete: {
        fn: () => skillApi.completeExchange(id, user.id),
        title: '确认完成',
        content: '确认线下交换已完成并结算造物币？',
        success: '交换已完成，造物币已结算',
      },
      noShow: {
        fn: () => skillApi.reportNoShow(id, user.id),
        title: '标记爽约',
        content: '确认对方未按约履约并标记爽约？',
        success: '已标记爽约',
      },
    };
    const cfg = actionMap[action];
    if (!cfg) return;

    wx.showModal({
      title: cfg.title,
      content: cfg.content,
      success: async (res) => {
        if (!res.confirm) return;
        this.setData({ actionLoadingId: Number(id) });
        try {
          await cfg.fn();
          wx.showToast({ title: cfg.success, icon: 'success' });
          if (action === 'complete') {
            try {
              const profile = await userApi.getProfile(user.id);
              getApp().setUser(profile);
            } catch (_) {}
            const item = this.data.exchanges.find((ex) => ex.id === Number(id));
            if (item) {
              this.openReview({
                currentTarget: {
                  dataset: { id: item.id, partnerId: item.partnerId },
                },
              });
            }
          }
          await this.loadExchanges();
        } catch (err) {
          wx.showToast({ title: err.message || '操作失败', icon: 'none' });
        } finally {
          this.setData({ actionLoadingId: null });
        }
      },
    });
  },

  async openReview(e) {
    const exchangeId = Number(e.currentTarget.dataset.id);
    const partnerId = Number(e.currentTarget.dataset.partnerId);
    const user = auth.getUser();
    if (!user || !exchangeId || !partnerId) return;
    try {
      const already = await skillApi.checkExchangeReview(exchangeId, user.id);
      if (already) {
        wx.showToast({ title: '您已评价过该交换', icon: 'none' });
        return;
      }
    } catch (_) {}
    this.setData({
      showReview: true,
      reviewExchangeId: exchangeId,
      reviewToUserId: partnerId,
      reviewScore: 5,
      reviewContent: '',
    });
  },

  closeReview() {
    this.setData({
      showReview: false,
      reviewExchangeId: null,
      reviewToUserId: null,
      reviewSubmitting: false,
    });
  },

  onReviewScoreTap(e) {
    this.setData({ reviewScore: Number(e.currentTarget.dataset.score) });
  },

  onReviewContentInput(e) {
    this.setData({ reviewContent: e.detail.value });
  },

  async submitReview() {
    const user = auth.getUser();
    const { reviewExchangeId, reviewToUserId, reviewScore, reviewContent } = this.data;
    if (!user || !reviewExchangeId || !reviewToUserId) return;
    this.setData({ reviewSubmitting: true });
    try {
      await skillApi.submitReview({
        exchangeId: reviewExchangeId,
        fromUserId: user.id,
        toUserId: reviewToUserId,
        score: reviewScore,
        content: reviewContent,
      });
      wx.showToast({ title: '评价已提交', icon: 'success' });
      this.closeReview();
      await this.loadExchanges();
    } catch (err) {
      wx.showToast({ title: err.message || '提交失败', icon: 'none' });
    } finally {
      this.setData({ reviewSubmitting: false });
    }
  },

  stopPropagation() {},
});
