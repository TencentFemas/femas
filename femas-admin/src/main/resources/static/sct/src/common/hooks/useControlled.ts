import * as React from 'react';

/**
 * 将有内部状态管理的组件封装为一个受控组件，为DuckCmp设计，也可用于类似的其它场景
 *
 * 例如：
 ```
 function Component({value, onChange}){
    useControlled(
        value, 
        onChange, 
        duck.selectors.id, 
        (v)=>dispatch(duck.creators.select(v))
    )
}
 ```
 * @param outerValue 外部指定的受控值
 * @param outerChange 内部值变化时通知外部
 * @param innerValue 内部状态的值
 * @param innerChange 外部值变化时更新内部状态
 * @param equal 如何比较值，默认直接使用 "==="
 */
export default function useControlled<T>(
  outerValue: T,
  outerChange: (v: T) => any,
  innerValue: T,
  innerChange: (v: T) => any,
  equal: (a: T, b: T) => boolean = (a, b) => a === b,
) {
  const lastOuterValue = React.useRef<T>();
  React.useEffect(() => {
    if (!equal(outerValue, innerValue)) {
      if (!equal(outerValue, lastOuterValue.current)) {
        // 外 -> 内
        innerChange(outerValue);
      } else {
        // 内 -> 外
        outerChange(innerValue);
      }
    }
    lastOuterValue.current = outerValue;
  }, [outerValue, innerValue]);
}
