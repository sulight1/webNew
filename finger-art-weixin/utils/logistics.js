function buildKuaidi100Url(companyCode, trackingNumber) {
  const nu = encodeURIComponent(trackingNumber || '');
  if (companyCode) {
    return `https://www.kuaidi100.com/chaxun?com=${companyCode}&nu=${nu}`;
  }
  return `https://www.kuaidi100.com/chaxun?nu=${nu}`;
}

function copyText(text, successTitle) {
  if (!text) return;
  wx.setClipboardData({
    data: String(text),
    success: () => {
      if (successTitle) wx.showToast({ title: successTitle, icon: 'success' });
    },
  });
}

function openFallbackUrl(url) {
  if (!url) return;
  wx.setClipboardData({
    data: url,
    success: () => {
      wx.showModal({
        title: '查询链接已复制',
        content: '请在手机浏览器中打开快递100进行查询',
        showCancel: false,
      });
    },
  });
}

module.exports = {
  buildKuaidi100Url,
  copyText,
  openFallbackUrl,
};
