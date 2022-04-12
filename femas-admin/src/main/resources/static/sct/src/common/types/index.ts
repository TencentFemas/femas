export interface Action<T> {
  type: string;
  payload: T;
}

export interface FilterTime {
  startTime: number;
  endTime: number;
}

export const nameTipMessage = '最长为60个字符，只能包含小写字母、数字及分隔符("_"、"-")，且不能以分隔符开头或结尾';
