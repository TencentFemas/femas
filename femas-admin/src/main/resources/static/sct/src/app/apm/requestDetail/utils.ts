import { TAG_GROUP } from './types';

export const convertTreeTags = tags => {
  if (!tags?.length) return [];
  // 处理系统tag&业务tag，__开头为系统tag
  const systemTags = tags?.filter(tag => tag.key.startsWith('__'));
  const customTags = tags?.filter(tag => !tag.key.startsWith('__'));

  return [
    {
      key: TAG_GROUP.custom,
      value: '',
      tags: customTags,
    },
    {
      key: TAG_GROUP.system,
      value: '',
      tags: systemTags,
    },
  ];
};

/**
 * 生成子孙关系信息
 */
export const getRecordRelations = records => {
  const relations = {};
  for (const record of records) {
    for (const tag of record.tags) {
      relations[tag.key] = record.key;
    }
  }
  return relations;
};
