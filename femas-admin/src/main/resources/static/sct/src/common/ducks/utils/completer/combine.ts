import { Completer } from './Base';
import { parallel } from 'redux-saga-catch';

/** 合并多个Completer，并行/串行处理 */
export function combineCompleters<TItem, TFilter>(
  Completers: (new (filter?) => Completer<TItem, any, TFilter>)[],
  runInParallel = true,
) {
  return class CombinedCompleter extends Completer<TItem, any> {
    private _completers: Completer<TItem, any>[];

    *onInit() {
      this._completers = Completers.map(C => new C(this.filter));
      yield parallel(this._completers.map(c => [c, c.init]));
    }

    *onFetched() {
      const update = this.selfUpdate.bind(this);
      const self = this;
      if (runInParallel) {
        // parallel容错
        yield parallel(
          this._completers.map(
            c =>
              function*() {
                yield* c.fetched(self.list, self.totalCount, update);
              },
          ),
        );
      } else {
        for (const completer of this._completers) {
          // 容错处理，避免一个Completer失败导致其它的也中止
          try {
            yield* completer.fetched(this.list, this.totalCount, update);
          } catch (e) {}
        }
      }
    }

    /** 维护本地的list，使在串行执行时能取到已更新的值 */
    *selfUpdate(index: number | any[], o?: any) {
      if (arguments.length <= 1) {
        const list = index as any[];
        this.list = this.list.slice(0);
        list.forEach((v, index) => {
          if (!v) {
            return;
          }
          this.list[index] = Object.assign({}, this.list[index], v);
        });
        yield* this.update(list);
      } else {
        const list = this.list.slice(0);
        const idx = index as number;
        list[idx] = Object.assign({}, list[idx], o);
        this.list = list;
        yield* this.update(idx, o);
      }
    }
  };
}
