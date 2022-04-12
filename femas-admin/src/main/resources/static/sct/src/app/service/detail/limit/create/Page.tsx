import * as React from 'react';
import { DuckCmpProps, purify } from 'saga-duck';
import Duck from './PageDuck';
import {
  Bubble,
  Button,
  Card,
  Form,
  FormItem,
  FormText,
  InputAdornment,
  Layout,
  Radio,
  RadioGroup,
  Switch,
  Table,
} from 'tea-component';
import { TAB } from '../../types';
import { SYSTEM_TAG } from '../../tagTable/types';
import TagTable from '../../tagTable/TagTable';
import DetailPage from '@src/common/duckComponents/DetailPage';
import FormField from '@src/common/duckComponents/form/Field';
import Input from '@src/common/duckComponents/form/Input';
import {
  descriptionKey,
  durationQuotaKey,
  durationSecondKey,
  LIMIT_RANGE,
  LIMIT_RANGE_MAP,
  nameKey,
  rangeKey,
  rulesKey,
  SOURCE,
  SOURCE_MAP,
  sourceKey,
  statusKey,
} from '../types';
import { nameTipMessage } from '@src/common/types';

const { Content } = Layout;

// 请求来源类型
const sourceOptions = Object.entries(SOURCE_MAP).map(([value, label]) => ({
  label,
  value,
}));

export default purify(
  class Page extends React.Component<DuckCmpProps<Duck>, any> {
    form: any;

    constructor(props) {
      super(props);

      this.onSubmit = this.onSubmit.bind(this);
      this.refCallback = this.refCallback.bind(this);
    }

    get fields() {
      const { duck, dispatch, store } = this.props;
      const {
        ducks: { form },
      } = duck;

      return form
        .getAPI(store, dispatch)
        .getFields([
          nameKey,
          durationSecondKey,
          durationQuotaKey,
          statusKey,
          descriptionKey,
          sourceKey,
          rulesKey,
          rangeKey,
        ]);
    }

    refCallback(form) {
      this.form = form;
    }

    onSubmit(tagTable) {
      const {
        dispatch,
        duck: { creators },
      } = this.props;
      if (!tagTable || (tagTable && tagTable.isValid())) {
        // 提交
        dispatch(creators.submit());
      }
    }

    render() {
      const { duck, store, dispatch } = this.props;
      const { selectors, ducks } = duck;
      const composeId = selectors.composedId(store);
      const value = JSON.parse(JSON.stringify(selectors.data(store)));
      const isAdd = !composeId.ruleId;
      const fields = this.fields;
      const systemTagKeys = [
        SYSTEM_TAG.TSF_LOCAL_SERVICE,
        SYSTEM_TAG.TSF_LOCAL_NAMESPACE_SERVICE,
        SYSTEM_TAG.TSF_LOCAL_IP,
      ];

      const { namespaceId, serviceName, registryId } = selectors.composedId(store);

      let tagTable;

      return (
        <DetailPage
          backHistory
          backRoute={`/service/service-detail?id=${serviceName}&namespaceId=${namespaceId}&registryId=${registryId}&tab=${TAB.Limit}`}
          title={composeId.ruleId ? '编辑限流规则' : '创建限流规则'}
          duck={duck}
          store={store}
          dispatch={dispatch}
          subTitle={serviceName}
        >
          {value && (
            <Content.Body full>
              <Card>
                <Card.Body>
                  <Table.ActionPanel>
                    <Form>
                      <FormField field={fields[nameKey]} label={'规则名'} message={nameTipMessage}>
                        <Input placeholder={'请输入规则名'} field={fields[nameKey]} maxLength={60} />
                      </FormField>
                      <FormItem label={'限流范围'}>
                        {isAdd ? (
                          <RadioGroup
                            value={fields[rangeKey].getValue()}
                            onChange={value => fields[rangeKey].setValue(value)}
                          >
                            {Object.values(LIMIT_RANGE).map((value, index) => (
                              <Radio key={index} name={value} disabled={value === LIMIT_RANGE.CLUSTER}>
                                <Bubble content={value === LIMIT_RANGE.CLUSTER ? '本版本不支持集群限流' : null}>
                                  {LIMIT_RANGE_MAP[value]}
                                </Bubble>
                              </Radio>
                            ))}
                          </RadioGroup>
                        ) : (
                          <FormText>{LIMIT_RANGE_MAP[fields[rangeKey].getValue()]}</FormText>
                        )}
                      </FormItem>
                      <FormItem label={'限流粒度'}>
                        {isAdd ? (
                          <RadioGroup
                            value={fields[sourceKey].getValue()}
                            onChange={value => fields[sourceKey].setValue(value)}
                          >
                            {sourceOptions.map((o, index) => (
                              <Radio
                                key={index}
                                name={o.value}
                                disabled={o.value === SOURCE.ALL && value.hasGlobalRule}
                              >
                                <Bubble
                                  content={
                                    o.value === SOURCE.ALL && value.hasGlobalRule
                                      ? '全局限流规则已经存在（单个服务仅支持1条）'
                                      : null
                                  }
                                >
                                  {o.label}
                                </Bubble>
                              </Radio>
                            ))}
                          </RadioGroup>
                        ) : (
                          <FormText>{SOURCE_MAP[fields[sourceKey].getValue()]}</FormText>
                        )}
                        {fields[sourceKey].getValue() === SOURCE.PART ? (
                          <section style={{ marginTop: 10 }}>
                            <TagTable
                              duck={ducks.tagTable}
                              store={store}
                              dispatch={dispatch}
                              isAllowEmpty={false}
                              ref={o => (tagTable = o)}
                              params={composeId}
                              onChange={value => fields[rulesKey].setValue(value)}
                              list={fields[rulesKey].getValue() || []}
                              systemTags={systemTagKeys}
                            />
                          </section>
                        ) : null}
                      </FormItem>
                      <FormItem label={'限流阈值'}>
                        <Form>
                          <FormField field={fields[durationSecondKey]} label={'单位时间'}>
                            <InputAdornment after='S'>
                              <Input field={fields[durationSecondKey]} type='number' placeholder={'请输入单位时间'} />
                            </InputAdornment>
                          </FormField>
                          <FormField field={fields[durationQuotaKey]} label={'请求数'}>
                            <InputAdornment after={'次'}>
                              <Input field={fields[durationQuotaKey]} type='number' placeholder={'请输入请求数'} />
                            </InputAdornment>
                          </FormField>
                        </Form>
                      </FormItem>
                      <FormItem label={'生效状态'}>
                        <Switch
                          value={!!fields[statusKey].getValue()}
                          onChange={checked => fields[statusKey].setValue(checked)}
                        />
                      </FormItem>
                      <FormField field={fields[descriptionKey]} label={'描述(选填)'}>
                        <Input
                          multiline={true}
                          field={fields[descriptionKey]}
                          maxLength={200}
                          placeholder={'不超过200个字符'}
                        />
                      </FormField>
                    </Form>
                    <Button type='primary' onClick={() => this.onSubmit(tagTable)}>
                      完成
                    </Button>
                  </Table.ActionPanel>
                </Card.Body>
              </Card>
            </Content.Body>
          )}
        </DetailPage>
      );
    }
  },
);
