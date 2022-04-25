import * as React from 'react';
import { DuckCmpProps, purify } from 'saga-duck';
import Duck from './PageDuck';
import { Button, Card, Form, FormItem, Input, Radio, Switch } from 'tea-component';
import TagTable from '../../tagTable/TagTable';
import { SYSTEM_TAG } from '../../tagTable/types';
import DetailPage from '@src/common/duckComponents/DetailPage';
import FormField from '@src/common/duckComponents/form/Field';
import { TAB } from '../../types';
import RadioGroup from '@src/common/duckComponents/form/RadioGroup';
import { AUTH_TYPE, AUTH_TYPE_MAP, TARGET, TARGET_MAP } from '../types';
import { nameTipMessage } from '@src/common/types';

// eslint-disable-next-line prettier/prettier
export default purify(function (props: DuckCmpProps<Duck>) {
  const { duck, store, dispatch } = props;
  const { selectors, ducks, creators } = duck;
  const { form } = ducks;
  const id = selectors.id(store);
  const composedId = selectors.composedId(store);
  const conditionList = selectors.conditionList(store);
  const canSubmit = selectors.canSubmit(store);

  const { status, ruleName, ruleType, target } = form
    .getAPI(store, dispatch)
    .getFields(['status', 'ruleName', 'ruleType', 'target']);

  let tableRef;

  const onSubmit = () => {
    if (tableRef.isValid()) {
      // 提交表单
      dispatch(creators.submit());
    }
  };

  const systemTagKeys = [SYSTEM_TAG.TSF_LOCAL_SERVICE, SYSTEM_TAG.TSF_LOCAL_NAMESPACE_SERVICE, SYSTEM_TAG.TSF_LOCAL_IP];

  const { namespaceId, serviceName } = selectors.composedId(store);

  return (
    <DetailPage
      backHistory
      title={id ? '编辑鉴权规则' : '新建鉴权规则'}
      backRoute={`/service-detail?&id=${serviceName}&namespaceId=${namespaceId}&tab=${TAB.Auth}`}
      duck={duck}
      store={store}
      dispatch={dispatch}
      subTitle={serviceName}
    >
      <Card>
        <Card.Body>
          <Form>
            <FormField field={ruleName} label={'规则名'} message={nameTipMessage}>
              <Input
                placeholder={'请输入规则名'}
                onChange={value => {
                  ruleName.setValue(value);
                  ruleName.setTouched();
                }}
                onBlur={() => {
                  ruleName.setTouched();
                }}
                value={ruleName.getValue()}
                maxLength={60}
              />
            </FormField>
            <FormField field={ruleType} label={'鉴权方式'}>
              <RadioGroup field={ruleType} layout='inline'>
                {Object.values(AUTH_TYPE).map((v, index) => (
                  <Radio name={v} key={index}>
                    {AUTH_TYPE_MAP[v]}
                  </Radio>
                ))}
              </RadioGroup>
            </FormField>
            <FormItem label={'鉴权标签'}>
              <TagTable
                duck={ducks.tagTable}
                store={store}
                dispatch={dispatch}
                ref={instance => (tableRef = instance)}
                isAllowEmpty={false}
                list={conditionList}
                onChange={list => dispatch(creators.setList(list))}
                params={composedId}
                systemTags={systemTagKeys}
              />
            </FormItem>
            <FormField field={target} label={'生效对象'}>
              <RadioGroup field={target} layout='inline'>
                {[TARGET.all].map((v, index) => (
                  <Radio name={v} key={index}>
                    {TARGET_MAP[v]}
                  </Radio>
                ))}
              </RadioGroup>
            </FormField>
            <FormItem label={'生效状态'}>
              <Switch value={status.getValue()} onChange={val => status.setValue(val)} />
            </FormItem>
          </Form>
        </Card.Body>
        <Card.Body>
          <Button type='primary' onClick={onSubmit} disabled={!canSubmit}>
            {'完成'}
          </Button>
          <Button style={{ marginLeft: '10px' }} onClick={() => dispatch(creators.cancel())}>
            {'取消'}
          </Button>
        </Card.Body>
      </Card>
    </DetailPage>
  );
});
