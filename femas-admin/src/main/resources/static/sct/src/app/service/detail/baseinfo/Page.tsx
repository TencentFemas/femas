import GridPageGrid from '@src/common/duckComponents/GridPageGrid';
import GridPagePagination from '@src/common/duckComponents/GridPagePagination';
import React from 'react';
import { DuckCmpProps } from 'saga-duck';
import { Card, Form, Text } from 'tea-component';
import { SELECT_ALL, ServiceStatus, ServiceStatusName, ServiceStatusTheme } from '../../types';
import BaseInfoDuck from './PageDuck';
import getColumns from './getColumns';
import { filterable } from 'tea-component/lib/table/addons';

export default function BaseInfo(props: DuckCmpProps<BaseInfoDuck>) {
  const { duck, store, dispatch } = props;
  const { selectors, creators } = duck;
  const columns = React.useMemo(() => getColumns(), []);
  const data = selectors.serviceData(store);
  const status = selectors.status(store);
  const serviceVersion = selectors.serviceVersion(store);

  const handlers = React.useMemo(
    () => ({
      setStatus: value => dispatch(creators.setStatus(value)),
      setVersion: value => dispatch(creators.setVersion(value)),
    }),
    [],
  );

  return (
    <>
      <Card>
        <Card.Body title='服务信息'>
          <Form>
            <Form.Item label='名称'>
              <Form.Text>{data.serviceName}</Form.Text>
            </Form.Item>
            <Form.Item label='所属命名空间'>
              <Form.Text>
                {data.namespaceName}({data.namespaceId})
              </Form.Text>
            </Form.Item>
            <Form.Item label='状态'>
              <Form.Text>
                <Text theme={ServiceStatusTheme[data.status]}>{ServiceStatusName[data.status] || '-'}</Text>
              </Form.Text>
            </Form.Item>
            <Form.Item label='总实例数'>
              <Form.Text>{data.instanceNum ?? '-'}</Form.Text>
            </Form.Item>
            <Form.Item label='在线实例数'>
              <Form.Text>{data.liveInstanceCount ?? '-'}</Form.Text>
            </Form.Item>
            <Form.Item label='版本数'>
              <Form.Text>{data.versionNum ?? '-'}</Form.Text>
            </Form.Item>
          </Form>
        </Card.Body>
      </Card>
      <Card>
        <Card.Body title={'实例列表'}>
          <GridPageGrid
            bordered
            duck={duck}
            dispatch={dispatch}
            store={store}
            columns={columns}
            addons={[
              filterable({
                all: SELECT_ALL,
                type: 'single',
                column: 'status',
                value: status,
                onChange: value => handlers.setStatus(value),
                options: [ServiceStatus.UP, ServiceStatus.DOWN].map(value => ({
                  text: ServiceStatusName[value],
                  value,
                })),
              }),
              filterable({
                all: SELECT_ALL,
                type: 'single',
                column: 'serviceVersion',
                value: serviceVersion,
                onChange: value => handlers.setVersion(value),
                options: data.versions?.map(value => ({ text: value, value })) || [],
              }),
            ]}
          />
          <GridPagePagination duck={duck} dispatch={dispatch} store={store} />
        </Card.Body>
      </Card>
    </>
  );
}
