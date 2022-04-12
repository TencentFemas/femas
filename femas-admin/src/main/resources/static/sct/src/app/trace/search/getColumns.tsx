import * as React from 'react';
import { Instance, TRACE_STATUS, TRACE_STATUS_TEXT_THEME } from '@src/app/apm/requestDetail/types';
import { Button, Text } from 'tea-component';
import formatDate from '@src/common/util/formatDate';
import CopyableText from '@src/common/components/CopyableText';
import { TAB } from '@src/app/service/detail/types';

export default () => [
  {
    key: 'id',
    header: 'Trace ID',
    render: x => <CopyableText text={x.traceId} />,
  },
  {
    key: 'service',
    header: '链路入口服务',
    render: (x: Instance) => {
      return x.entryService ? (
        <Button
          type='link'
          onClick={() =>
            window.open(`/service/detail?id=${x.entryService}&namespaceId=${x.namespaceId}&registryId=${x.registryId}`)
          }
          style={{ width: '100%', textAlign: 'left' }}
        >
          <Text overflow tooltip={x.entryService}>
            {x.entryService}
          </Text>
        </Button>
      ) : (
        '-'
      );
    },
  },
  {
    key: 'timestamp',
    header: '产生时间',
    render: (x: Instance) => (
      <Text overflow tooltip={formatDate(+x.start, 'yyyy-MM-dd hh:mm:ss.SSS')}>
        {formatDate(+x.start, 'yyyy-MM-dd hh:mm:ss.SSS')}
      </Text>
    ),
  },
  {
    key: 'interface',
    header: '链路入口接口',
    render: (x: Instance) => {
      const api = x.endpointNames?.[0] || '';
      return api && x.entryService ? (
        <Button
          type='link'
          onClick={() =>
            window.open(
              `/service/detail?id=${x.entryService}&namespaceId=${x.namespaceId}&registryId=${x.registryId}&tab=${TAB.Api}`,
            )
          }
          style={{ width: '100%', textAlign: 'left' }}
        >
          <Text overflow tooltip={api}>
            {api}
          </Text>
        </Button>
      ) : (
        <Text overflow tooltip={api}>
          {api || '-'}
        </Text>
      );
    },
  },
  {
    key: 'status',
    header: '状态',
    render: (x: Instance) => {
      const status = x.isError ?? false;
      return (
        <Text overflow tooltip={TRACE_STATUS[+status]} theme={TRACE_STATUS_TEXT_THEME[+status]}>
          {TRACE_STATUS[+status]}
        </Text>
      );
    },
  },
  {
    key: 'duration',
    header: '耗时（ms）',
    render: (x: Instance) => (
      <Text overflow tooltip={x.duration.toFixed(2)}>
        {x.duration.toFixed(2)}
      </Text>
    ),
  },
];
