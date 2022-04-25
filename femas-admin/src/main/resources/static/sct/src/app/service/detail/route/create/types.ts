export enum TARGET_TYPE {
  VERSION = 'TSF_PROG_VERSION',
}

export const TARGET_TYPE_NAME = [{ value: TARGET_TYPE.VERSION, name: '版本号' }];

export interface SearchParams {
  namespaceId: string;
  serviceName: string;
}
