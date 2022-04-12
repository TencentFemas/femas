import * as React from 'react';
import { memorize } from 'saga-duck';
import { LIMIT_RANGE, LIMIT_RANGE_MAP, LimitRuleItem, SOURCE, SOURCE_MAP } from './types';
import Duck from './PageDuck';
import { Link } from 'react-router-dom';
import { Button, Switch, Text } from 'tea-component';
import formatDate from '@src/common/util/formatDate';
import { RULE_STATUS, RULE_STATUS_MAP } from '../../types';

export default memorize(({ creators }: Duck, dispatch) => [
  {
    key: 'name',
    header: '规则名',
    render: (x: LimitRuleItem) => {
      return (
        <Text parent={'p'} overflow tooltip={x.ruleName}>
          {x.ruleName}
        </Text>
      );
    },
  },
  {
    key: 'durationQuota',
    header: '限流请求数/单位时间',
    render: (x: LimitRuleItem) => {
      const content = `${x.totalQuota} 次/ ${x.duration} 秒`;
      return (
        <Text parent={'p'} overflow tooltip={content}>
          {content}
        </Text>
      );
    },
  },
  {
    key: 'range',
    header: '限流范围',
    render: () => (
      <Text overflow tooltip={LIMIT_RANGE_MAP[LIMIT_RANGE.SINGLE]}>
        {LIMIT_RANGE_MAP[LIMIT_RANGE.SINGLE]}
      </Text>
    ),
  },
  {
    key: 'type',
    header: '限流粒度',
    width: 350,
    render: (x: LimitRuleItem) => {
      const content = x.type !== SOURCE.ALL && x.dimensionsDesc.length ? x.dimensionsDesc : SOURCE_MAP[x.type];
      return (
        <Text overflow tooltip={content}>
          {content}
        </Text>
      );
    },
  },
  {
    key: 'status',
    header: '生效状态',
    render: (x: LimitRuleItem) => (
      <Switch
        value={+x.status === RULE_STATUS.OPEN}
        tooltip={RULE_STATUS_MAP[+x.status]}
        onChange={checked => dispatch(creators.changeStatus({ ...x, status: checked }))}
      />
    ),
  },
  {
    key: 'updateTime',
    header: '生效时间',
    render: (x: LimitRuleItem) => (
      <Text parent={'p'} overflow tooltip={formatDate(x.updateTime)}>
        {formatDate(x.updateTime) || '-'}
      </Text>
    ),
  },
  {
    key: 'description',
    header: '描述',
    render: (x: LimitRuleItem) => (
      <Text parent={'p'} overflow tooltip={x.desc}>
        {x.desc || '-'}
      </Text>
    ),
  },
  {
    key: 'operation',
    header: '操作',
    render: (x: LimitRuleItem) => {
      return (
        <>
          <Link
            to={`/service/service-limit-create?id=${x.ruleId}&namespaceId=${x.namespaceId}&registryId=${x.registryId}&serviceName=${x.serviceName}`}
          >
            <Button type='link'>编辑</Button>
          </Link>
          <Button
            type='link'
            onClick={() => {
              dispatch(creators.remove(x));
            }}
          >
            {'删除'}
          </Button>
        </>
      );
    },
  },
]);
