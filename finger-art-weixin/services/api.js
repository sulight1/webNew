const req = require('../utils/request');

const userApi = {
  login: (data) => req.post('/users/login', data),
  register: (data) => req.post('/users/register', data),
  getProfile: (id) => req.get('/users/profile', { id }),
  getPublicProfile: (id) => req.get(`/users/${id}/public`),
  updateUser: (id, data) => req.put(`/users/${id}`, data),
  applyArtisan: (userId) => req.post('/users/apply-artisan', null, { params: { userId } }),
  followUser: (followerId, followingId) => req.post('/users/follow', { followerId, followingId }),
  unfollowUser: (followerId, followingId) => req.post('/users/unfollow', { followerId, followingId }),
  checkFollowing: (followerId, followingId) => req.get('/users/is-following', { followerId, followingId }),
  getTopArtisans: (limit = 5, excludeUserId) => req.get('/users/top-artisans', { limit, excludeUserId }),
};

const productApi = {
  getProducts: (params) => req.get('/products', params),
  getProductById: (id) => req.get(`/products/${id}`),
  searchProducts: (q, limit = 50) => req.get('/products/search', { q, limit }),
  getSimilarProducts: (id, limit = 6) => req.get(`/products/${id}/similar`, { limit }),
  likeProduct: (id) => req.post(`/products/${id}/like`),
  favoriteProduct: (id) => req.post(`/products/${id}/favorite`),
  getFavoriteProducts: () => req.get('/products/favorites'),
  createProduct: (data) => req.post('/products', data),
  updateProduct: (id, data) => req.put(`/products/${id}`, data),
  deleteProduct: (id) => req.del(`/products/${id}`),
};

const orderApi = {
  createOrder: (data) => req.post('/orders', data),
  getOrderById: (id) => req.get(`/orders/${id}`),
  getBuyerOrders: (id) => req.get(`/orders/buyer/${id}`),
  getArtisanOrders: (id) => req.get(`/orders/artisan/${id}`),
  confirmOrder: (id, artisanId) => req.post(`/orders/${id}/confirm`, { artisanId }),
  payDeposit: (id, buyerId, paymentChannel = 'MOCK_WECHAT') =>
    req.post(`/orders/${id}/pay-deposit`, { buyerId, paymentChannel }),
  payBalance: (id, buyerId) => req.post(`/orders/${id}/pay-balance`, { buyerId }),
  confirmReceipt: (id, buyerId) => req.post(`/orders/${id}/confirm-receipt`, { buyerId }),
  updateStatus: (id, status, operatorId, operatorName) =>
    req.put(`/orders/${id}/status`, null, { params: { status, operatorId, operatorName } }),
  shipOrder: (id, artisanId, shippingCompany, trackingNumber, operatorName) =>
    req.post(`/orders/${id}/ship`, { artisanId, shippingCompany, trackingNumber, operatorName }),
  requestCancel: (id, buyerId, reason) => req.post(`/orders/${id}/request-cancel`, { buyerId, reason }),
  approveCancel: (id, artisanId) => req.post(`/orders/${id}/approve-cancel`, { artisanId }),
  rejectCancel: (id, artisanId, reason) => req.post(`/orders/${id}/reject-cancel`, { artisanId, reason }),
  openDispute: (id, userId, reason) => req.post(`/orders/${id}/dispute`, { userId, reason }),
  getMilestones: (id) => req.get(`/orders/${id}/milestones`),
  addMilestone: (id, data) => req.post(`/orders/${id}/milestones`, data),
  getLogistics: (id) => req.get(`/orders/${id}/logistics`),
};

const customRequestApi = {
  list: () => req.get('/custom-requests'),
  search: (params) => req.get('/custom-requests', params),
  getById: (id) => req.get(`/custom-requests/${id}`),
  create: (data) => req.post('/custom-requests', data),
  getBids: (id, viewerUserId) => req.get(`/custom-requests/${id}/bids`, { viewerUserId }),
  submitBid: (id, artisanId, message) => req.post(`/custom-requests/${id}/bids`, { artisanId, message }),
  selectBid: (id, buyerId, bidId) => req.post(`/custom-requests/${id}/select-bid`, { buyerId, bidId }),
};

const skillApi = {
  getSkills: (params) => req.get('/skills', params),
  addSkill: (data) => req.post('/skills', data),
  deleteSkill: (id) => req.del(`/skills/${id}`),
  requestExchange: (data) => req.post('/skill-exchange/request', data),
  getMyExchanges: (userId) => req.get('/skill-exchange/my', { userId }),
  acceptExchange: (id, userId) => req.patch(`/skill-exchange/${id}/accept`, { userId }),
  confirmExchange: (id, userId) => req.patch(`/skill-exchange/${id}/confirm`, { userId }),
  completeExchange: (id, userId) => req.patch(`/skill-exchange/${id}/complete`, { userId }),
};

const walletApi = {
  recharge: (userId, amount) => req.post('/wallet/recharge', { userId, amount, channel: 'MOCK_WECHAT' }),
  transactions: (userId, page = 0, size = 20) => req.get('/wallet/transactions', { userId, page, size }),
};

const messageApi = {
  list: (userId) => req.get(`/messages/list/${userId}`),
  getChat: (userId1, userId2) => req.get('/messages/chat', { userId1, userId2 }),
  send: (data) => req.post('/messages', data),
};

const notificationApi = {
  list: (userId) => req.get('/notifications', { userId }),
  unreadCount: (userId) => req.get('/notifications/unread-count', { userId }),
  markRead: (id, userId) => req.patch(`/notifications/${id}/read`, { userId }),
};

const statsApi = {
  platform: () => req.get('/stats/platform'),
  summary: () => req.get('/stats/summary'),
  getArtisanAnalytics: (userId) => req.get(`/stats/artisan/${userId}`),
};

const economyApi = {
  checkIn: (userId) => req.post('/economy/check-in', null, { params: { userId }, skipAuthRedirect: true }),
  getTasks: (userId) => req.get('/economy/tasks', { userId }, { skipAuthRedirect: true }),
  claimTask: (userId, taskCode) => req.post('/economy/tasks/claim', { userId, taskCode }, { skipAuthRedirect: true }),
  boostProduct: (userId, productId) => req.post('/economy/boost-product', { userId, productId }, { skipAuthRedirect: true }),
};

const inspirationGachaApi = {
  getStatus: (userId) => req.get('/ai/inspiration-gacha/status', { userId }),
  draw: (userId, useFree = true) => req.post('/ai/inspiration-gacha/draw', { userId, useFree }),
  generateImage: (userId, imagePrompt) =>
    req.post('/ai/inspiration-gacha/generate-image', { userId, imagePrompt }, { timeout: 120000 }),
};

module.exports = {
  userApi,
  productApi,
  orderApi,
  customRequestApi,
  skillApi,
  walletApi,
  messageApi,
  notificationApi,
  statsApi,
  economyApi,
  inspirationGachaApi,
};
