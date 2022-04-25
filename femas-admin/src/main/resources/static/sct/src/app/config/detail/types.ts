export enum TAB {
  BaseInfo = 'baseinfo',
  Version = 'version',
}

export const TAB_LABLES = {
  [TAB.BaseInfo]: '基本信息',
  [TAB.Version]: '配置版本',
};

export interface ComposedId {
  namespaceId: string;
  configId: string;
}
