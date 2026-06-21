const REQUEST_CATEGORIES = [
  { label: '钩织', value: '钩织' },
  { label: '滴胶', value: '滴胶' },
  { label: '穿戴甲', value: '穿戴甲' },
  { label: '粘土', value: '粘土' },
  { label: '缠花', value: '缠花' },
  { label: '拼豆', value: '拼豆' },
  { label: '刺绣', value: '刺绣' },
  { label: '串珠', value: '串珠' },
  { label: '其它', value: '其它' },
];

const CATEGORY_FILTER_OPTIONS = [{ label: '全部分类', value: 'all' }, ...REQUEST_CATEGORIES];

const REQUEST_SORT_TABS = [
  { value: 'latest', label: '最新' },
  { value: 'budget', label: '预算高' },
  { value: 'deadline', label: '工期紧' },
];

function getRequestStatusLabel(status) {
  if (status === 'OPEN') return '招募中';
  if (status === 'MATCHED') return '已匹配';
  return '已结束';
}

module.exports = {
  REQUEST_CATEGORIES,
  CATEGORY_FILTER_OPTIONS,
  REQUEST_SORT_TABS,
  getRequestStatusLabel,
};
