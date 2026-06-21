/** 手作分类封面 — 与 Web craftCoverImages.ts 保持一致 */

const CRAFT_COVER_MAP = {
  钩织: 'https://images.unsplash.com/photo-1584992236310-6edddc08acff?w=640&q=80',
  滴胶: 'https://images.unsplash.com/photo-1608571423902-eed4a5ad8108?w=640&q=80',
  穿戴甲: 'https://images.unsplash.com/photo-1596462502278-27bfdc403348?w=640&q=80',
  粘土: 'https://images.unsplash.com/photo-1565193566173-7a0ee3dbe261?w=640&q=80',
  缠花: 'https://images.unsplash.com/photo-1469334031218-e382a71b716b?w=640&q=80',
  拼豆: 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=640&q=80',
  刺绣: 'https://images.unsplash.com/photo-1558171813-4c088753af8f?w=640&q=80',
  串珠: 'https://images.unsplash.com/photo-1535632066927-ab7c9ab60908?w=640&q=80',
  摄影: 'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=640&q=80',
  设计: 'https://images.unsplash.com/photo-1558655146-d09347e92766?w=640&q=80',
  其它: 'https://images.unsplash.com/photo-1528819622765-d6bcf132f793?w=640&q=80',
};

const DEFAULT_CRAFT_COVER = CRAFT_COVER_MAP['其它'];

function getCraftCoverImage(category) {
  const key = (category || '').trim();
  if (key && CRAFT_COVER_MAP[key]) return CRAFT_COVER_MAP[key];
  return DEFAULT_CRAFT_COVER;
}

module.exports = {
  CRAFT_COVER_MAP,
  DEFAULT_CRAFT_COVER,
  getCraftCoverImage,
};
