const auth = require('./auth');

const GUEST_CART_KEY = 'finger_art_cart_guest';

function cartStorageKey(userId) {
  return userId ? `finger_art_cart_${userId}` : GUEST_CART_KEY;
}

function loadItems() {
  const user = auth.getUser();
  const key = cartStorageKey(user && user.id);
  try {
    const raw = wx.getStorageSync(key);
    return Array.isArray(raw) ? raw : [];
  } catch (_) {
    return [];
  }
}

function persist(items) {
  const user = auth.getUser();
  const key = cartStorageKey(user && user.id);
  wx.setStorageSync(key, items);
}

function getTotalCount(items) {
  const list = items || loadItems();
  return list.reduce((sum, item) => sum + (item.quantity || 0), 0);
}

function getTotalAmount(items) {
  const list = items || loadItems();
  return list.reduce((sum, item) => sum + Number(item.price) * (item.quantity || 0), 0);
}

function addProduct(product, quantity = 1) {
  const maxStock = Math.max(product.stock ?? 1, 1);
  const addQty = Math.max(1, Math.min(Math.floor(quantity), maxStock));
  const items = loadItems();
  const existing = items.find((item) => item.productId === product.id);
  if (existing) {
    if (existing.quantity + addQty > maxStock) {
      return { ok: false, reason: 'stock' };
    }
    existing.quantity += addQty;
    existing.stock = maxStock;
    existing.price = Number(product.price);
  } else {
    items.unshift({
      productId: product.id,
      title: product.title,
      price: Number(product.price),
      image: product.image,
      creator: product.creator,
      creatorId: product.creatorId,
      stock: maxStock,
      type: product.type,
      quantity: addQty,
      addedAt: Date.now(),
    });
  }
  persist(items);
  return { ok: true, items };
}

function updateQuantity(productId, quantity) {
  const items = loadItems();
  const item = items.find((entry) => entry.productId === productId);
  if (!item) return loadItems();
  if (quantity <= 0) {
    return removeItem(productId);
  }
  item.quantity = Math.min(Math.floor(quantity), item.stock);
  persist(items);
  return items;
}

function removeItem(productId) {
  const items = loadItems().filter((item) => item.productId !== productId);
  persist(items);
  return items;
}

function removeItems(productIds) {
  const idSet = new Set(productIds);
  const items = loadItems().filter((item) => !idSet.has(item.productId));
  persist(items);
  return items;
}

function clear() {
  persist([]);
  return [];
}

module.exports = {
  loadItems,
  getTotalCount,
  getTotalAmount,
  addProduct,
  updateQuantity,
  removeItem,
  removeItems,
  clear,
};
