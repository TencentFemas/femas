import React from 'react';
import { Button, Card, Layout, Stepper, Text } from 'tea-component';
import { Menu as MenuConfig } from '../../config';
import { purify } from 'saga-duck';
import router from '@src/common/util/router';

const { Content } = Layout;

export default purify(function StepperExample() {
  const steps = [
    {
      id: 'registry',
      label: '配置注册中心',
      detail: (
        <Text parent='p'>
          <Text verticalAlign='middle'>请先前往</Text>
          <Button type='link' onClick={() => router.navigate(`/registry`)}>
            注册中心列表
          </Button>

          <Text verticalAlign='middle'>，配置注册中心。</Text>
        </Text>
      ),
    },
    {
      id: 'namespace',
      label: '创建命名空间',
      detail: (
        <Text parent='p'>
          <Text verticalAlign='middle'>前往</Text>
          <Button type='link' onClick={() => router.navigate(`/namespace`)}>
            命名空间列表
          </Button>
          <Text verticalAlign='middle'>，创建命名空间。</Text>
        </Text>
      ),
    },
    {
      id: 'env',
      label: '配置环境变量',
      detail: '需要在客户端完成',
    },
    { id: 'service', label: '部署应用', detail: '需要在客户端完成' },
  ];

  return (
    <>
      <Content.Header title={MenuConfig['guide'].title}></Content.Header>
      <Content.Body>
        <Card full style={{ height: 400 }}>
          <Card.Body>
            <Stepper type='process-vertical' steps={steps} />
          </Card.Body>
        </Card>
      </Content.Body>
    </>
  );
});
