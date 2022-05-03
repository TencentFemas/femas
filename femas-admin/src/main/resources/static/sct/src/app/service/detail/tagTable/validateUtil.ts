import { OPERATOR, SYSTEM_TAG, SYSTEM_TAG_MAP } from './types';
import { CONDITION_KEY } from './TagTable';

const KEY_RULE = /^[A-Za-z0-9\-\._]+$/;
const keyTypeErrorMsg = '仅支持字母、数字、及分隔符（"-"、"."、"_")';
const KEY_MAX_LENGTH = 32;
const keyLengthErrorMsg = `标签名长度不超过${KEY_MAX_LENGTH}个字符`;
const SIGNAL_VAL_MAX_LENGTH = 128;
const MULTI_VAL_MAX_LENGTH = 200;
const signalValLengthErrorMsg = `单个值长度${SIGNAL_VAL_MAX_LENGTH}个字符`;
const MultiValLengthErrorMsg = `多个值长度总和不超过${MULTI_VAL_MAX_LENGTH}个字符`;

export function validateKey(str, list) {
  if (str === null || str === '') {
    return '标签名不能为空';
  } else if (!KEY_RULE.test(str)) {
    return keyTypeErrorMsg;
  } else if (str.length > KEY_MAX_LENGTH) {
    return keyLengthErrorMsg;
  }

  const mutex = `不能同时选择 “${SYSTEM_TAG_MAP[SYSTEM_TAG.TSF_LOCAL_SERVICE]}” 、“${
    SYSTEM_TAG_MAP[SYSTEM_TAG.TSF_LOCAL_NAMESPACE_SERVICE]
  }“ 标签`;

  function hasTag(tag: string) {
    return list.findIndex(item => item[CONDITION_KEY.KEY] === tag) >= 0;
  }

  if (
    (str === SYSTEM_TAG.TSF_LOCAL_SERVICE && hasTag(SYSTEM_TAG.TSF_LOCAL_NAMESPACE_SERVICE)) ||
    (str === SYSTEM_TAG.TSF_LOCAL_NAMESPACE_SERVICE && hasTag(SYSTEM_TAG.TSF_LOCAL_SERVICE))
  ) {
    return mutex;
  }
}

export function validateVal({ tagOperator: operator }, str) {
  if ([OPERATOR.EQUAL, OPERATOR.NOTEQUAL].includes(operator)) {
    // 相等不相等
    if (str.indexOf(',') >= 0) {
      return '只能输入一个值';
    } else if (str.length > SIGNAL_VAL_MAX_LENGTH) {
      return signalValLengthErrorMsg;
    } else if (str === null || str === '') {
      return '值不能为空';
    }
  } else if ([OPERATOR.IN, OPERATOR.NOTIN].includes(operator)) {
    // 包含不包含
    const values = str.split(',');
    // 多值有可能存在其中一个值没有的情况 比如 1,
    if (values.some(val => val === null || val === '')) {
      return '值不能为空';
    }
    // 多值有可能存在其中一个值长度超过128个字符的情况
    if (values.some(val => val.length > SIGNAL_VAL_MAX_LENGTH)) {
      return signalValLengthErrorMsg;
    }
    // 多值总长度
    const allStrLength = values.reduce((acc, cur) => {
      acc += cur.length;
      return acc;
    }, 0);

    if (allStrLength > MULTI_VAL_MAX_LENGTH) {
      return MultiValLengthErrorMsg;
    }
  } else {
    // 正则
    try {
      new RegExp(str);
    } catch (e) {
      return '请输入合法正则表达式';
    }
  }
}
