import * as React from 'react';
import { Column } from '../../common/ducks/GridPage';
import { ConfigItem } from './types';
import { DuckCmpProps } from 'saga-duck';
import SevicePageDuck from './PageDuck';
import { Link } from 'react-router-dom';
import { Bubble, Button, Text } from 'tea-component';
import formatDate from '@src/common/util/formatDate';
import CopyableText from '@src/common/components/CopyableText';

export default ({ duck: { creators }, dispatch }: DuckCmpProps<SevicePageDuck>): Column<ConfigItem>[] => [
  {
    key: 'id',
    header: '配置ID/名称',
    render: x => (
      <>
        <CopyableText
          text={x.configId}
          component={
            <Link to={`/config/detail?id=${x.configId}&namespaceId=${x.namespaceId}&registryId=${x.registryId}`}>
              {x.configId}
            </Link>
          }
        />
        <Text parent='p'>
          <CopyableText text={x.configName} />
        </Text>
      </>
    ),
  },
  {
    key: 'service',
    header: '所属服务',
    render: x => (
      <Text overflow tooltip={x.serviceName}>
        {x.serviceName || '-'}
      </Text>
    ),
  },
  {
    key: 'namespace',
    header: '命名空间ID/名称',
    render: x => (
      <>
        <Text parent='p'>
          <Text overflow tooltip={x.namespaceId}>
            {x.namespaceId}
          </Text>
        </Text>
        <Text parent='p'>
          <Text overflow tooltip={x.namespaceName}>
            {x.namespaceName}
          </Text>
        </Text>
      </>
    ),
  },
  {
    key: 'count',
    header: '配置版本',
    render: x => (
      <Text overflow tooltip={x.versionCount}>
        {x.versionCount}
      </Text>
    ),
  },
  {
    key: 'type',
    header: '配置类型',
    render: x => (
      <Text overflow tooltip={x.configType}>
        {x.configType}
      </Text>
    ),
  },
  {
    key: 'tag',
    header: '系统标签',
    render: x => {
      const tag = x.systemTag ? JSON.parse(x.systemTag) : null;
      return (
        <>
          {tag
            ? Object.keys(tag)?.map(key => (
                <Bubble key={key} content={`${key} : ${tag[key]}`}>
                  <Text parent='p'>
                    {key}: {tag[key]}
                  </Text>
                </Bubble>
              ))
            : '-'}
        </>
      );
    },
  },
  {
    key: 'create',
    header: '创建时间',
    render: x => (
      <Text overflow tooltip={formatDate(x.createTime)}>
        {formatDate(x.createTime)}
      </Text>
    ),
  },
  {
    key: 'release',
    header: '最新发布时间',
    render: x => (
      <Text overflow tooltip={formatDate(x.releaseTime)}>
        {formatDate(x.releaseTime) || '-'}
      </Text>
    ),
  },
  {
    key: 'operation',
    header: '操作',
    render: x => (
      <>
        <Button type='link' onClick={() => dispatch(creators.delete([x]))}>
          删除
        </Button>
        <Button
          disabled={!x.lastReleaseVersion}
          type='link'
          onClick={() => dispatch(creators.configureVersion(x.configId))}
          tooltip={!x.lastReleaseVersion && '当前配置不支持回滚操作'}
        >
          回滚
        </Button>
      </>
    ),
  },
];
