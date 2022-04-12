import { useEffect, useMemo, useState } from 'react';

export function takeLastAsyncEffectGen() {
  let lastPromise = null;
  // eslint-disable-next-line prettier/prettier
  return function (callback, effectFun: () => Promise<any>, effectDependents?: any[]) {
    useEffect(() => {
      const currentPromise = effectFun();
      lastPromise = currentPromise;
      lastPromise &&
        lastPromise.then(result => {
          if (lastPromise === currentPromise) {
            callback(result);
          }
        });
    }, effectDependents);
  };
}

/**
 * 带有异步 effect 的 state。如果同时有多个依赖变更引起的异步过程，则只有最后一个生效
 * @param fetchState
 * @param dependents
 */
export function useStateWithAsyncDependents<T>(fetchState: () => Promise<T>, dependents?) {
  const [x, setX] = useState<T>(null);
  const asyncEffect = useMemo(takeLastAsyncEffectGen, []);
  asyncEffect(
    newState => {
      setX(newState);
    },
    fetchState,
    dependents,
  );
  return [x, setX];
}
