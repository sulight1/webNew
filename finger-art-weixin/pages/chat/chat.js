const { messageApi } = require('../../services/api');
const auth = require('../../utils/auth');

function formatTimeLabel(value) {
  if (!value) return '';
  const text = String(value).replace('T', ' ');
  return text.length >= 16 ? text.slice(5, 16) : text;
}

Page({
  data: {
    peerId: null,
    peerName: '',
    messages: [],
    draft: '',
    canSend: false,
    loading: true,
    sending: false,
    scrollIntoView: '',
  },

  onLoad(options) {
    if (!auth.isLoggedIn()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    this.peerId = Number(options.peerId);
    this.peerName = decodeURIComponent(options.peerName || '手作人');
    wx.setNavigationBarTitle({ title: this.peerName });
    this._onRealtimeBound = (event) => this._onRealtimeEvent(event);
    getApp().registerRealtimeHandler('chat', this._onRealtimeBound);
    this.loadChat();
  },

  onUnload() {
    getApp().unregisterRealtimeHandler('chat');
  },

  _onRealtimeEvent(event) {
    if (event.type !== 'CHAT_MESSAGE') return;
    const payload = event.payload || event.data;
    if (!payload) return;
    const user = auth.getUser();
    if (!user) return;
    const involved = (
      (payload.senderId === user.id && payload.receiverId === this.peerId)
      || (payload.senderId === this.peerId && payload.receiverId === user.id)
    );
    if (involved) this.loadChat(false);
  },

  mapMessages(list, userId) {
    return (list || []).map((item) => ({
      ...item,
      mine: item.senderId === userId,
      timeLabel: formatTimeLabel(item.createTime),
    }));
  },

  async loadChat(showLoading = true) {
    const user = auth.getUser();
    if (!user || !this.peerId) return;
    if (showLoading) this.setData({ loading: true });
    try {
      const list = await messageApi.getChat(user.id, this.peerId);
      const messages = this.mapMessages(list, user.id);
      const last = messages[messages.length - 1];
      this.setData({
        messages,
        scrollIntoView: last ? `msg-${last.id}` : '',
      });
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },

  onDraftInput(e) {
    const draft = e.detail.value;
    this.setData({ draft, canSend: !!draft.trim() });
  },

  async onSend() {
    const user = auth.getUser();
    const content = (this.data.draft || '').trim();
    if (!user || !content || this.data.sending) return;
    this.setData({ sending: true });
    try {
      await messageApi.send({
        senderId: user.id,
        senderName: user.username,
        receiverId: this.peerId,
        receiverName: this.peerName,
        content,
      });
      this.setData({ draft: '', canSend: false });
      await this.loadChat(false);
    } catch (e) {
      wx.showToast({ title: e.message || '发送失败', icon: 'none' });
    } finally {
      this.setData({ sending: false });
    }
  },
});
