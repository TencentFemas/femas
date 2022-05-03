import { RegistryItem } from '../types';
import { Modal } from 'tea-component';
import { deleteRegistryCluster } from '../model';

export default async function remove(instance: RegistryItem): Promise<boolean> {
  let successed = false;
  await Modal.confirm({
    message: '确认删除当前所选注册中心？',
    description: `注册中心删除后，该注册中心(${instance.registryName})下的所有配置将会被清空，且无法恢复。`,
    okText: '删除',
    cancelText: '取消',
    onOk: async () => {
      try {
        await deleteRegistryCluster({
          registryId: instance.registryId,
        });
        successed = true;
      } catch (e) {}
    },
  });

  return successed;
}
