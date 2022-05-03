export interface InterfaceItem {
  id: string;
  path: string;
  method: string;
  status: string;
  namespaceId: string;
  lastUpdateTime: number;
  serviceVersion: string;
}

export enum STATUS {
  UP = '1',
  DOWN = '2',
}

export const STATUS_MAP = {
  [STATUS.UP]: '在线',
  [STATUS.DOWN]: '离线',
};

export const STATUS_THEME_MAP = {
  [STATUS.UP]: 'success',
  [STATUS.DOWN]: 'danger',
};
