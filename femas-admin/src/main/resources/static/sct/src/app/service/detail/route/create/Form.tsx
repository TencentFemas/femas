import * as React from 'react';
import TagTable from '../../tagTable/TagTable';
import TargetGrid from './TargetGrid';
import { SYSTEM_TAG } from '../../tagTable/types';
import { Button, Card, Form, FormItem, Icon, notification, Switch } from 'tea-component';
import { RouteRule } from '../types';
import Input from '@src/common/duckComponents/form/Input';
import SortList from '@src/common/components/SortList';
import FormField from '@src/common/duckComponents/form/Field';
import { pingRuleContent } from '../../utils';
import { nameTipMessage } from '@src/common/types';

let systemTagOptions = [SYSTEM_TAG.TSF_LOCAL_SERVICE, SYSTEM_TAG.TSF_LOCAL_NAMESPACE_SERVICE, SYSTEM_TAG.TSF_LOCAL_IP];

export default class CreateForm extends React.Component<any, any> {
  constructor(props) {
    super(props);
    const self = this as any;
    self.ruleRefs = {};
    self.state = {
      sortMode: false,
    };
    this.setFieldValue = this.setFieldValue.bind(this);
  }

  get fields() {
    const { duck, store, dispatch } = this.props;
    return duck.getAPI(store, dispatch).getFields(['ruleName', 'sortMode', 'tagRules', 'status']);
  }

  format(tagRules) {
    const res: Array<RouteRule> = [];

    for (const index in tagRules) {
      const tagRule: RouteRule = {
        destTag: [],
        tags: [],
      };
      const { tags, destTag } = tagRules[index];

      for (const item of tags) {
        tagRule.tags.push({
          tagType: item.tagType,
          tagField: item.tagField,
          tagOperator: item.tagOperator,
          tagValue: item.tagValue,
        });
      }

      for (const target of destTag) {
        tagRule.destTag.push(target);
      }

      res.push(tagRule);
    }

    return res;
  }

  isValid() {
    const self = this as any;
    const { tagRules } = this.fields;
    const rules = tagRules.getValue();
    let valid = true;
    for (const key in rules) {
      if (self.ruleRefs[`TagTable_${key}`] && !self.ruleRefs[`TagTable_${key}`].isValid()) valid = false;
      if (self.ruleRefs[`TargetGrid_${key}`] && !self.ruleRefs[`TargetGrid_${key}`].isValid()) valid = false;
    }
    return valid;
  }

  addTagRule() {
    const { tagRules } = this.fields;
    const rules = tagRules.getValue() || [];
    const newState = rules.slice();
    newState.push({});
    tagRules.setValue(newState);
  }

  removeTagRule(index) {
    const { tagRules } = this.fields;
    const rules = tagRules.getValue();
    const newState = rules.slice() || [];
    newState.splice(index, 1);
    tagRules.setValue(newState);
  }

  toggleSummery(index, status) {
    const self = this as any;
    const { tagRules } = this.fields;
    const rules = tagRules.getValue() || [];
    const newState = rules.slice() || [];
    if (!newState[index].summery) {
      const tagTableValid = self.ruleRefs[`TagTable_${index}`].isValid();
      const targetGridValid = self.ruleRefs[`TargetGrid_${index}`].isValid();
      if (!tagTableValid || !targetGridValid) {
        notification.error({ description: '请完善信息后隐藏' });
        return;
      }
    }
    newState[index].summery = !!status;
    tagRules.setValue(newState);
  }

  // 规则摘要信息
  getSummery(item, index) {
    const { tagRules, sortMode } = this.fields;
    const rules = tagRules.getValue();
    const mode = sortMode.getValue();
    return (
      <Card style={{ width: '100%!important' }}>
        <Card.Body
          title={
            <>
              {mode && <Icon type={'sort'} />}
              {pingRuleContent(this.format([item])[0])}
            </>
          }
          operation={
            <>
              {!mode && rules.length > 1 && (
                <Button type={'text'} onClick={this.removeTagRule.bind(this, index)}>
                  删除规则
                </Button>
              )}
              {!mode && (
                <Button type={'text'} onClick={this.toggleSummery.bind(this, index, false)}>
                  显示
                </Button>
              )}
            </>
          }
        />
      </Card>
    );
  }

  setFieldValue(field, index, key, value) {
    const list = field.getValue();
    list[index][key] = value;
    field.setValue(list);
  }

  renderItem(rules, data, index) {
    const { duck, store, dispatch } = this.props;
    const { ducks } = duck;
    const self = this as any;
    const { serviceName, namespaceId, disableApi, registryId } = self.props.options;
    const composedId = { serviceName, namespaceId, registryId };
    const { tagRules } = this.fields;
    if (disableApi) {
      systemTagOptions = systemTagOptions.filter(item => item !== SYSTEM_TAG.TSF_DEST_API_PATH);
    }

    return rules[index].summery ? (
      this.getSummery(rules[index], index)
    ) : (
      <Card style={{ width: '100%!important' }}>
        <Card.Body
          title={`规则【${index + 1}】`}
          operation={
            <>
              {rules.length > 1 && (
                <Button type={'text'} onClick={this.removeTagRule.bind(this, index)}>
                  删除规则
                </Button>
              )}
              <Button type={'text'} onClick={this.toggleSummery.bind(this, index, true)}>
                隐藏
              </Button>
            </>
          }
        >
          <Form>
            <FormItem label={'流量来源配置'}>
              <TagTable
                duck={ducks.tagTable}
                store={store}
                dispatch={dispatch}
                ref={instance => {
                  self.ruleRefs[`TagTable_${index}`] = instance;
                }}
                list={[...(data.tags || [])]}
                systemTags={systemTagOptions}
                onChange={list => this.setFieldValue(tagRules, index, 'tags', list)}
                params={composedId}
              />
            </FormItem>
            <FormItem label={'流量目的地'}>
              <TargetGrid
                duck={ducks.targetGrid}
                store={store}
                dispatch={dispatch}
                ref={instance => {
                  self.ruleRefs[`TargetGrid_${index}`] = instance;
                }}
                list={[...(data.destTag || [])]}
                onChange={list => this.setFieldValue(tagRules, index, 'destTag', list)}
                params={composedId}
              />
            </FormItem>
          </Form>
        </Card.Body>
      </Card>
    );
  }

  render() {
    const self = this as any;
    const { ruleName, sortMode, tagRules, status } = this.fields;
    const { onSubmit } = this.props;

    return (
      <>
        <div style={{ clear: 'both' }} />
        <Form style={{ width: '100%' }}>
          <FormField field={ruleName} message={nameTipMessage} label={'规则名'} align='middle'>
            <Input placeholder={'请输入名称'} field={ruleName} maxLength={60} />
          </FormField>
          <FormItem label={'规则'} align='middle'>
            {!sortMode.getValue() && (
              <Button type={'primary'} onClick={this.addTagRule.bind(this)}>
                新增规则
              </Button>
            )}
            {!sortMode.getValue() && tagRules.getValue() && tagRules.getValue().length > 1 && (
              <Button
                style={{ marginLeft: '10px' }}
                onClick={() => {
                  if (this.isValid()) {
                    self.sortlistRef.sort();
                    sortMode.setValue(true);
                  } else {
                    notification.error({ description: '请完善信息后排序' });
                  }
                }}
              >
                调整顺序
              </Button>
            )}
            {sortMode.getValue() && (
              <Button
                onClick={() => {
                  self.sortlistRef.finish();
                  sortMode.setValue(false);
                }}
              >
                确认规则顺序
              </Button>
            )}
            <SortList
              ref={instance => (self.sortlistRef = instance)}
              canSort={false}
              list={tagRules.getValue() || []}
              getSummery={this.getSummery.bind(this)}
              renderItem={this.renderItem.bind(this)}
              onSave={rules => tagRules.setValue(JSON.parse(JSON.stringify(rules)))}
            />
          </FormItem>
          <FormItem label={'生效状态'}>
            <Switch
              value={status.getValue()}
              onChange={checked => {
                status.setValue(+checked);
              }}
            />
          </FormItem>
          <FormItem>
            <Button type='primary' onClick={onSubmit}>
              完成
            </Button>
          </FormItem>
        </Form>
      </>
    );
  }
}
