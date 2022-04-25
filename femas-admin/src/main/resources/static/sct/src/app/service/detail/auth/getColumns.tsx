import * as React from 'react';
import { memorize } from 'saga-duck';
import Duck from './PageDuck';
import { Button, Switch, Text } from 'tea-component';
import { Link } from 'react-router-dom';
import formatDate from '@src/common/util/formatDate';
import { AUTH_TYPE_MAP, AuthItem, TARGET_MAP } from './types';
import { Column } from '@src/common/ducks/GridPage';
import { RULE_STATUS, RULE_STATUS_MAP } from '../../types';

export default memorize(
  ({ creators }: Duck, dispatch) =>
    [
      {
        key: 'name',
        header: '规则名',
        // width: 100,
        render: x => {
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
        key: 'ruleType',
        header: '规则类型',
        render: x => (
          <Text parent={'p'} overflow tooltip={AUTH_TYPE_MAP[x.ruleType]}>
            {AUTH_TYPE_MAP[x.ruleType] || '-'}
          </Text>
        ),
      },
      {
        key: 'status',
        header: '生效状态',
        render: x => {
          return (
            <Switch
              value={+x.isEnabled === RULE_STATUS.OPEN}
              onChange={checked => dispatch(creators.setStatus({ ...x, status: checked }))}
              tooltip={RULE_STATUS_MAP[+x.isEnabled]}
            />
          );
        },
      },
      {
        key: 'description',
        header: '规则描述',
        width: 400,
        render: x => (
          <Text parent={'p'} overflow tooltip={x.description}>
            {x.description}
          </Text>
        ),
      },
      {
        key: 'target',
        header: '生效对象',
        render: x => (
          <Text parent={'p'} overflow tooltip={TARGET_MAP[x.target]}>
            {TARGET_MAP[x.target] || '-'}
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
      {
        key: 'availableTime',
        header: '生效时间',
        render: x => (
          <Text parent={'p'} overflow tooltip={formatDate(x.availableTime)}>
            {formatDate(x.availableTime) || '-'}
          </Text>
        ),
      },
      {
        key: 'operation',
        header: '操作',
        render: x => {
          return (
            <>
              <Link
                to={`/service/service-auth-create?id=${x.ruleId}&namespaceId=${x.namespaceId}&registryId=${x.registryId}&serviceName=${x.serviceName}`}
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
    ] as Column<AuthItem>[],
);
