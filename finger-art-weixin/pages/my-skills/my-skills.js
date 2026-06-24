const { skillApi, userApi } = require('../../services/api');
const auth = require('../../utils/auth');
const { getCraftCoverImage, getCraftCoverFallback, inferCraftCategoryFromText } = require('../../utils/craftCoverImages');
const { exchangeStatusText } = require('../../utils/format');
const { SKILL_CATEGORIES } = require('../../constants/skillCategories');

const STATUS_MAP = {
  PENDING: { label: '审核中', cls: 'pending' },
  APPROVED: { label: '已上架', cls: 'approved' },
  REJECTED: { label: '未通过', cls: 'rejected' },
};

function todayStr() {
  const d = new Date();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${m}-${day}`;
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

function mapSkillItem(item) {
  const status = item.status || 'PENDING';
  const statusMeta = STATUS_MAP[status] || { label: status, cls: 'pending' };
  return {
    id: item.id,
    title: item.title,
    description: item.description,
    category: item.category,
    duration: item.duration,
    zaowuBiCost: item.zaowuBiCost != null ? item.zaowuBiCost : 10,
    status,
    statusLabel: statusMeta.label,
    statusClass: statusMeta.cls,
    coverImage: getCraftCoverImage(item.category, item.title, item.description),
    coverCategory: inferCraftCategoryFromText(item.category, item.title, item.description),
  };
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
  if (status === 'REQUESTED' && isUserA) waitHint = '等待技能方接受请求';
  else if (status === 'REQUESTED' && isUserB) waitHint = '对方已发起交换，请点「接受请求」';
  else if (status === 'ACCEPTED' && isUserA && userAConfirmed && !userBConfirmed) waitHint = '你已确认预约，等待对方确认';
  else if (status === 'ACCEPTED' && isUserB && userBConfirmed && !userAConfirmed) waitHint = '你已确认预约，等待对方确认';
  else if (status === 'CONFIRMED' && isUserA) waitHint = '预约已锁定，等待技能方确认完成';
  else if (status === 'CONFIRMED' && isUserB) waitHint = '预约已锁定，线下完成后请点「确认完成并收款」';

  return {
    id: ex.id,
    status,
    description: ex.description,
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
    hubTabs: [
      { key: 'published', label: '我发布的' },
      { key: 'exchanges', label: '交换记录' },
    ],
    hubTab: 'published',
    scopeTabs: [
      { key: 'all', label: '全部' },
      { key: 'PENDING', label: '审核中' },
      { key: 'APPROVED', label: '已上架' },
      { key: 'REJECTED', label: '未通过' },
    ],
    scope: 'all',
    allList: [],
    list: [],
    loading: false,
    exchanges: [],
    exchangesLoading: false,
    actionLoadingId: null,
    showEdit: false,
    saving: false,
    editForm: {
      id: null,
      title: '',
      description: '',
      category: '钩织',
      duration: '2小时',
      zaowuBiCost: '10',
      status: 'PENDING',
    },
    categoryLabels: SKILL_CATEGORIES.map((c) => c.label),
    categoryValues: SKILL_CATEGORIES.map((c) => c.value),
    editCategoryIndex: 0,
    showReview: false,
    reviewExchangeId: null,
    reviewToUserId: null,
    reviewScore: 5,
    reviewContent: '',
    reviewSubmitting: false,
    reviewStars: [1, 2, 3, 4, 5],
  },

  onShow() {
    if (!auth.isLoggedIn()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    const app = getApp();
    if (app.globalData.pendingSkillHubTab === 'exchanges') {
      app.globalData.pendingSkillHubTab = null;
      this.setData({ hubTab: 'exchanges' }, () => this.loadHubData());
      return;
    }
    this.loadHubData();
  },

  onPullDownRefresh() {
    this.loadHubData().finally(() => wx.stopPullDownRefresh());
  },

  loadHubData() {
    if (this.data.hubTab === 'exchanges') {
      return this.loadExchanges();
    }
    return this.loadList();
  },

  onHubTabChange(e) {
    const hubTab = e.currentTarget.dataset.key;
    this.setData({ hubTab }, () => this.loadHubData());
  },

  applyFilter() {
    const { scope, allList } = this.data;
    const list = scope === 'all'
      ? allList
      : allList.filter((item) => item.status === scope);
    this.setData({ list });
  },

  async loadList() {
    if (!auth.isLoggedIn()) return;
    this.setData({ loading: true });
    try {
      const raw = await skillApi.getMySkills();
      const allList = (raw || []).map(mapSkillItem);
      this.setData({ allList }, () => this.applyFilter());
    } catch (e) {
      const msg = e.message || '加载失败';
      if (msg.includes('登录') || msg.includes('401')) {
        wx.showToast({ title: '登录已过期，请重新登录', icon: 'none' });
        setTimeout(() => wx.navigateTo({ url: '/pages/login/login' }), 800);
      } else {
        wx.showToast({ title: msg, icon: 'none' });
      }
      this.setData({ allList: [], list: [] });
    } finally {
      this.setData({ loading: false });
    }
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

  onScopeChange(e) {
    this.setData({ scope: e.currentTarget.dataset.key }, () => this.applyFilter());
  },

  onCoverError(e) {
    const index = e.currentTarget.dataset.index;
    const category = e.currentTarget.dataset.category || '其它';
    const title = e.currentTarget.dataset.title || '';
    const description = e.currentTarget.dataset.description || '';
    if (index == null) return;
    const item = this.data.list[index];
    const stage = ((item && item.coverFallbackStage) || 0) + 1;
    this.setData({
      [`list[${index}].coverImage`]: getCraftCoverFallback(category, stage, title, description),
      [`list[${index}].coverFallbackStage`]: stage,
    });
  },

  goPublish() {
    wx.switchTab({ url: '/pages/skill-exchange/skill-exchange' });
  },

  goUserProfile(e) {
    const userId = e.currentTarget.dataset.userid;
    if (!userId) return;
    wx.navigateTo({ url: `/pages/artisan-profile/artisan-profile?id=${userId}` });
  },

  openEdit(e) {
    const id = Number(e.currentTarget.dataset.id);
    const item = this.data.allList.find((s) => s.id === id);
    if (!item) return;
    const categoryIndex = Math.max(0, this.data.categoryValues.indexOf(item.category));
    this.setData({
      showEdit: true,
      editCategoryIndex: categoryIndex,
      editForm: {
        id: item.id,
        title: item.title,
        description: item.description || '',
        category: item.category || '钩织',
        duration: item.duration || '2小时',
        zaowuBiCost: String(item.zaowuBiCost != null ? item.zaowuBiCost : 10),
        status: item.status,
      },
    });
  },

  closeEdit() {
    this.setData({ showEdit: false, saving: false });
  },

  stopPropagation() {},

  onEditInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ [`editForm.${field}`]: e.detail.value });
  },

  onEditCategoryPick(e) {
    const index = Number(e.detail.value);
    this.setData({
      editCategoryIndex: index,
      'editForm.category': this.data.categoryValues[index],
    });
  },

  async submitEdit() {
    const { editForm } = this.data;
    const user = auth.getUser();
    if (!user || !editForm.id) return;
    if (!editForm.title || !editForm.description) {
      wx.showToast({ title: '请填写标题和描述', icon: 'none' });
      return;
    }
    this.setData({ saving: true });
    try {
      await skillApi.updateSkill(editForm.id, {
        userId: user.id,
        title: editForm.title.trim(),
        description: editForm.description.trim(),
        category: editForm.category,
        duration: editForm.duration || '2小时',
        zaowuBiCost: Number(editForm.zaowuBiCost) || 10,
      });
      wx.showToast({ title: '已保存，将重新审核', icon: 'success' });
      this.setData({ showEdit: false });
      await this.loadList();
    } catch (e) {
      wx.showToast({ title: e.message || '保存失败', icon: 'none' });
    } finally {
      this.setData({ saving: false });
    }
  },

  onDelete(e) {
    const id = Number(e.currentTarget.dataset.id);
    const user = auth.getUser();
    if (!user || !id) return;
    wx.showModal({
      title: '删除技能',
      content: '确定删除该技能吗？删除后不可恢复。',
      confirmColor: '#ff4d4f',
      success: async (res) => {
        if (!res.confirm) return;
        try {
          await skillApi.deleteSkill(id, user.id);
          wx.showToast({ title: '已删除', icon: 'success' });
          await this.loadList();
        } catch (err) {
          wx.showToast({ title: err.message || '删除失败', icon: 'none' });
        }
      },
    });
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
    } catch (e) {
      wx.showToast({ title: e.message || '提交失败', icon: 'none' });
    } finally {
      this.setData({ reviewSubmitting: false });
    }
  },
});
