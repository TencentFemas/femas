import { RegistryItem } from './../registry/types';

export interface NamespaceItem {
  id: string;
  desc: string;
  name: string;
  namespaceId: string;
  registry: Array<RegistryItem>;
  registryId: string;
  serviceCount: number;
}
