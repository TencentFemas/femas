import * as React from 'react';
import { memorize } from 'saga-duck';
import Duck from './PageDuck';
import { Link } from 'react-router-dom';
import { Bubble, Button, Switch, Text } from 'tea-component';
import { RouteRuleItem } from './types';
import formatDate from '@src/common/util/formatDate';
import { RULE_STATUS, RULE_STATUS_MAP } from '../../types';

export default memorize(({ creators }: Duck, dispatch) => [
  {
    key: 'id',
    header: '规则名',
    render: (x: RouteRuleItem) => {
      return (
        <>
          <Text parent={'p'} overflow tooltip={x.ruleName}>
            {x.ruleName}
          </Text>
        </>
      );
    },
  },
  {
    key: 'content',
    header: '规则内容',
    width: 550,
    render: (x: RouteRuleItem) => (
      <Bubble
        placement='top'
        className='white'
        content={x.routeDesc?.map((d, i) => (
          <Text parent='p' key={i}>
            {d}
          </Text>
        ))}
      >
        <Text parent={'p'} overflow>
          {x.routeDesc}
        </Text>
      </Bubble>
    ),
  },
  {
    key: 'status',
    header: '生效状态',
    render: (x: RouteRuleItem) => (
      <Switch
        value={+x.status === RULE_STATUS.OPEN}
        onChange={checked => {
          dispatch(creators.changeStatus({ ...x, checked }));
        }}
        tooltip={RULE_STATUS_MAP[+x.status]}
      />
    ),
  },
  {
    key: 'createTime',
    header: '创建时间',
    render: (x: RouteRuleItem) => (
      <Text parent={'p'} overflow tooltip={formatDate(x.createTime)}>
        {formatDate(x.updateTime) || '-'}
      </Text>
    ),
  },
  {
    key: 'updateTime',
    header: '生效时间',
    render: (x: RouteRuleItem) => (
      <Text parent={'p'} overflow tooltip={formatDate(x.updateTime)}>
        {formatDate(x.updateTime) || '-'}
      </Text>
    ),
  },
  {
    key: 'operation',
    header: '操作',
    render: (x: RouteRuleItem) => {
      return (
        <>
          <Link
            to={`/service/service-route-create?id=${x.ruleId}&namespaceId=${x.namespaceId}&registryId=${x.registryId}&serviceName=${x.serviceName}`}
          >
            <Button type='link'>编辑</Button>
          </Link>
          <Button
            type='link'
            onClick={() => {
              dispatch(creators.remove(x.ruleId));
            }}
          >
            删除
          </Button>
        </>
      );
    },
  },
]);
