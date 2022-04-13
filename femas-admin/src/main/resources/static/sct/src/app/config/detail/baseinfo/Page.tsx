import formatDate from '@src/common/util/formatDate';
import React from 'react';
import { DuckCmpProps, memorize } from 'saga-duck';
import { Bubble, Button, Card, Form, LoadingTip, Tag } from 'tea-component';
import PageDuck from './PageDuck';
import router from '@src/common/util/router';
import { TAB } from '../types';

export const getHandlers = memorize(({ creators }: PageDuck, dispatch) => ({
  editDesc: () => dispatch(creators.editDesc()),
}));

export default function BaseInfo(props: DuckCmpProps<PageDuck>) {
  const { duck, store } = props;
  const { selector, selectors } = duck;
  const { loading } = selector(store);
  const handlers = getHandlers(props);
  if (loading) return <LoadingTip />;
  const { configId, namespaceId } = selectors.composedId(store);
  const data = selectors.configData(store);
  if (!data) return <noscript />;
  const tag = data.systemTag ? JSON.parse(data.systemTag) : null;
  return (
    <>
      <Card>
        <Card.Body>
          <Form>
            <Form.Item label={'配置名称'}>
              <Form.Text>{data.configName}</Form.Text>
            </Form.Item>
            <Form.Item label={'命名空间'}>
              <Form.Text>
                {data.namespaceName} ({data.namespaceId})
              </Form.Text>
            </Form.Item>
            <Form.Item label={'所属服务'}>
              <Form.Text>{data.serviceName || '-'}</Form.Text>
            </Form.Item>
            <Form.Item label={'版本数'}>
              <Form.Text>
                <Button
                  type='link'
                  onClick={() =>
                    router.navigate(`/config/detail?id=${configId}&namespaceId=${namespaceId}&tab=${TAB.Version}`)
                  }
                >
                  {data.versionCount}
                </Button>
              </Form.Text>
            </Form.Item>
            <Form.Item label={'系统标签'} align='middle'>
              <Form.Text>
                {tag
                  ? Object.keys(tag)?.map(key => (
                      <Bubble key={key} content={`${key} : ${tag[key]}`}>
                        <Tag style={{ marginRight: 4 }} key={key}>
                          {key}: {tag[key]}
                        </Tag>
                      </Bubble>
                    ))
                  : '-'}
              </Form.Text>
            </Form.Item>
            <Form.Item label={'创建时间'}>
              <Form.Text>{formatDate(data.createTime)}</Form.Text>
            </Form.Item>
            <Form.Item label={'最新发布时间'}>
              <Form.Text>{formatDate(data.releaseTime) || '-'}</Form.Text>
            </Form.Item>
            <Form.Item label={'描述'}>
              <Form.Text>
                {data.configDesc || '-'}
                <Button type='icon' icon='pencil' onClick={() => handlers.editDesc()} />
              </Form.Text>
            </Form.Item>
          </Form>
        </Card.Body>
      </Card>
    </>
  );
}
