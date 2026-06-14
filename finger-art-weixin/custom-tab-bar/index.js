Component({
  data: {
    selected: 0,
    list: [
      { pagePath: '/pages/home/home', text: '首页', icon: '🏠' },
      { pagePath: '/pages/marketplace/marketplace', text: '市集', icon: '🛍️' },
      { pagePath: '/pages/custom-request/custom-request', text: '需求', icon: '📋' },
      { pagePath: '/pages/skill-exchange/skill-exchange', text: '技能', icon: '🔄' },
      { pagePath: '/pages/account/account', text: '我的', icon: '👤' },
    ],
  },
  methods: {
    switchTab(e) {
      const index = e.currentTarget.dataset.index;
      const path = this.data.list[index].pagePath;
      wx.switchTab({ url: path });
    },
  },
});
