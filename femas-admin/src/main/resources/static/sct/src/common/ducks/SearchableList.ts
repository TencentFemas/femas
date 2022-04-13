/**
 * 可搜索列表，有以下功能
 *
 * 1. 关键字搜索/输入自动搜索
 * 2. 加载更多
 */

import Base, { SearchParam as BaseSearchParam } from './AbstractSearchableList';

export interface SearchParam extends BaseSearchParam {
  keyword?: string;
}

export default abstract class SearchableSelect extends Base {
  LocalParam: string;

  get defaultLocalParam() {
    return '';
  }

  getParam(base: this['Param'], local: this['LocalParam'], paging: BaseSearchParam): this['Param'] & SearchParam {
    return {
      ...base,
      ...paging,
      keyword: local,
    };
  }
}
