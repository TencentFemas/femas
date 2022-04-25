import React from 'react';
import DetailPage from '@src/common/duckComponents/DetailPage';
import FormField from '@src/common/duckComponents/form/Field';
import { DuckCmpProps } from 'saga-duck';
import { Bubble, Button, Form, Icon, InputAdornment, Text } from 'tea-component';
import CreatePageDuck from './PageDuck';
import SearchableTeaSelect from '@src/common/duckComponents/SearchableTeaSelect';
import Select from '@src/common/duckComponents/form/Select';
import { ServiceItem } from '@src/app/service/types';
import { ISOLATION_TYPE, ISOLATION_TYPE_LIST, Strategy } from '../types';
import { FieldAPI } from '@src/common/ducks/Form';
import CreateForm from './FormDuck';
import Number from '@src/common/duckComponents/form/Number';
import ApiTableSelect from './ApiTableSelect';

export default function CreatePage(props: DuckCmpProps<CreatePageDuck>) {
  const { duck, store, dispatch } = props;
  const { ducks, selectors } = duck;
  const formApi = ducks.form.getAPI(store, dispatch);
  const { targetServiceName, isolationLevel, strategy } = formApi.getFields([
    'targetServiceName',
    'isolationLevel',
    'strategy',
  ]);
  const strategyArrFeild = strategy.asArray();
  const loading = ducks.submit.selectors.loading(store);
  const { serviceName, ruleId } = selectors.composedId(store);
  const handlers = React.useMemo(
    () => ({
      submit: () => dispatch(duck.creators.submit()),
    }),
    [],
  );
  return (
    <DetailPage
      duck={duck}
      store={store}
      dispatch={dispatch}
      backHistory
      backRoute={'/service/detail'}
      title={ruleId ? '编辑熔断规则' : '创建熔断规则'}
      showCard
      subTitle={serviceName}
    >
      <Form>
        <FormField
          field={targetServiceName}
          align='middle'
          label={
            <Text>
              下游服务
              <Bubble content={'熔断规则在上游服上设置，作用于下游服务。'}>
                <Icon type='info' style={{ marginLeft: 2 }} />
              </Bubble>
            </Text>
          }
        >
          <InputAdornment before={'命名空间'}>
            <SearchableTeaSelect
              duck={ducks.namespace}
              dispatch={dispatch}
              store={store}
              searchable={false}
              toOption={o => {
                return {
                  text: o.name,
                  tooltip: o.name,
                  value: o.namespaceId,
                };
              }}
              boxSizeSync
            />
          </InputAdornment>
          <SearchableTeaSelect
            duck={ducks.service}
            dispatch={dispatch}
            store={store}
            searchable={false}
            toOption={(o: ServiceItem) => {
              return {
                text: o.serviceName,
                tooltip: o.serviceName,
                value: o.serviceName,
              };
            }}
            boxSizeSync
          />
        </FormField>
        <FormField
          field={isolationLevel}
          label={
            <Text>
              隔离级别
              <Bubble content='根据不同隔离级别进行指标统计，达到阈值后隔离该级别的对象。'>
                <Icon type='info' style={{ marginLeft: 2 }} />
              </Bubble>
            </Text>
          }
        >
          <Select type='simulate' appearance='button' size='m' field={isolationLevel} options={ISOLATION_TYPE_LIST} />
        </FormField>
        {isolationLevel.getValue() !== ISOLATION_TYPE.API &&
          renderStrategy(
            strategyArrFeild.get(0),
            isolationLevel.getValue(),
            ducks.namespace.selectors.id(store),
            ducks.service.selectors.id(store),
          )}
        {isolationLevel.getValue() === ISOLATION_TYPE.API && (
          <Form.Item label='熔断策略'>
            {strategyArrFeild.getValue().map((item, index) => (
              <Form key={index} style={{ position: 'relative' }}>
                {strategyArrFeild.asArray().length > 1 && (
                  <Icon
                    type='close'
                    onClick={() => {
                      strategyArrFeild.asArray().remove(index);
                    }}
                    style={{
                      position: 'absolute',
                      right: 10,
                      cursor: 'pointer',
                      zIndex: 1,
                    }}
                  />
                )}
                {renderStrategy(
                  strategyArrFeild.get(index),
                  isolationLevel.getValue(),
                  ducks.namespace.selectors.id(store),
                  ducks.service.selectors.id(store),
                )}
              </Form>
            ))}
            <Text reset parent='p' style={{ marginTop: 10 }}>
              <Button
                type='link'
                onClick={() => {
                  strategyArrFeild.asArray().push(duck.defaultStrategy);
                }}
              >
                {'新增熔断策略'}
              </Button>
            </Text>
          </Form.Item>
        )}
      </Form>
      <Form.Action>
        <Button type='primary' onClick={handlers.submit} loading={loading}>
          完成
        </Button>
      </Form.Action>
    </DetailPage>
  );
}

function renderStrategy(
  strategy: FieldAPI<Strategy, CreateForm>,
  isolationLevel: ISOLATION_TYPE,
  namespaceId: string,
  serviceName: string,
) {
  const {
    api,
    slidingWindowSize,
    minimumNumberOfCalls,
    slowCallDurationThreshold,
    slowCallRateThreshold,
    failureRateThreshold,
    maxEjectionPercent,
    waitDurationInOpenState,
  } = strategy.getFields([
    'api',
    'slidingWindowSize',
    'minimumNumberOfCalls',
    'slowCallDurationThreshold',
    'slowCallRateThreshold',
    'failureRateThreshold',
    'maxEjectionPercent',
    'waitDurationInOpenState',
  ]);

  return (
    <>
      {isolationLevel === ISOLATION_TYPE.API && (
        <FormField field={api} label='API' showStatusIcon={false} align='middle'>
          <ApiTableSelect
            namespaceId={namespaceId}
            serviceName={serviceName}
            value={api.getValue()}
            onChange={value => api.setValue(value)}
          />
        </FormField>
      )}
      <FormField
        field={slidingWindowSize}
        label={
          <Text>
            滑动时间窗口
            <Bubble content='用于统计熔断器关闭时的请求结果。'>
              <Icon type='info' style={{ marginLeft: 2 }} />
            </Bubble>
          </Text>
        }
      >
        <InputAdornment after={'秒'}>
          <Number field={slidingWindowSize} min={1} max={9999} style={{ width: 80 }} />
        </InputAdornment>
      </FormField>
      <FormField
        field={minimumNumberOfCalls}
        label={
          <Text>
            最少请求数
            <Bubble content='配置熔断器可以计算错误率之前的最小请求数（每个滑动时间窗口）。'>
              <Icon type='info' style={{ marginLeft: 2 }} />
            </Bubble>
          </Text>
        }
      >
        <InputAdornment after={'次'}>
          <Number field={minimumNumberOfCalls} min={1} style={{ width: 80 }} />
        </InputAdornment>
      </FormField>
      <Form.Item label='触发条件'>
        <Text parent='p' reset theme='label'>
          满足以下<strong>任一</strong>条件触发熔断：
        </Text>
        <Text parent='div' reset style={{ marginTop: 10 }}>
          <section>
            慢请求率：当响应耗时超过
            <InputAdornment after={'毫秒'} style={{ margin: '0 10px' }}>
              <Number field={slowCallDurationThreshold} maxLength={3} min={1} max={9999999} style={{ width: 80 }} />
            </InputAdornment>
            的请求比率达到
            <InputAdornment after='%' style={{ margin: '0 10px' }}>
              <Number field={slowCallRateThreshold} maxLength={3} min={1} max={100} style={{ width: 80 }} />
            </InputAdornment>
            时，触发熔断
          </section>
          <section style={{ marginTop: 10 }}>
            失败请求率：达到
            <InputAdornment after='%' style={{ margin: '0 10px' }}>
              <Number
                field={failureRateThreshold.map(
                  v => v,
                  v => Math.floor(v),
                )}
                maxLength={3}
                min={1}
                max={100}
                style={{ width: 80 }}
              />
            </InputAdornment>
            时，触发熔断
          </section>
        </Text>
        );
      </Form.Item>
      {isolationLevel === ISOLATION_TYPE.INSTANCE && (
        <FormField
          field={maxEjectionPercent}
          label={
            <Text>
              最大熔断实例比率
              <Bubble content='最大熔断实例个数百分比'>
                <Icon type='info' style={{ marginLeft: 2 }} />
              </Bubble>
            </Text>
          }
        >
          <InputAdornment after='%'>
            <Number field={maxEjectionPercent} maxLength={3} min={1} max={100} style={{ width: 80 }} />
          </InputAdornment>
        </FormField>
      )}
      <FormField
        field={waitDurationInOpenState}
        label={
          <Text>
            开启到半开间隔
            <Bubble content={'熔断器打开到半开状态的时间。'}>
              <Icon type='info' style={{ marginLeft: 2 }} />
            </Bubble>
          </Text>
        }
      >
        <InputAdornment after='秒'>
          <Number field={waitDurationInOpenState} min={1} max={9999} style={{ width: 80 }} />
        </InputAdornment>
      </FormField>
    </>
  );
}

export interface ApiList {
  path: string;
  method: string;
  value: string;
}
