import { Completer } from './Base';

/**
 * 只更新列表中指定的条目，比如：只更新列表中激活的项，用于详情展开展示
 * ```ts
 * const FilteredCompleter = filterCompleter(FooCompleter, item => item.expanded)
 * const completer = new FilteredCompleter()
 * yield* completer.init(filter)
 * yield* completer.fetched(list, totalCount, update)
 * ```
 *
 * 代码参考模板： http://code.qcm.oa.com/?tpl=gridpage&addons=drawerdetail-completer&patchOnly=1
 *
 * @param completer 原始更新函数
 * @param filter 指定哪些条目需要更新
 */
export function filterCompleter<TItem, TExtra, TFilter = any>(
  OriginCompleter: new (filter?) => Completer<TItem, TExtra, TFilter>,
  filterFn: (item: TItem, filter: TFilter) => boolean,
) {
  return class FilteredCompleter extends Completer<TItem, TExtra, TFilter> {
    private childCompleter: Completer<TItem, TExtra, TFilter>;

    constructor() {
      // eslint-disable-next-line prefer-rest-params
      super(...arguments);
      this.childCompleter = new OriginCompleter();
    }

    *onInit() {
      yield* this.childCompleter.init(this.filter);
    }

    *onFetched() {
      const { filter, list, totalCount, update } = this;
      // 保存过滤过的数组index到原数组index的映射
      const filteredIndexMap = new Map<number, number>();
      const filtered: TItem[] = [];
      for (const [index, item] of list.entries()) {
        if (filterFn(item, filter)) {
          filteredIndexMap.set(filtered.length, index);
          filtered.push(item);
        }
      }
      yield* this.childCompleter.fetched(filtered, totalCount, function*(index: number | TExtra[], data?: any) {
        if (arguments.length <= 1) {
          // 将过滤后的列表恢复为原列表的对应位置
          const list = index as TExtra[];
          const originList: TExtra[] = [];
          for (const [index, data] of list.entries()) {
            originList[filteredIndexMap.get(index)] = data;
          }
          yield* update(originList);
        } else {
          yield* update(filteredIndexMap.get(index as number), data);
        }
      });
    }
  };
}
