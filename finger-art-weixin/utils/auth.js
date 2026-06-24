const TOKEN_KEY = 'token';
const USER_KEY = 'user';

function getToken() {
  return wx.getStorageSync(TOKEN_KEY) || '';
}

function getUser() {
  try {
    const raw = wx.getStorageSync(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch (e) {
    return null;
  }
}

function setLogin(user, token) {
  wx.setStorageSync(USER_KEY, JSON.stringify(user));
  if (token) wx.setStorageSync(TOKEN_KEY, token);
}

function clearAuth() {
  wx.removeStorageSync(TOKEN_KEY);
  wx.removeStorageSync(USER_KEY);
}

function isLoggedIn() {
  const token = getToken();
  if (!token || !getUser()) return false;
  try {
    const segment = token.split('.')[1];
    if (!segment) return false;
    const payload = JSON.parse(decodeURIComponent(escape(atob(segment.replace(/-/g, '+').replace(/_/g, '/')))));
    if (payload.exp && Date.now() >= payload.exp * 1000) {
      clearAuth();
      return false;
    }
    return true;
  } catch (e) {
    return !!token && !!getUser();
  }
}

function isArtisan(user) {
  return user && user.role === 'ARTISAN';
}

module.exports = {
  getToken,
  getUser,
  setLogin,
  clearAuth,
  isLoggedIn,
  isArtisan,
};
