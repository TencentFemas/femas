import * as React from 'react';
import { Column } from '@src/common/ducks/GridPage';
import { RELEASE_STATUS, RELEASE_STATUS_NAME, RELEASE_STATUS_THEME, VersionItem } from '../../types';
import { DuckCmpProps } from 'saga-duck';
import SevicePageDuck from './PageDuck';
import { Button, Text } from 'tea-component';
import formatDate from '@src/common/util/formatDate';
import { EDIT_TYPE } from '../../operations/create/CreateDuck';

export default ({ duck: { creators }, dispatch }: DuckCmpProps<SevicePageDuck>): Column<VersionItem>[] => [
  {
    key: 'configVersion',
    header: '版本号',
    render: x => (
      <Text overflow tooltip={x.configVersion}>
        {x.configVersion}
      </Text>
    ),
  },
  {
    key: 'status',
    header: '发布状态',
    render: x => (
      <Text overflow theme={RELEASE_STATUS_THEME[x.releaseStatus]} tooltip={RELEASE_STATUS_NAME[x.releaseStatus]}>
        {RELEASE_STATUS_NAME[x.releaseStatus]}
      </Text>
    ),
  },
  {
    key: 'createTime',
    header: '创建时间',
    render: x => (
      <Text overflow tooltip={formatDate(x.createTime)}>
        {formatDate(x.createTime)}
      </Text>
    ),
  },
  {
    key: 'releaseTime',
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
        <Button
          type='link'
          onClick={() =>
            dispatch(
              creators.configureVersion({
                item: x,
                editType: EDIT_TYPE.generate,
              }),
            )
          }
        >
          生成新版本
        </Button>
        <Button
          type='link'
          onClick={() =>
            dispatch(
              creators.configureVersion({
                item: x,
                editType: EDIT_TYPE.release,
              }),
            )
          }
        >
          发布
        </Button>
        <Button
          type='link'
          onClick={() => dispatch(creators.delete([x.configVersionId]))}
          disabled={x.releaseStatus === RELEASE_STATUS.doing}
          tooltip={x.releaseStatus === RELEASE_STATUS.doing && '生效中的版本不允许被删除'}
        >
          删除
        </Button>
      </>
    ),
  },
];
