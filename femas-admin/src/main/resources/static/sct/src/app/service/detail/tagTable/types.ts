// 逻辑关系

export enum OPERATOR {
  EQUAL = 'EQUAL', // 相等
  NOTEQUAL = 'NOT_EQUAL', //不等于
  IN = 'IN', // 包含
  NOTIN = 'NOT_IN', // 不包含
  REGEX = 'REGEX', // 正则
}

export const OPERATOR_MAP = {
  [OPERATOR.EQUAL]: '等于',
  [OPERATOR.NOTEQUAL]: '不等于',
  [OPERATOR.IN]: '包含',
  [OPERATOR.NOTIN]: '不包含',
  [OPERATOR.REGEX]: '正则表达式',
};

// 系统标签
export enum SYSTEM_TAG {
  TSF_LOCAL_SERVICE = 'source.service.name',
  TSF_LOCAL_NAMESPACE_SERVICE = 'source.namespace.service.name',
  TSF_APPLICATION_ID = 'source.application.id',
  TSF_DEST_APPLICATION_ID = 'destination.application.id',
  TSF_APP_VERSION = 'source.application.version',
  TSF_DEST_APP_VERSION = 'destination.application.version',
  TSF_GROUP_ID = 'source.group.id',
  TSF_DEST_GROUP_ID = 'destination.group.id',
  TSF_LOCAL_API_PATH = 'source.interface',
  TSF_DEST_IP = 'destination.connection.ip',
  TSF_LOCAL_IP = 'source.connection.ip',
  TSF_DEST_API_PATH = 'destination.interface',
  TSF_HTTP_METHOD = 'request.http.method',
}

// 系统标签下拉框
export const SYSTEM_TAG_MAP = {
  [SYSTEM_TAG.TSF_LOCAL_SERVICE]: '调用方服务名',
  [SYSTEM_TAG.TSF_LOCAL_NAMESPACE_SERVICE]: '命名空间+调用方服务名',
  [SYSTEM_TAG.TSF_APPLICATION_ID]: '上游应用',
  [SYSTEM_TAG.TSF_APP_VERSION]: '上游应用版本号',
  [SYSTEM_TAG.TSF_GROUP_ID]: '上游部署组',
  [SYSTEM_TAG.TSF_LOCAL_IP]: '调用方IP',
  [SYSTEM_TAG.TSF_DEST_APPLICATION_ID]: '当前应用',
  [SYSTEM_TAG.TSF_DEST_APP_VERSION]: '当前应用版本号',
  [SYSTEM_TAG.TSF_DEST_GROUP_ID]: '当前部署组',
  [SYSTEM_TAG.TSF_DEST_API_PATH]: '当前服务API PATH',
  [SYSTEM_TAG.TSF_HTTP_METHOD]: '请求 HTTP METHOD',
};

export const SYSTEM_TAG_OPTIONS = map2options(SYSTEM_TAG_MAP);

// 标签类型
export enum TAG_TYPE {
  SYSTEM = 'S',
  CUSTOM = 'U',
}

export const TAG_TYPE_MAP = {
  [TAG_TYPE.SYSTEM]: '服务标签',
  [TAG_TYPE.CUSTOM]: '自定义标签',
};
export const TAG_TYPE_OPTIONS = map2options({
  [TAG_TYPE.SYSTEM]: '服务标签',
  [TAG_TYPE.CUSTOM]: '自定义标签',
});

// 没有正则表达式的操作符
export const OPERATOR_NO_REGEX = [OPERATOR.EQUAL, OPERATOR.NOTEQUAL, OPERATOR.IN, OPERATOR.NOTIN];
export const OPERATOR_ALL = [...OPERATOR_NO_REGEX, OPERATOR.REGEX];

// 标签key 与 逻辑关系的对应关系
export const KEY_OPERATOR_MAP = {
  // 主调
  [SYSTEM_TAG.TSF_LOCAL_SERVICE]: OPERATOR_ALL,
  [SYSTEM_TAG.TSF_LOCAL_NAMESPACE_SERVICE]: OPERATOR_ALL,
  [SYSTEM_TAG.TSF_APPLICATION_ID]: OPERATOR_NO_REGEX,
  [SYSTEM_TAG.TSF_APP_VERSION]: OPERATOR_ALL,
  [SYSTEM_TAG.TSF_GROUP_ID]: OPERATOR_NO_REGEX,
  [SYSTEM_TAG.TSF_LOCAL_IP]: OPERATOR_ALL,
  // 被调
  [SYSTEM_TAG.TSF_DEST_APPLICATION_ID]: OPERATOR_NO_REGEX,
  [SYSTEM_TAG.TSF_DEST_APP_VERSION]: OPERATOR_ALL,
  [SYSTEM_TAG.TSF_DEST_GROUP_ID]: OPERATOR_NO_REGEX,
  [SYSTEM_TAG.TSF_DEST_API_PATH]: OPERATOR_ALL,
  // http method
  [SYSTEM_TAG.TSF_HTTP_METHOD]: [OPERATOR.EQUAL, OPERATOR.NOTEQUAL],
};

export const HTTP_METHOD = ['POST', 'GET', 'PUT', 'HEAD', 'DELETE', 'CONNECT', 'OPTIONS', 'TRACE', 'PATCH'];

function map2options(map) {
  const options = [];
  for (const key in map) {
    options.push({
      value: key,
      label: map[key],
    });
  }
  return options;
}
