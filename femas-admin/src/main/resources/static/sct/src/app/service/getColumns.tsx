import * as React from 'react';
import { Column } from '../../common/ducks/GridPage';
import { ServiceItem, ServiceStatusName, ServiceStatusTheme } from './types';
import { DuckCmpProps } from 'saga-duck';
import SevicePageDuck from './PageDuck';
import { Link } from 'react-router-dom';
import { ExternalLink, Text } from 'tea-component';
import CopyableText from '@src/common/components/CopyableText';

export default ({ store, duck: { selectors } }: DuckCmpProps<SevicePageDuck>): Column<ServiceItem>[] => {
  const grafanaUrl = selectors.grafanaUrl(store);
  return [
    {
      key: 'id',
      header: '服务名称',
      render: x => (
        <React.Fragment>
          <CopyableText
            text={x.serviceName}
            component={
              <Link to={`/service/detail?id=${x.serviceName}&namespaceId=${x.namespaceId}&registryId=${x.registryId}`}>
                {x.serviceName}
              </Link>
            }
          />
        </React.Fragment>
      ),
    },
    {
      key: 'status',
      header: '状态',
      render: x => (
        <Text theme={ServiceStatusTheme[x.status]} tooltip={ServiceStatusName[x.status]}>
          {ServiceStatusName[x.status]}
        </Text>
      ),
    },
    {
      key: 'instance',
      header: '实例数',
      render: x => <Text tooltip={x.instanceNum}>{x.instanceNum ?? '-'}</Text>,
    },
    {
      key: 'version',
      header: '版本数',
      render: x => <Text tooltip={x.versionNum}>{x.versionNum ?? '-'}</Text>,
    },
    {
      key: 'operation',
      header: '操作',
      render: x => (
        <>
          <ExternalLink
            style={{ marginRight: 10 }}
            href={`${grafanaUrl}/d/t_cI60n7k/fu-wu-liu-liang-jian-kong?orgId=1&var-namespaceId=${x.namespaceId}&var-serviceName=${x.serviceName}`}
          >
            监控详情
          </ExternalLink>
        </>
      ),
    },
  ];
};
