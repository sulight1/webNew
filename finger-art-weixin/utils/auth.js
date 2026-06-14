const TOKEN_KEY = 'token';
const USER_KEY = 'user';

function getToken() {
  return wx.getStorageSync(TOKEN_KEY) || '';
}

function getUser() {
  try {
    const raw = wx.getStorageSync(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
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
  return !!getToken() && !!getUser();
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
