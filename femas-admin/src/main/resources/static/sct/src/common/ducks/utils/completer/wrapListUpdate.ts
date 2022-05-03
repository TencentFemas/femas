import { UPDATE } from './Base';

/**
 * 将列表更新(updateList)包装为UPDATE方法，支持列表更新或按索引更新
 * @param updateList
 */
export function wrapListUpdate<T>(updateList: (list: T[]) => any): UPDATE<T> {
  return function update(index: number | T[], data?: any) {
    if (arguments.length <= 1) {
      return updateList(index as T[]);
    } else {
      const list = [];
      list[index as number] = data;
      return updateList(list);
    }
  };
}
