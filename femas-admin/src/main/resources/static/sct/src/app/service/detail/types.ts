export enum TAB {
  BaseInfo = 'baseinfo',
  Api = 'api',
  Event = 'event',
  Auth = 'auth',
  Limit = 'limit',
  Route = 'route',
  Fuse = 'fuse',
}

export const TAB_LABLES = {
  [TAB.BaseInfo]: '服务概览',
  [TAB.Api]: '接口列表',
  [TAB.Auth]: '服务鉴权',
  [TAB.Limit]: '服务限流',
  [TAB.Route]: '服务路由',
  [TAB.Fuse]: '服务熔断',
  [TAB.Event]: '服务事件',
};

export interface ComposedId {
  registryId: string;
  serviceName: string;
  namespaceId: string;
  versions?: Array<string>;
  ruleId?: string;
}
