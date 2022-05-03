export interface RegistryItem {
  id: string;
  registryCluster: string;
  registryId: string;
  registryName: string;
  registryType: string;
  status?: number;
  kubeConfig: string;
  secret: string;
  certificateType: string;
  apiServerAddr: string;
}

export enum ClusterType {
  Consul = 'consul',
  Eureka = 'eureka',
  Nacos = 'nacos',
  Polaris = 'polaris',
  K8s = 'k8s',
}

export const ClusterTypeMap = {
  [ClusterType.Consul]: 'Consul',
  [ClusterType.Eureka]: 'Eureka',
  [ClusterType.Nacos]: 'Nacos',
  [ClusterType.Polaris]: '北极星Polaris',
  [ClusterType.K8s]: 'Kubenetes服务发现',
};

export enum RegistryStatus {
  'Nomarl' = 1,
  'Exception' = 2,
}

export const RegistryStatusMap = {
  [RegistryStatus.Nomarl]: '正常',
  [RegistryStatus.Exception]: '异常',
};

export enum K8S_NATIVE_TYPE {
  kubeconfig = 'config',
  serviceAccount = 'account',
}

export const K8S_NATIVE_TYPE_NAME = {
  [K8S_NATIVE_TYPE.kubeconfig]: 'Kubeconfig',
  [K8S_NATIVE_TYPE.serviceAccount]: 'Service Account',
};

export enum K8S_HTTP_PROTOCOL {
  http = 'http://',
  https = 'https://',
}
