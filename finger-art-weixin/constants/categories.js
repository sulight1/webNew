const PRODUCT_CATEGORIES = [
  { key: 'crochet', label: '钩织系列', emoji: '🧶' },
  { key: 'resin', label: '滴胶干花', emoji: '💎' },
  { key: 'nails', label: '精致穿戴甲', emoji: '💅' },
  { key: 'clay', label: '软陶粘土', emoji: '🏺' },
  { key: 'flower', label: '古法缠花', emoji: '🌸' },
  { key: 'perler', label: '拼豆系列', emoji: '🫘' },
  { key: 'embroidery', label: '刺绣布艺', emoji: '🪡' },
  { key: 'bead', label: '串珠饰品', emoji: '📿' },
  { key: 'leather', label: '皮艺皮雕', emoji: '🧵' },
  { key: 'wood', label: '木工雕绘', emoji: '🪵' },
  { key: 'candle', label: '香薰蜡烛', emoji: '🕯️' },
  { key: 'paper', label: '纸艺衍纸', emoji: '📄' },
  { key: 'tuanshan', label: '手绘团扇', emoji: '🪭' },
  { key: 'lantern', label: '传统花灯', emoji: '🏮' },
];

function categoryLabel(key) {
  const item = PRODUCT_CATEGORIES.find((c) => c.key === key);
  return item ? `${item.emoji} ${item.label}` : key;
}

module.exports = {
  PRODUCT_CATEGORIES,
  categoryLabel,
};
