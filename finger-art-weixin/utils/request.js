const config = require('./config');
const auth = require('./auth');

function getApiBase() {
  return config.resolveApiBase();
}

function buildQuery(params) {
  if (!params) return '';
  const parts = Object.keys(params)
    .filter((k) => params[k] !== undefined && params[k] !== null && params[k] !== '')
    .map((k) => `${encodeURIComponent(k)}=${encodeURIComponent(params[k])}`);
  return parts.length ? `?${parts.join('&')}` : '';
}

function request(options) {
  const { url, method = 'GET', data, params, skipAuthRedirect } = options;
  const header = { 'Content-Type': 'application/json', ...(options.header || {}) };
  const token = auth.getToken();
  if (token) header.Authorization = `Bearer ${token}`;

  return new Promise((resolve, reject) => {
    wx.request({
      url: getApiBase() + url + buildQuery(params),
      method,
      data,
      header,
      success(res) {
        const body = res.data;
        if (body && body.code === 200) {
          resolve(body.data);
          return;
        }
        if (body && body.code === 401) {
          if (!skipAuthRedirect) {
            auth.clearAuth();
            wx.showToast({ title: body.message || '请先登录', icon: 'none' });
          }
          reject(new Error(body.message || '请先登录'));
          return;
        }
        reject(new Error((body && body.message) || '请求失败'));
      },
      fail(err) {
        const raw = err.errMsg || '网络错误';
        let message = raw;
        try {
          const platform = wx.getSystemInfoSync().platform;
          if (platform !== 'devtools' && /fail|timeout|connect/i.test(raw)) {
            message = `${raw}。真机请确认：同一 WiFi、config.js 中 DEV_LAN_IP、防火墙已放行 3000 端口`;
          }
        } catch (e) {
          // ignore
        }
        reject(new Error(message));
      },
    });
  });
}

function uploadFile(filePath) {
  const token = auth.getToken();
  return new Promise((resolve, reject) => {
    wx.uploadFile({
      url: `${getApiBase()}/files/upload`,
      filePath,
      name: 'file',
      header: token ? { Authorization: `Bearer ${token}` } : {},
      success(res) {
        try {
          const body = JSON.parse(res.data);
          if (body.code === 200) resolve(body.data);
          else reject(new Error(body.message || '上传失败'));
        } catch (e) {
          reject(new Error('上传失败'));
        }
      },
      fail: reject,
    });
  });
}

module.exports = {
  get: (url, params, opt) => request({ url, method: 'GET', params, ...opt }),
  post: (url, data, opt) => request({ url, method: 'POST', data, ...opt }),
  put: (url, data, opt) => request({ url, method: 'PUT', data, ...opt }),
  patch: (url, data, opt) => request({ url, method: 'PATCH', data, ...opt }),
  del: (url, opt) => request({ url, method: 'DELETE', ...opt }),
  uploadFile,
};
