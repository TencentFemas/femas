/* eslint-disable prettier/prettier */
import * as deepEqual from 'fast-deep-equal';

interface CacheMethod<T> {
  (args: any[], result?: T);
}

interface Cache<T> {
  [key: string]: T;
}

/** 防重入，注意传入的参数必须可以JSON.stringify序列化 */
export function once<TFunction extends (...args: any[]) => any>(
    f: TFunction,
    cache?: CacheMethod<ReturnType<TFunction>>,
): TFunction {
  if (!cache) {
    const resultMap: Cache<ReturnType<TFunction>> = {};
    cache = (args, result) => {
      const key = JSON.stringify(args);
      if (result) {
        resultMap[key] = result;
      }
      return resultMap[key];
    };
  }
  return function (...args: any[]) {
    let result = cache.call(this, args);
    if (!result) {
      result = cache.call(this, args, f.apply(this, args));
    }
    return result;
  } as TFunction;
}

/**
 * 设置有效期，使用方法：
 * once(fn, ttl(5000)) // 直接用法
 * @cacheableWith(ttl(5000)) // 装饰器用法
 *
 * TODO 这里用于 cacheableWith 时会有bug，导致不同实例共享结果，需要修复
 */
export function ttl<T>(ms = 5 * 60 * 1000): CacheMethod<T> {
  const resultMap: Cache<T> = {};
  return function (args: any[], result: T) {
    const key = JSON.stringify(args);
    if (result) {
      resultMap[key] = result;
      setTimeout(() => {
        delete resultMap[key];
      }, ms);
    }
    return resultMap[key];
  };
}

/**
 * 类似memoize-once，只判断上一次的值，并进行深度对比
 *
 * TODO 这里用于 cacheableWith 时会有bug，导致不同实例共享结果，需要修复
 */
export function untilChange<T>(): CacheMethod<T> {
  let lastArgs: any[];
  let lastResult: T;
  return function (args: any[], result: T): T {
    if (result) {
      return (lastResult = result);
    }
    return deepEqual(args, lastArgs) ? lastResult : null;
  };
}

/**
 * Decorator版缓存，设计为class method使用，也可以直接调用来包装function
 * 注意： decorator标准只为class设计，所以应避免在function上使用，而应该直接调用once
 */
export const cacheable = cacheableWith(null);

/**
 * Decorator构造器
 */
export function cacheableWith<T>(cacheMethod: CacheMethod<T>) {
  return function cacheable(target, propertyKey: string, descriptor: PropertyDescriptor) {
    if (propertyKey && descriptor === undefined) {
      descriptor = Object.getOwnPropertyDescriptor(target, propertyKey);
    }
    let cache = cacheMethod;
    if (!cache) {
      // 生成随机数，以兼容继承的情况
      const cacheKey = `_once_cache_${propertyKey}_${Math.random()}`;
      cache = function (args, result) {
        const key = JSON.stringify(args);
        const cache = this[cacheKey] || (this[cacheKey] = {});
        if (result) {
          return (cache[key] = result);
        }
        return cache[key];
      };
    }
    // 缓存放在实例上
    const originalFn = descriptor.value;
    descriptor.value = once(originalFn, cache);
    return descriptor;
  };
}
