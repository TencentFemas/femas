export interface ConfigItem {
  configId: string;
  configName: string;
  registryId: string;
  namespaceId: string;
  namespaceName: string;
  serviceName: string;
  systemTag: string;
  versionCount: number;
  configType: string;
  configValue: string;
  configDesc: string;
  createTime: number;
  releaseTime: number;
  currentReleaseVersion: VersionItem;
  lastReleaseVersion: VersionItem;
}

export interface VersionItem {
  configVersionId: string;
  configVersion: number;
  configType: string;
  configValue: string;
  createTime: number;
  releaseTime: number;
  releaseStatus: string;
}

export enum CONFIG_TYPE {
  yaml = 'yaml',
  properties = 'properties',
}

export enum RELEASE_STATUS {
  undo = 'U',
  doing = 'V',
  success = 'S',
  failed = 'F',
}

export const RELEASE_STATUS_NAME = {
  [RELEASE_STATUS.undo]: '未发布',
  [RELEASE_STATUS.doing]: '生效中',
  [RELEASE_STATUS.success]: '发布成功',
  [RELEASE_STATUS.failed]: '发布失败',
};

export const RELEASE_STATUS_THEME = {
  [RELEASE_STATUS.success]: 'success',
  [RELEASE_STATUS.doing]: 'success',
  [RELEASE_STATUS.failed]: 'danger',
};
