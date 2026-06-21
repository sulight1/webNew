const { productApi, orderApi } = require('../../services/api');
const auth = require('../../utils/auth');
const cartUtil = require('../../utils/cart');
const { formatImageUrl } = require('../../utils/format');
const { categoryLabel: catLabel } = require('../../constants/categories');

Page({
  data: {
    product: null,
    similar: [],
    isFollowing: false,
    isLiked: false,
    isFavorited: false,
    loading: true,
    purchaseQuantity: 1,
    lineTotal: 0,
    isSoldOut: false,
    canAddToCart: false,
    maxPurchaseQuantity: 1,
    showCustomModal: false,
    customRequirements: '',
    customSubmitting: false,
  },

  onLoad(options) {
    this.productId = options.id;
    this.loadDetail();
  },

  syncQuantityMeta(product, purchaseQuantity) {
    const isSoldOut = product.type !== 'CUSTOMIZABLE' && (product.stock ?? 1) <= 0;
    const maxPurchaseQuantity = product.type === 'CUSTOMIZABLE'
      ? 1
      : Math.max(product.stock ?? 1, 1);
    const qty = Math.min(Math.max(1, purchaseQuantity), maxPurchaseQuantity);
    const user = auth.getUser();
    const canAddToCart = !isSoldOut
      && product.type !== 'CUSTOMIZABLE'
      && (!user || product.creatorId !== user.id);
    const lineTotal = Math.round(Number(product.price) * qty * 100) / 100;
    this.setData({
      purchaseQuantity: qty,
      isSoldOut,
      canAddToCart,
      maxPurchaseQuantity,
      lineTotal,
    });
  },

  async loadDetail() {
    this.setData({ loading: true });
    try {
      const product = await productApi.getProductById(this.productId);
      product.image = formatImageUrl(product.image);
      if (product.detailImages) {
        try {
          product.detailImagesList = JSON.parse(product.detailImages).map(formatImageUrl);
        } catch (e) {
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
        isFavorited: !!product.favorited,
      });
      this.syncQuantityMeta(product, 1);
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },

  onMinusQty() {
    const { purchaseQuantity, product } = this.data;
    if (purchaseQuantity <= 1) return;
    this.syncQuantityMeta(product, purchaseQuantity - 1);
  },

  onPlusQty() {
    const { purchaseQuantity, product, maxPurchaseQuantity } = this.data;
    if (purchaseQuantity >= maxPurchaseQuantity) {
      wx.showToast({ title: '已达库存上限', icon: 'none' });
      return;
    }
    this.syncQuantityMeta(product, purchaseQuantity + 1);
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

  async onFavorite() {
    if (!auth.isLoggedIn()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    try {
      const result = await productApi.favoriteProduct(this.productId);
      const favorited = !!(result && (result.favorited ?? result.isFavorited));
      this.setData({ isFavorited: favorited });
      wx.showToast({ title: favorited ? '已加入收藏' : '已取消收藏', icon: 'none' });
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

  onAddToCart() {
    if (!auth.isLoggedIn()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    const { product, purchaseQuantity } = this.data;
    const user = auth.getUser();
    if (product.creatorId === user.id) {
      wx.showToast({ title: '不能将自己的作品加入购物车', icon: 'none' });
      return;
    }
    const result = cartUtil.addProduct(product, purchaseQuantity);
    if (!result.ok) {
      wx.showToast({ title: '已达库存上限', icon: 'none' });
      return;
    }
    wx.showToast({ title: '已加入购物车', icon: 'success' });
  },

  async onBuy() {
    if (!auth.isLoggedIn()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    const { product, purchaseQuantity } = this.data;
    const user = auth.getUser();
    if (product.creatorId === user.id) {
      wx.showToast({ title: '不能购买自己的作品', icon: 'none' });
      return;
    }
    const isCustom = product.type === 'CUSTOMIZABLE';
    if (!isCustom) {
      if ((product.stock ?? 1) <= 0) {
        wx.showToast({ title: '该商品已售罄', icon: 'none' });
        return;
      }
      wx.navigateTo({
        url: `/pages/checkout/checkout?productId=${product.id}&qty=${purchaseQuantity}`,
      });
      return;
    }

    wx.showModal({
      title: '确认定制',
      content: `发起定制「${product.title}」¥${product.price}？提交前请先填写定制需求。`,
      success: (res) => {
        if (!res.confirm) return;
        this.setData({ showCustomModal: true, customRequirements: '' });
      },
    });
  },

  onCustomRequirementsInput(e) {
    this.setData({ customRequirements: e.detail.value });
  },

  closeCustomModal() {
    this.setData({ showCustomModal: false, customRequirements: '' });
  },

  stopPropagation() {},

  async submitCustomOrder() {
    const { product, customRequirements, customSubmitting } = this.data;
    if (!product || customSubmitting) return;
    const user = auth.getUser();
    const requirements = (customRequirements || '').trim();
    if (!requirements) {
      wx.showToast({ title: '请填写定制需求描述', icon: 'none' });
      return;
    }
    if (!product.creatorId) {
      wx.showToast({ title: '作品信息不完整', icon: 'none' });
      return;
    }
    this.setData({ customSubmitting: true });
    try {
      wx.showLoading({ title: '提交中' });
      const order = await orderApi.createOrder({
        buyerId: user.id,
        buyerName: user.username,
        artisanId: Number(product.creatorId),
        artisanName: product.creator,
        productTitle: product.title,
        productType: product.type,
        price: Number(product.price),
        quantity: 1,
        productId: product.id,
        requirements: `【${product.title}】${requirements}`,
        status: 'PENDING_CONFIRM',
      });
      wx.hideLoading();
      this.setData({ showCustomModal: false, customRequirements: '' });
      wx.showToast({ title: '定制订单已发起', icon: 'success' });
      wx.navigateTo({ url: `/pages/order-detail/order-detail?id=${order.id}` });
    } catch (e) {
      wx.hideLoading();
      wx.showToast({ title: e.message || '购买失败', icon: 'none' });
    } finally {
      this.setData({ customSubmitting: false });
    }
  },

  goSimilar(e) {
    wx.navigateTo({ url: `/pages/product-detail/product-detail?id=${e.currentTarget.dataset.id}` });
  },
});
