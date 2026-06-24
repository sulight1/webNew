/** 分类封面 SVG 渐变 — 与 Web categoryCovers.ts 保持一致 */

const CATEGORY_DEFS = {
  钩织: { emoji: '🧶', from: '#fce4ec', to: '#f48fb1' },
  滴胶: { emoji: '💧', from: '#e8eaf6', to: '#9fa8da' },
  穿戴甲: { emoji: '💅', from: '#fce4ec', to: '#ec407a' },
  粘土: { emoji: '🏺', from: '#fff3e0', to: '#ffb74d' },
  陶瓷: { emoji: '🏺', from: '#fff8e1', to: '#ffcc80' },
  缠花: { emoji: '🌸', from: '#fce4ec', to: '#ce93d8' },
  拼豆: { emoji: '🧩', from: '#e3f2fd', to: '#64b5f6' },
  刺绣: { emoji: '🪡', from: '#f3e5f5', to: '#ba68c8' },
  串珠: { emoji: '📿', from: '#e0f7fa', to: '#4dd0e1' },
  摄影: { emoji: '📷', from: '#eceff1', to: '#90a4ae' },
  设计: { emoji: '🎨', from: '#f3e5f5', to: '#9575cd' },
  皮艺: { emoji: '🧵', from: '#efebe9', to: '#a1887f' },
  木工: { emoji: '🪵', from: '#fff3e0', to: '#bcaaa4' },
  香薰: { emoji: '🕯️', from: '#fff8e1', to: '#ffe082' },
  纸艺: { emoji: '📄', from: '#fafafa', to: '#e0e0e0' },
  团扇: { emoji: '🪭', from: '#fff8e1', to: '#ffb74d' },
  花灯: { emoji: '🏮', from: '#ffebee', to: '#ef5350' },
  其它: { emoji: '✨', from: '#f9f0ff', to: '#b37feb' },
};

const COVER_CACHE = {};

function buildSvgCover(label, def) {
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="600" height="400" viewBox="0 0 600 400">
  <defs>
    <linearGradient id="bg" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="${def.from}"/>
      <stop offset="100%" stop-color="${def.to}"/>
    </linearGradient>
  </defs>
  <rect width="600" height="400" fill="url(#bg)"/>
  <circle cx="480" cy="80" r="120" fill="white" fill-opacity="0.12"/>
  <circle cx="80" cy="340" r="90" fill="white" fill-opacity="0.1"/>
  <text x="300" y="175" font-size="72" text-anchor="middle">${def.emoji}</text>
  <text x="300" y="260" font-size="32" fill="#434343" text-anchor="middle" font-family="PingFang SC,Microsoft YaHei,sans-serif" font-weight="600">${label}</text>
</svg>`;
  return `data:image/svg+xml,${encodeURIComponent(svg)}`;
}

function getCategoryCoverImage(category) {
  const key = (category || '').trim() || '其它';
  if (!COVER_CACHE[key]) {
    const def = CATEGORY_DEFS[key] || CATEGORY_DEFS['其它'];
    COVER_CACHE[key] = buildSvgCover(key, def);
  }
  return COVER_CACHE[key];
}

module.exports = {
  getCategoryCoverImage,
};
