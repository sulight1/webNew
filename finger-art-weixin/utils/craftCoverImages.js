/** 手作分类封面 — 与 Web craftCoverImages.ts 保持一致 */

const { getCategoryCoverImage } = require('./categoryCovers');

const CATEGORY_ALIASES = {
  陶瓷: '粘土',
  软陶: '粘土',
  陶艺: '粘土',
  陶土: '粘土',
  毛线: '钩织',
  编织: '钩织',
  钩针: '钩织',
  美甲: '穿戴甲',
  甲片: '穿戴甲',
  绒花: '缠花',
  发簪: '缠花',
  像素: '拼豆',
  融合豆: '拼豆',
  布艺: '刺绣',
  皮艺: '皮艺',
  皮雕: '皮艺',
  木工: '木工',
  木作: '木工',
  香薰: '香薰',
  蜡烛: '香薰',
  纸艺: '纸艺',
  衍纸: '纸艺',
  团扇: '团扇',
  花灯: '花灯',
};

const CRAFT_COVER_MAP = {
  钩织: 'https://images.unsplash.com/photo-1584992236310-6edddc08acff?w=640&q=80',
  滴胶: 'https://images.unsplash.com/photo-1608571423902-eed4a5ad8108?w=640&q=80',
  穿戴甲: 'https://images.unsplash.com/photo-1596462502278-27bfdc403348?w=640&q=80',
  粘土: 'https://images.unsplash.com/photo-1565193566173-7a0ee3dbe261?w=640&q=80',
  缠花: 'https://images.unsplash.com/photo-1544576846-484939e4c563?w=640&q=80',
  拼豆: 'https://images.unsplash.com/photo-1513475382585-d06e58bcb0e0?w=640&q=80',
  刺绣: 'https://images.unsplash.com/photo-1558171813-4c088753af8f?w=640&q=80',
  串珠: 'https://images.unsplash.com/photo-1535632066927-ab7c9ab60908?w=640&q=80',
  摄影: 'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=640&q=80',
  设计: 'https://images.unsplash.com/photo-1558655146-d09347e92766?w=640&q=80',
  其它: 'https://images.unsplash.com/photo-1528819622765-d6bcf132f793?w=640&q=80',
};

const DEFAULT_CRAFT_COVER = CRAFT_COVER_MAP['其它'];

const TITLE_CATEGORY_RULES = [
  [/缠花|发簪|绒花/, '缠花'],
  [/拼豆|融合豆|perler/i, '拼豆'],
  [/钩织|毛线|编织|钩针|crochet/i, '钩织'],
  [/滴胶|干花|resin/i, '滴胶'],
  [/穿戴甲|美甲|nails/i, '穿戴甲'],
  [/粘土|黏土|软陶|陶瓷|陶艺|clay/i, '粘土'],
  [/刺绣|embroidery/i, '刺绣'],
  [/串珠|bead/i, '串珠'],
  [/皮艺|皮雕|leather/i, '皮艺'],
  [/木工|木作|wood/i, '木工'],
  [/香薰|蜡烛|candle/i, '香薰'],
  [/纸艺|衍纸|paper/i, '纸艺'],
  [/团扇|tuanshan/i, '团扇'],
  [/花灯|灯笼|lantern/i, '花灯'],
  [/摄影|photography/i, '摄影'],
  [/设计|design/i, '设计'],
];

function normalizeCraftCategory(category) {
  const raw = (category || '').trim();
  if (!raw) return '其它';
  if (CRAFT_COVER_MAP[raw]) return raw;
  if (CATEGORY_ALIASES[raw]) return CATEGORY_ALIASES[raw];
  return raw;
}

function matchText(text) {
  const value = (text || '').trim();
  if (!value) return null;
  for (let i = 0; i < TITLE_CATEGORY_RULES.length; i += 1) {
    const rule = TITLE_CATEGORY_RULES[i];
    if (rule[0].test(value)) return rule[1];
  }
  return null;
}

function inferCraftCategoryFromText(category, title, description) {
  const fromTitle = matchText(title || '');
  if (fromTitle) return normalizeCraftCategory(fromTitle);
  const fromAll = matchText(`${title || ''} ${description || ''}`);
  if (fromAll) return normalizeCraftCategory(fromAll);
  return normalizeCraftCategory(category);
}

function getCraftCoverImage(category, title, description) {
  const key = inferCraftCategoryFromText(category, title, description);
  if (CRAFT_COVER_MAP[key]) return CRAFT_COVER_MAP[key];
  return getCategoryCoverImage(key);
}

function getCraftCoverFallback(category, stage, title, description) {
  const key = inferCraftCategoryFromText(category, title, description);
  if (stage <= 1) return getCategoryCoverImage(key);
  return getCategoryCoverImage('其它');
}

module.exports = {
  CRAFT_COVER_MAP,
  CATEGORY_ALIASES,
  DEFAULT_CRAFT_COVER,
  normalizeCraftCategory,
  inferCraftCategoryFromText,
  getCraftCoverImage,
  getCraftCoverFallback,
};
