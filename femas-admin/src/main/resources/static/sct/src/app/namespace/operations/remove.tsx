import { NamespaceItem } from '../types';
import { Modal } from 'tea-component';
import { deleteNamespace } from '../model';

export default async function remove(instance: NamespaceItem): Promise<boolean> {
  let successed = false;
  await Modal.confirm({
    message: '确认删除当前所选命名空间？',
    description: `命名空间删除后，该命名空间(${instance.name})下的所有配置将会被清空，且无法恢复。`,
    okText: '删除',
    cancelText: '取消',
    onOk: async () => {
      try {
        await deleteNamespace({
          namespaceId: instance.namespaceId,
        });
        successed = true;
      } catch (e) {}
    },
  });

  return successed;
}
