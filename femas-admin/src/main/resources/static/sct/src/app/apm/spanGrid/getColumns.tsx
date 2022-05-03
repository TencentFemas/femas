import * as React from 'react';
import { Instance, TRACE_STATUS, TRACE_STATUS_TEXT_THEME } from '../requestDetail/types';
import Duck from './PageDuck';
import { Button, Copy, TableColumn, Text } from 'tea-component';
import { findInfoByTag } from '@src/app/apm/utils';
import { TagMap } from '@src/app/apm/types';
import { Props } from './Page';
import CopyableText from '@src/common/components/CopyableText';
import formatDate from '@src/common/util/formatDate';

export default props => {
  const { duck, dispatch, hideApiHref } = props as Props;
  const { creators } = duck as Duck;
  return [
    {
      key: 'time',
      header: '调用时间',
      render: (x: Instance) => {
        return (
          <Text overflow tooltip={formatDate(+x.startTime, 'yyyy-MM-dd hh:mm:ss.SSS')}>
            {formatDate(+x.startTime, 'yyyy-MM-dd hh:mm:ss.SSS')}
          </Text>
        );
      },
    },
    {
      key: 'traceId',
      header: 'Trace ID/ Span ID',
      render: (x: Instance) => (
        <>
          <Text parent='p'>
            <Button
              style={{
                marginRight: 0,
                maxWidth: 'calc(100% - 16px)',
                textAlign: 'left',
              }}
              type='link'
              onClick={() => {
                dispatch(creators.inspect(x));
              }}
            >
              <Text overflow tooltip={x.traceId}>
                {x.traceId}
              </Text>
            </Button>
            <Text className={'hover-icon'}>
              <Copy text={x.traceId} />
            </Text>
          </Text>
          <CopyableText text={x.spanId} />
        </>
      ),
    },
    {
      key: 'interface',
      header: '接口名',
      render: (x: Instance) => {
        const api = findInfoByTag(x, TagMap.localOperation);
        return api ? (
          hideApiHref ? (
            <Text overflow tooltip={api}>
              {api}
            </Text>
          ) : (
            // <a
            //   target="_blank"
            //   rel="noopener noreferrer"
            //   href={`/tsw/service-detail?rid=${
            //     x.regionId
            //   }&id=${encodeURIComponent(service)}&tab=${TAB.API}&startTime=${
            //     x.startTimestamp
            //   }&endTime=${x.endTimestamp}&curApi=${encodeURIComponent(api)}`}
            //   style={{ display: "block" }}
            // >
            <Text overflow tooltip={api}>
              {api}
            </Text>
            // </a>
          )
        ) : (
          '-'
        );
      },
    },
    {
      key: 'duration',
      header: '耗时（ms）',
      width: 120,
      render: (x: Instance) => (
        <Text overflow tooltip={x.duration.toFixed(2)}>
          {x.duration.toFixed(2)}
        </Text>
      ),
    },
    {
      key: 'status',
      header: '调用状态',
      width: 100,
      render: (x: Instance) => {
        const status = findInfoByTag(x, TagMap.status) === 'true';
        return (
          <Text overflow tooltip={TRACE_STATUS[+status]} theme={TRACE_STATUS_TEXT_THEME[+status]}>
            {TRACE_STATUS[+status]}
          </Text>
        );
      },
    },
    {
      key: 'callType',
      header: '调用类型/状态码',
      render: (x: Instance) => {
        const type = findInfoByTag(x, TagMap.type);
        const httpCode = findInfoByTag(x, TagMap.httpStatus);
        return (
          <>
            <Text parent='p'>
              <Text overflow tooltip={type}>
                {type || '-'}
              </Text>
            </Text>
            <Text overflow tooltip={httpCode}>
              {httpCode || '-'}
            </Text>
          </>
        );
      },
    },
    {
      key: 'action',
      header: '操作',
      width: 100,
      render: (x: Instance) => {
        return (
          <Button type='link' onClick={() => dispatch(creators.showSpanDetail(x))}>
            查看详情
          </Button>
        );
      },
    },
  ] as TableColumn<Instance>[];
};
