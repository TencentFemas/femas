import CodeMirrorBox from '@src/common/components/CodeMirrorBox';
import formatDate from '@src/common/util/formatDate';
import React from 'react';
import { DuckCmpProps } from 'saga-duck';
import { Card, Col, Form, LoadingTip, Row, Table, Text } from 'tea-component';
import { autotip } from 'tea-component/lib/table/addons';
import {
  ClusterType,
  ClusterTypeMap,
  K8S_NATIVE_TYPE,
  K8S_NATIVE_TYPE_NAME,
  RegistryStatus,
  RegistryStatusMap,
} from '../../types';
import BaseInfoDuck from './PageDuck';

export default function BaseInfo(props: DuckCmpProps<BaseInfoDuck>) {
  const { duck, store } = props;
  const { selector } = duck;
  const { loading, data } = selector(store);
  if (loading) return <LoadingTip />;
  if (!data) return <noscript />;
  const { config: registryInfo, clusterServers } = data;
  return (
    <>
      <Card>
        <Card.Body title='服务信息'>
          <Form layout='inline'>
            <Row>
              <Col>
                <Form.Item label={'名称'}>
                  <Form.Text>
                    {registryInfo.registryName}({registryInfo.registryId})
                  </Form.Text>
                </Form.Item>
                <Form.Item label={'注册中心类型'}>
                  <Form.Text>{ClusterTypeMap[registryInfo.registryType]}</Form.Text>
                </Form.Item>
                <Form.Item label={'运行状态'}>
                  <Form.Text>
                    <Text theme={registryInfo.status === RegistryStatus.Nomarl ? 'success' : 'danger'}>
                      {RegistryStatusMap[registryInfo.status]}
                    </Text>
                  </Form.Text>
                </Form.Item>
                {registryInfo.registryType !== ClusterType.K8s && (
                  <Form.Item label={'集群地址'}>
                    <Form.Text>{registryInfo.registryCluster}</Form.Text>
                  </Form.Item>
                )}
                {registryInfo.certificateType && (
                  <>
                    <Form.Item label={'认证方式'}>
                      <Form.Text>{K8S_NATIVE_TYPE_NAME[registryInfo.certificateType]}</Form.Text>
                    </Form.Item>
                    {registryInfo.certificateType === K8S_NATIVE_TYPE.kubeconfig && (
                      <Form.Item label='kubeconfig'>
                        <CodeMirrorBox
                          height={300}
                          width={650}
                          value={registryInfo.kubeConfig}
                          options={{
                            readOnly: true,
                          }}
                        />
                      </Form.Item>
                    )}
                    {registryInfo.certificateType === K8S_NATIVE_TYPE.serviceAccount && (
                      <>
                        <Form.Item label={'API Server地址'}>
                          <Form.Text>{registryInfo.apiServerAddr || '-'}</Form.Text>
                        </Form.Item>
                        <Form.Item label={'Secret'}>
                          <Form.Text>{registryInfo.secret}</Form.Text>
                        </Form.Item>
                      </>
                    )}
                  </>
                )}
              </Col>
            </Row>
          </Form>
        </Card.Body>
      </Card>
      <Card>
        <Card.Body title={'实例'}>
          <Table
            bordered
            columns={[
              {
                key: 'serverAddr',
                header: '实例',
                render: x => x.serverAddr,
              },
              {
                key: 'state',
                header: '状态',
                render: x => x.state,
              },
              {
                key: 'clusterRole',
                header: '角色',
                render: x => x.clusterRole,
              },
              {
                key: 'lastRefreshTime',
                header: '刷新时间',
                render: x => formatDate(x.lastRefreshTime),
              },
            ]}
            recordKey='serverAddr'
            records={clusterServers}
            addons={[
              autotip({
                emptyText: '暂无数据',
              }),
            ]}
          />
        </Card.Body>
      </Card>
    </>
  );
}
