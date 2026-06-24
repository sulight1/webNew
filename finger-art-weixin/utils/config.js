/**
 * API / 图片地址配置
 * - 开发者工具模拟器：localhost（brand 为 devtools）
 * - 真机 / 真机远程调试：局域网 IP（图片不走调试代理，必须用 LAN）
 * 换网络后请更新 DEV_LAN_IP（ipconfig 查看 IPv4）
 */
const DEV_LAN_IP = '192.168.40.46';
const PORT = 3000;

function getSystemInfoSafe() {
  try {
    if (typeof wx !== 'undefined' && wx.getSystemInfoSync) {
      return wx.getSystemInfoSync();
    }
  } catch (e) {
    // ignore
  }
  return null;
}

/**
 * 是否开发者工具里的模拟器（不是真机）
 * 模拟器 platform=devtools 且 brand=devtools；
 * 真机远程调试 platform 也是 devtools，但 brand 为 vivo/iPhone 等
 */
function isDevtoolsSimulator() {
  const info = getSystemInfoSafe();
  if (!info || info.platform !== 'devtools') return false;
  const brand = String(info.brand || '').toLowerCase();
  return brand === 'devtools';
}

function resolveApiBase() {
  if (isDevtoolsSimulator()) {
    return `http://localhost:${PORT}`;
  }
  return `http://${DEV_LAN_IP}:${PORT}`;
}

/** 图片/头像：真机必须用局域网 IP（<image> 不走调试通道代理） */
function resolveMediaBase() {
  if (isDevtoolsSimulator()) {
    return `http://localhost:${PORT}`;
  }
  return `http://${DEV_LAN_IP}:${PORT}`;
}

/** WebSocket 基址（与 resolveApiBase 同 host，协议 ws） */
function resolveWsBase() {
  return resolveApiBase().replace(/^http/, 'ws');
}

module.exports = {
  DEV_LAN_IP,
  PORT,
  isDevtoolsSimulator,
  resolveApiBase,
  resolveMediaBase,
  resolveWsBase,
};
