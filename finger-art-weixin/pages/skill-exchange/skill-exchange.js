const { skillApi } = require('../../services/api');
const auth = require('../../utils/auth');
const { formatImageUrl } = require('../../utils/format');

Page({
  data: {
    skills: [],
    loading: false,
    showForm: false,
    form: { title: '', description: '', category: '钩织', zaowuBiCost: '10' },
    showExchanges: false,
    exchanges: [],
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 3 });
    }
    this.loadSkills();
  },

  onPullDownRefresh() {
    this.loadSkills().finally(() => wx.stopPullDownRefresh());
  },

  async loadSkills() {
    this.setData({ loading: true });
    try {
      const skills = await skillApi.getSkills({ status: 'APPROVED' });
      this.setData({
        skills: (skills || []).map((s) => ({
          ...s,
          avatar: formatImageUrl(s.avatar),
        })),
      });
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },

  toggleForm() {
    if (!auth.isLoggedIn()) {
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
      });
    } catch (e) {
      wx.showToast({ title: e.message || '发布失败', icon: 'none' });
    }
  },

  async onExchange(e) {
    if (!auth.isLoggedIn()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    const skill = this.data.skills.find((s) => s.id === e.currentTarget.dataset.id);
    const user = auth.getUser();
    if (skill.userId === user.id) {
      wx.showToast({ title: '不能交换自己的技能', icon: 'none' });
      return;
    }
    wx.showModal({
      title: '申请交换',
      editable: true,
      placeholderText: '说明你想交换的内容',
      success: async (res) => {
        if (!res.confirm) return;
        try {
          await skillApi.requestExchange({
            userAId: user.id,
            userBId: skill.userId,
            description: res.content || `申请交换：${skill.title}`,
            zaowuBiCost: skill.zaowuBiCost || 10,
            scheduleDate: new Date().toISOString().slice(0, 10),
          });
          wx.showToast({ title: '申请已提交', icon: 'success' });
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
      this.setData({ exchanges: exchanges || [], showExchanges: true });
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
    }
  },

  hideExchanges() {
    this.setData({ showExchanges: false });
  },
});
