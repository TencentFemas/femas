/**
 * GridPage/DetailPage通用数据补全
 */

/**
 * 列表信息实全对象
 *
 * 用于列表页的代码参考模板： http://code.qcm.oa.com/?tpl=gridpage&addons=completer.auto-completer&patchOnly=1
 *
 * 用法：
 * ```ts
 * class MyCompleter extends Completer{
 *    *onFetched(){
 *      const data = yield fetchSomeThing()
 *      // 单条更新
 *      for(let i=0; i<list.length; i++){
 *        yield* update(i, { someProperty: data[list[i].id] })
 *      }
 *      // 批量更新
 *      yield* update(list.map(item=>data[item.id]))
 *    }
 * }
 *
 * class MyGridPageDuck extends GridPageDuck{
 *    get completer(){
 *      return MyCompleter
 *    }
 * }
 * ```
 *

 *
 * 单独使用：
 * ```ts
 * // 创建实例
 * const completer = new Completer()
 * // 请求开始，传入参数
 * yield* completer.init(filter)
 * // 请求结束，传入原始列表及更新方法，供completer后续处理
 * yield* completer.fetched(list, totalCount, update)
 * ```
 */

export abstract class Completer<TItem, TExtra, TFilter = any> {
  protected list: TItem[];
  protected totalCount: number;
  protected update: UPDATE<TExtra>;

  /** 请求参数 */
  constructor(protected filter: TFilter = null) {}

  /** 如果列表为空，不进行补全 */
  protected get ignoreEmptyList() {
    return true;
  }

  *init(filter?: TFilter): IterableIterator<any> {
    if (filter) {
      this.filter = filter;
    }
    yield* this.onInit();
  }

  /** 列表刚开始加载，此时可访问 this.filter  */
  *onInit(): IterableIterator<any> {}

  *fetched(list: TItem[], totalCount: number, update: UPDATE<TExtra>): IterableIterator<any> {
    this.list = list;
    this.totalCount = totalCount;
    this.update = update;
    if (this.ignoreEmptyList && list.length <= 0) {
      return;
    }
    yield* this.onFetched();
  }

  /** 列表加载完成，此时可访问 this.list, this.totalCount, this.update */
  *onFetched(): IterableIterator<any> {}
}

/** 数据更新方法 */
export interface UPDATE<TExtra> {
  (index: number, o: TExtra): any;

  (list: TExtra[]): any;
}
