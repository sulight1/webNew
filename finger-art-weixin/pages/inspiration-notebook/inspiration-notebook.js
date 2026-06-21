const notebook = require('../../utils/inspiration-notebook');
const auth = require('../../utils/auth');
const inspirePayload = require('../../utils/inspire-payload');
const { formatImageUrl } = require('../../utils/format');

function formatTime(iso) {
  if (!iso) return '';
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return '';
  const p = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`;
}

Page({
  data: {
    items: [],
  },

  onShow() {
    this.loadItems();
  },

  loadItems() {
    const user = auth.getUser();
    const userId = user && user.id;
    const items = notebook.list(userId).map((item) => ({
      ...item,
      imagePreview: item.imageUrl ? formatImageUrl(item.imageUrl) : '',
      savedAtText: formatTime(item.savedAt),
    }));
    this.setData({ items });
  },

  goGacha() {
    wx.navigateTo({ url: '/pages/inspiration-gacha/inspiration-gacha' });
  },

  buildPayload(item) {
    return {
      title: item.title,
      description: item.description,
      category: item.category,
      suggestedPrice: item.suggestedPrice,
      imageUrl: item.imageUrl || '',
      copy: item.copy,
      styleLabel: item.styleLabel,
      paletteName: item.paletteName,
    };
  },

  useProduct(e) {
    const item = e.currentTarget.dataset.item;
    inspirePayload.setPayload({ ...this.buildPayload(item), target: 'product' });
    wx.navigateTo({ url: '/pages/publish-product/publish-product' });
  },

  useRequest(e) {
    const item = e.currentTarget.dataset.item;
    inspirePayload.setPayload({ ...this.buildPayload(item), target: 'request' });
    wx.navigateTo({ url: '/pages/publish-request/publish-request' });
  },

  removeItem(e) {
    const id = e.currentTarget.dataset.id;
    const user = auth.getUser();
    notebook.remove(id, user && user.id);
    this.loadItems();
    wx.showToast({ title: '已删除', icon: 'none' });
  },

  clearAll() {
    wx.showModal({
      title: '清空灵感本',
      content: '确定删除当前账号下的全部收藏？',
      success: (res) => {
        if (!res.confirm) return;
        const user = auth.getUser();
        notebook.clear(user && user.id);
        this.loadItems();
      },
    });
  },

  previewImage(e) {
    const url = e.currentTarget.dataset.url;
    if (url) wx.previewImage({ urls: [url] });
  },
});
