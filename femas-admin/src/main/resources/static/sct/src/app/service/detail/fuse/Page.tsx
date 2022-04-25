import * as React from 'react';
import { DuckCmpProps, memorize, purify } from 'saga-duck';
import Duck from './PageDuck';
import GridPageGrid from '@src/common/duckComponents/GridPageGrid';
import GridPagePagination from '@src/common/duckComponents/GridPagePagination';
import getColumns from './getColumns';
import BasicLayout from '@src/common/components/BaseLayout';
import { Button, Card, Form, Justify, Modal, SearchBox, Table } from 'tea-component';
import { filterable } from 'tea-component/lib/table/addons';
import { FuseRuleItem, ISOLATION_MAP, ISOLATION_TYPE, ISOLATION_TYPE_LIST, Strategy } from './types';
import { useHistory } from 'react-router-dom';

const getHandlers = memorize(({ creators }: Duck, dispatch) => ({
  search: keyword => dispatch(creators.search(keyword)),
  inputKeyword: keyword => dispatch(creators.inputKeyword(keyword)),
  clearKeyword: () => dispatch(creators.search('')),
  reload: () => dispatch(creators.reload()),
  setIsolationLevel: type => dispatch(creators.setIsolationLevel(type)),
}));

// eslint-disable-next-line prettier/prettier
export default purify(function (props: DuckCmpProps<Duck>) {
  const { duck, store, dispatch } = props;
  const { selectors } = duck;
  const history = useHistory();
  const handlers = getHandlers(props);
  const isolationLevel = selectors.isolationLevel(store);
  const [visible, setVisible] = React.useState(false);
  const [curRule, setCurRule] = React.useState<FuseRuleItem>(null);
  const showRuleDeatil = (rule: FuseRuleItem) => {
    setCurRule(rule);
    setVisible(true);
  };
  const columns = React.useMemo(() => getColumns(props, showRuleDeatil), []);
  const composedId = selectors.composedId(store);

  return (
    <BasicLayout type='fregment' selectors={selectors} store={store}>
      <Table.ActionPanel>
        <Justify
          left={
            <Button
              type='primary'
              title={'新建熔断规则'}
              onClick={() =>
                history.push(
                  `/service/fuse-create?registryId=${composedId.registryId}&namespaceId=${composedId.namespaceId}&service=${composedId.serviceName}`,
                )
              }
            >
              {'新建熔断规则'}
            </Button>
          }
          right={
            <>
              <SearchBox
                value={selectors.pendingKeyword(store)}
                placeholder={'请输入关键字搜索'}
                onSearch={handlers.search}
                onChange={handlers.inputKeyword}
                onClear={handlers.clearKeyword}
              />
              <Button title={'重新加载'} icon='refresh' onClick={handlers.reload} />
            </>
          }
        />
      </Table.ActionPanel>
      <Card>
        <GridPageGrid
          duck={duck}
          store={store}
          dispatch={dispatch}
          columns={columns}
          addons={[
            filterable({
              type: 'single',
              column: 'isolationLevel',
              value: isolationLevel,
              onChange: value => handlers.setIsolationLevel(value),
              all: {
                text: '所有',
                value: '',
              },
              options: ISOLATION_TYPE_LIST,
            }),
          ]}
        />
        <GridPagePagination duck={duck} store={store} dispatch={dispatch} />
      </Card>
      <Modal visible={visible} caption='熔断规则' size='l' onClose={() => setVisible(false)}>
        <Modal.Body>
          <Form style={{ maxHeight: 430, overflow: 'auto', display: 'block' }}>
            <Form.Item label='下游服务'>
              <Form.Text>{curRule?.targetServiceName}</Form.Text>
            </Form.Item>
            <Form.Item label='所属命名空间'>
              <Form.Text>{curRule?.targetNamespaceId}</Form.Text>
            </Form.Item>
            <Form.Item label='隔离级别'>
              <Form.Text>{ISOLATION_MAP[curRule?.isolationLevel]}</Form.Text>
            </Form.Item>
            {curRule?.isolationLevel === ISOLATION_TYPE.API && (
              <Form.Item label='熔断策略'>
                {curRule?.strategy.map((strategy, index) => (
                  <Form style={{ minWidth: 530 }} key={index}>
                    <Form.Item label='API'>
                      <Form.Text>{strategy?.api.map(item => `${item.path}(${item.method})`).join(', ')}</Form.Text>
                    </Form.Item>
                    {renderDuplicateItem(strategy, curRule)}
                  </Form>
                ))}
              </Form.Item>
            )}
            {curRule?.isolationLevel !== ISOLATION_TYPE.API && renderDuplicateItem(curRule?.strategy[0], curRule)}
          </Form>
        </Modal.Body>
        <Modal.Footer>
          <Button type='primary' onClick={() => setVisible(false)}>
            确定
          </Button>
        </Modal.Footer>
      </Modal>
    </BasicLayout>
  );
});

function renderDuplicateItem(strategy: Strategy, rule: FuseRuleItem) {
  return (
    <>
      <Form.Item label='最少请求数'>
        <Form.Text>{strategy?.minimumNumberOfCalls}次</Form.Text>
      </Form.Item>
      <Form.Item label='触发条件'>
        {strategy?.failureRateThreshold && (
          <Form.Text>
            失败请求率：当失败请求比率达到{strategy?.failureRateThreshold}
            %时，触发熔断
          </Form.Text>
        )}
        {strategy?.slowCallDurationThreshold && (
          <Form.Text>
            慢请求率：当响应耗时超过{strategy?.slowCallDurationThreshold}
            毫秒的请求比率达到{strategy?.slowCallRateThreshold}%时，触发熔断
          </Form.Text>
        )}
      </Form.Item>
      <Form.Item label='滑动时间窗口'>
        <Form.Text>{strategy?.slidingWindowSize}秒</Form.Text>
      </Form.Item>
      <Form.Item label='开启到半开间隔'>
        <Form.Text>{strategy?.waitDurationInOpenState}秒</Form.Text>
      </Form.Item>
      {rule?.isolationLevel === ISOLATION_TYPE.INSTANCE && (
        <Form.Item label='最大熔断实例比率'>
          <Form.Text>{strategy?.maxEjectionPercent}%</Form.Text>
        </Form.Item>
      )}
    </>
  );
}
