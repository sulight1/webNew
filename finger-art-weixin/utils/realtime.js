const { resolveWsBase } = require('./config');

let socketTask = null;
let currentUserId = null;
let pingTimer = null;
let reconnectTimer = null;
let reconnectAttempts = 0;
let onEventCallback = null;

const MAX_RECONNECT = 8;
const PING_INTERVAL_MS = 25000;

function onEvent(callback) {
  onEventCallback = callback;
}

function startPing() {
  stopPing();
  pingTimer = setInterval(() => {
    if (socketTask) {
      socketTask.send({ data: 'ping' });
    }
  }, PING_INTERVAL_MS);
}

function stopPing() {
  if (pingTimer) {
    clearInterval(pingTimer);
    pingTimer = null;
  }
}

function clearReconnect() {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer);
    reconnectTimer = null;
  }
}

function closeSocket() {
  stopPing();
  if (socketTask) {
    try {
      socketTask.close({});
    } catch (e) {
      // ignore
    }
    socketTask = null;
  }
}

function scheduleReconnect() {
  if (!currentUserId || reconnectTimer) return;
  if (reconnectAttempts >= MAX_RECONNECT) return;
  reconnectAttempts += 1;
  const delay = Math.min(1000 * reconnectAttempts, 10000);
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null;
    if (currentUserId) {
      openSocket(currentUserId);
    }
  }, delay);
}

function handleMessage(res) {
  try {
    const data = JSON.parse(res.data);
    if (data.type === 'PONG') return;
    if (onEventCallback) onEventCallback(data);
  } catch (e) {
    // ignore malformed payload
  }
}

function openSocket(userId) {
  closeSocket();
  const url = `${resolveWsBase()}/ws/realtime?userId=${userId}`;
  socketTask = wx.connectSocket({ url });

  socketTask.onOpen(() => {
    reconnectAttempts = 0;
    startPing();
  });

  socketTask.onMessage(handleMessage);

  socketTask.onClose(() => {
    socketTask = null;
    stopPing();
    scheduleReconnect();
  });

  socketTask.onError(() => {
    stopPing();
  });
}

function connect(userId) {
  if (!userId) return;
  if (socketTask && currentUserId === userId) return;
  clearReconnect();
  currentUserId = userId;
  reconnectAttempts = 0;
  openSocket(userId);
}

function disconnect() {
  clearReconnect();
  currentUserId = null;
  reconnectAttempts = 0;
  closeSocket();
}

module.exports = {
  connect,
  disconnect,
  onEvent,
};
