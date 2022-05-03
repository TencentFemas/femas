import * as React from 'react';
import { Column } from '../../../../common/ducks/GridPage';
import { DuckCmpProps } from 'saga-duck';
import SevicePageDuck from './PageDuck';
import { Text } from 'tea-component';
import { EventItem } from './types';
import formatDate from '@src/common/util/formatDate';

export default ({ duck: { selectors }, store }: DuckCmpProps<SevicePageDuck>): Column<EventItem>[] => {
  const eventTypeList = selectors.eventTypeList(store);
  return [
    {
      key: 'id',
      header: '影响实例ID',
      render: x => (
        <>
          <Text tooltip={x.instanceId} overflow>
            {x.instanceId}
          </Text>
        </>
      ),
    },
    {
      key: 'eventType',
      header: '事件类型',
      render: x => {
        const type = eventTypeList?.find(d => d.value === x.eventType)?.text;
        return (
          <>
            <Text tooltip={type} overflow>
              {type || '-'}
            </Text>
          </>
        );
      },
    },
    {
      key: 'detail',
      header: '对象详情',
      width: 400,
      render: x => (
        <>
          <Text tooltip={x.detail} overflow>
            {x.detail}
          </Text>
        </>
      ),
    },
    {
      key: 'quality',
      header: '指标值',
      render: x => (
        <Text parent={'p'} overflow tooltip={x.quality}>
          {x.quality || '-'}
        </Text>
      ),
    },
    {
      key: 'createTime',
      header: '创建时间',
      render: x => (
        <Text parent={'p'} overflow tooltip={formatDate(x.createTime)}>
          {formatDate(x.createTime) || '-'}
        </Text>
      ),
    },
  ];
};
