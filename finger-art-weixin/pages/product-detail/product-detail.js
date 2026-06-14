const { productApi, orderApi } = require('../../services/api');
const auth = require('../../utils/auth');
const { formatImageUrl } = require('../../utils/format');
const { categoryLabel: catLabel } = require('../../constants/categories');

Page({
  data: {
    product: null,
    similar: [],
    isFollowing: false,
    isLiked: false,
    loading: true,
  },

  onLoad(options) {
    this.productId = options.id;
    this.loadDetail();
  },

  async loadDetail() {
    this.setData({ loading: true });
    try {
      const product = await productApi.getProductById(this.productId);
      product.image = formatImageUrl(product.image);
      if (product.detailImages) {
        try {
          product.detailImagesList = JSON.parse(product.detailImages).map(formatImageUrl);
        } catch {
          product.detailImagesList = [];
        }
      }
      product.categoryLabel = catLabel(product.category);
      const similar = await productApi.getSimilarProducts(this.productId).catch(() => []);
      const user = auth.getUser();
      let isFollowing = false;
      if (user && product.creatorId) {
        const { userApi } = require('../../services/api');
        isFollowing = await userApi.checkFollowing(user.id, product.creatorId).catch(() => false);
      }
      this.setData({
        product,
        similar: (similar || []).map((p) => ({ ...p, image: formatImageUrl(p.image) })),
        isFollowing,
        isLiked: !!product.liked,
      });
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },

  async onLike() {
    if (!auth.isLoggedIn()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    try {
      const result = await productApi.likeProduct(this.productId);
      const liked = !!(result && (result.liked ?? result.isLiked));
      const count = Number(result && (result.count ?? result.likes ?? result.likeCount ?? 0));
      this.setData({
        'product.likes': count,
        isLiked: liked,
      });
      wx.showToast({ title: liked ? '点赞成功' : '已取消点赞', icon: 'none' });
    } catch (e) {
      wx.showToast({ title: e.message || '操作失败', icon: 'none' });
    }
  },

  async onFollow() {
    if (!auth.isLoggedIn()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    const { product, isFollowing } = this.data;
    const user = auth.getUser();
    const { userApi } = require('../../services/api');
    try {
      if (isFollowing) {
        await userApi.unfollowUser(user.id, product.creatorId);
      } else {
        await userApi.followUser(user.id, product.creatorId);
      }
      this.setData({ isFollowing: !isFollowing });
    } catch (e) {
      wx.showToast({ title: e.message || '操作失败', icon: 'none' });
    }
  },

  goArtisan() {
    const id = this.data.product.creatorId;
    if (id) wx.navigateTo({ url: `/pages/artisan-profile/artisan-profile?id=${id}` });
  },

  async onBuy() {
    if (!auth.isLoggedIn()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    const { product } = this.data;
    const user = auth.getUser();
    if (product.creatorId === user.id) {
      wx.showToast({ title: '不能购买自己的作品', icon: 'none' });
      return;
    }
    const isCustom = product.type === 'CUSTOMIZABLE';
    wx.showModal({
      title: '确认购买',
      content: `购买「${product.title}」¥${product.price}？`,
      success: async (res) => {
        if (!res.confirm) return;
        try {
          wx.showLoading({ title: '下单中' });
          const order = await orderApi.createOrder({
            buyerId: user.id,
            buyerName: user.username,
            artisanId: Number(product.creatorId),
            artisanName: product.creator,
            productTitle: product.title,
            productType: product.type,
            price: Number(product.price),
            productId: product.id,
            requirements: isCustom ? `正式定制请求: ${product.title}` : `正式购买请求: ${product.title}`,
            ...(isCustom ? { status: 'PENDING_CONFIRM' } : {}),
          });
          if (!isCustom) {
            try {
              await orderApi.payDeposit(order.id, user.id);
            } catch (payErr) {
              wx.hideLoading();
              wx.showToast({ title: payErr.message || '请前往订单支付', icon: 'none' });
              wx.navigateTo({ url: `/pages/order-detail/order-detail?id=${order.id}` });
              return;
            }
          }
          wx.hideLoading();
          wx.showToast({ title: isCustom ? '定制订单已发起' : '购买成功', icon: 'success' });
          wx.navigateTo({ url: `/pages/order-detail/order-detail?id=${order.id}` });
        } catch (e) {
          wx.hideLoading();
          wx.showToast({ title: e.message || '购买失败', icon: 'none' });
        }
      },
    });
  },

  goSimilar(e) {
    wx.navigateTo({ url: `/pages/product-detail/product-detail?id=${e.currentTarget.dataset.id}` });
  },
});
