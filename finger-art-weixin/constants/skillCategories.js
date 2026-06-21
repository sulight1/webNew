const SKILL_CATEGORIES = [
  { value: '钩织', label: '钩织' },
  { value: '滴胶', label: '滴胶' },
  { value: '缠花', label: '缠花' },
  { value: '粘土', label: '粘土' },
  { value: '拼豆', label: '拼豆' },
  { value: '刺绣', label: '刺绣' },
  { value: '摄影', label: '摄影' },
  { value: '设计', label: '设计' },
];

const SKILL_FILTER_OPTIONS = [{ value: 'all', label: '全部' }, ...SKILL_CATEGORIES];

const SKILL_SORT_TABS = [
  { value: 'recommend', label: '推荐' },
  { value: 'latest', label: '最新' },
  { value: 'credit', label: '信用高' },
  { value: 'rating', label: '评分高' },
];

module.exports = {
  SKILL_CATEGORIES,
  SKILL_FILTER_OPTIONS,
  SKILL_SORT_TABS,
};
