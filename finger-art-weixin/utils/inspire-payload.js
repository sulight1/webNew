const PAYLOAD_KEY = 'ai_inspire_payload';

function setPayload(payload) {
  wx.setStorageSync(PAYLOAD_KEY, payload);
}

function consumePayload() {
  try {
    const payload = wx.getStorageSync(PAYLOAD_KEY);
    if (payload) wx.removeStorageSync(PAYLOAD_KEY);
    return payload || null;
  } catch (e) {
    return null;
  }
}

function peekPayload() {
  try {
    return wx.getStorageSync(PAYLOAD_KEY) || null;
  } catch (e) {
    return null;
  }
}

module.exports = {
  PAYLOAD_KEY,
  setPayload,
  consumePayload,
  peekPayload,
};
