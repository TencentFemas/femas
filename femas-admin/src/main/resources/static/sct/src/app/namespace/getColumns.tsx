import * as React from 'react';
import { Column } from '../../common/ducks/GridPage';
import { NamespaceItem } from './types';
import { DuckCmpProps } from 'saga-duck';
import SevicePageDuck from './PageDuck';
import { Button, ExternalLink, Text } from 'tea-component';
import CopyableText from '@src/common/components/CopyableText';
import router from '@src/common/util/router';

export default ({
  duck: { creators, selectors },
  dispatch,
  store,
}: DuckCmpProps<SevicePageDuck>): Column<NamespaceItem>[] => {
  const grafanaUrl = selectors.grafanaUrl(store);
  return [
    {
      key: 'id',
      header: '命名空间ID/名称',
      render: x => (
        <>
          <Text parent='p'>
            <CopyableText text={x.namespaceId} />
          </Text>
          <CopyableText text={x.name} />
        </>
      ),
    },
    {
      key: 'registry',
      header: '关联注册中心',
      render: x => (
        <>
          {x.registry?.length
            ? x.registry?.map(d => (
                <Text parent='p' key={d.registryId}>
                  <Button type='link' onClick={() => router.navigate(`/registry/detail?id=${d.registryId}`)}>
                    {x.registryId}
                  </Button>
                  <Text> ({d.registryName})</Text>
                </Text>
              ))
            : '-'}
        </>
      ),
    },
    {
      key: 'service',
      header: '实例服务个数',
      render: x => (
        <Button
          type='link'
          onClick={() => router.navigate(`/service?registryId=${x.registryId}&namespaceId=${x.namespaceId}`)}
        >
          {x.serviceCount}
        </Button>
      ),
    },
    {
      key: 'desc',
      header: '描述',
      render: x => <Text tooltip={x.desc}>{x.desc || '-'}</Text>,
    },
    {
      key: 'operation',
      header: '操作',
      render: x => (
        <>
          <ExternalLink
            style={{ marginRight: 10 }}
            href={`${grafanaUrl}/d/tXtxs3n7k/fu-wu-zhi-biao-jian-kong-ming-ming-kong-jian-wei-du?orgId=1&var-namespaceId=${x.namespaceId}`}
          >
            监控详情
          </ExternalLink>
          <Button type='link' onClick={() => dispatch(creators.edit(x))}>
            编辑
          </Button>
          <Button type='link' onClick={() => dispatch(creators.remove(x))}>
            删除
          </Button>
        </>
      ),
    },
  ];
};
