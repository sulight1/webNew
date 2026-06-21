const auth = require('./auth');

const LEGACY_KEY = 'inspiration_notebook';
const MAX_ITEMS = 50;

function getUserId() {
  const user = auth.getUser();
  if (!user || user.id == null) return null;
  return user.id;
}

function storageKey(userId) {
  const id = userId != null ? userId : getUserId();
  return id ? `${LEGACY_KEY}_${id}` : `${LEGACY_KEY}_guest`;
}

function migrateLegacyIfNeeded(userId) {
  try {
    const legacy = wx.getStorageSync(LEGACY_KEY);
    if (!legacy || !legacy.length) {
      return;
    }
    const userKey = storageKey(userId);
    const existing = wx.getStorageSync(userKey) || [];
    if (!existing.length) {
      wx.setStorageSync(userKey, legacy.slice(0, MAX_ITEMS));
    }
    wx.removeStorageSync(LEGACY_KEY);
  } catch (e) {
    /* ignore */
  }
}

function readList(userId) {
  const id = userId != null ? userId : getUserId();
  if (id) migrateLegacyIfNeeded(id);
  try {
    return wx.getStorageSync(storageKey(id)) || [];
  } catch (e) {
    return [];
  }
}

function writeList(items, userId) {
  wx.setStorageSync(storageKey(userId != null ? userId : getUserId()), items);
}

function list(userId) {
  return readList(userId);
}

function save(item, userId) {
  const id = userId != null ? userId : getUserId();
  const entry = {
    id: Date.now(),
    savedAt: new Date().toISOString(),
    ...item,
  };
  const items = [entry, ...readList(id)].slice(0, MAX_ITEMS);
  writeList(items, id);
  return entry;
}

function remove(entryId, userId) {
  const id = userId != null ? userId : getUserId();
  const next = readList(id).filter((item) => item.id !== entryId);
  writeList(next, id);
  return next;
}

function clear(userId) {
  wx.removeStorageSync(storageKey(userId != null ? userId : getUserId()));
}

module.exports = {
  LEGACY_KEY,
  list,
  save,
  remove,
  clear,
  getUserId,
};
