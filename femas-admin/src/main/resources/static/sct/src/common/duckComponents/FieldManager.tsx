import * as React from 'react';
import { Component } from 'react';
import { Button, Modal, ModalProps, notification } from 'tea-component';
import { DuckCmpProps } from 'saga-duck';
import DialogDuck from '../ducks/DialogPure';
import classnames from 'classnames';
import { showDialog } from '../helpers/showDialog';

interface Identifiable {
  id: number | string;
}

export interface ColumnField extends Identifiable {
  headTitle: React.ReactChild;
  /** 表示必选，不能取消 */
  required?: boolean;
  /** 表示默认选中 */
  defaults?: boolean;
  hide?: boolean;

  [propName: string]: any;
}

interface Props extends ModalProps {
  title?: string;
  fields: ColumnField[];
  tipText?: string;
  cacheKey: string;
  /** 最多选多少个字段 */
  maxLimit?: number;
  /** 超出最大限制强制不可选 */
  mandatoryMaxLimit?: boolean;
  /** 默认选了多少个 */
  defaultsNum?: number;
  /** 弹出窗内，每列展示多少个选项 */
  eachColNum?: number;
  onChange: any;
}

interface State {
  selection: string[];

  [propName: string]: any;
}

const defaultValue = {
  fields: [],
  tipText: '',
  eachColNum: 6,
  cacheKey: '',
  maxLimit: 0,
  defaultsNum: 0,
};

/**
 * 保证 selection 保持与 records 有相同的偏序关系，查找插入位置
 */
function findInsertPosition<T extends Identifiable>(records: T[], selection: string[], item: T): number {
  let ri = 0;
  let si = 0;
  while (records[ri] && selection[si]) {
    if (records[ri].id === item.id) break;
    if (records[ri].id === selection[si]) {
      si++;
    }
    ri++;
  }
  return si;
}

/**
 * 插入到选区中，保持与 records 相同的偏序顺序
 */
function selectionInsert(records: Identifiable[], selection: string[], item: Identifiable): string[] {
  selection = selection ? selection.slice() : [];

  let index = -1;
  for (let i = 0; i < selection.length; i++) {
    if (selection[i] === item.id) {
      index = i;
      break;
    }
  }

  if (index === -1) {
    const insertIndex = findInsertPosition(records, selection, item);
    selection.splice(insertIndex, 0, item.id + '');
  }

  return selection;
}

function selectionRemove<T extends Identifiable>(records: T[], selection: string[], item: T): string[] {
  selection = selection.slice();
  const index = selection.indexOf(item.id + '');

  if (index > -1) {
    selection.splice(index, 1);
  }

  return (selection || []).filter(x => x !== item.id);
}

type ButtonProps = Props;

/**
 * 自定义Grid列配置
 *
 * 代码参考模板： http://code.qcm.oa.com/?tpl=gridpage&addons=field-manager&patchOnly=1
 */
export default class FieldManagerButton extends React.Component<ButtonProps, any> {
  protected model: CustomFields;

  constructor(props) {
    super(props);
    this.model = new CustomFields(this.props.fields, this.props.cacheKey);
  }

  // eslint-disable-next-line react/no-deprecated
  componentWillMount() {
    // 初始触发一次
    this.props.onChange(this.model.getDisplayFields());
  }

  // eslint-disable-next-line react/no-deprecated
  componentWillReceiveProps(nexProps) {
    if (this.props.cacheKey !== nexProps.cacheKey || this.props.fields !== nexProps.fields) {
      this.model = new CustomFields(nexProps.fields, nexProps.cacheKey);
    }
    // 如果cacheKey变化，通常是不同的列表了，这里相当于初始化触发一次
    if (this.props.cacheKey !== nexProps.cacheKey) {
      this.props.onChange(this.model.getDisplayFields());
    }
  }

  render() {
    const { model } = this;
    const { style, ...rest } = this.props;
    return (
      <React.Fragment>
        <Button
          style={style}
          icon='setting'
          onClick={() => {
            showDialog(
              // eslint-disable-next-line prettier/prettier
                    function (props: MyDialogProps) {
                return <FieldManager {...{ model, ...rest, ...props }} />;
              },
              Duck,
              function*(duck: Duck) {
                yield* duck.show(null, () => {});
              },
            );
          }}
        />
      </React.Fragment>
    );
  }
}

class Duck extends DialogDuck {
  Data: any;
}

/**
 * 自定义列逻辑对象
 */
class CustomFields {
  public selection: string[];

  constructor(public fields: ColumnField[], public cacheKey: string) {
    this.init();
  }

  init() {
    this.selection = this.getInitSelection();
  }

  setSelection(selection: string[] = []) {
    this.selection = selection;
    const { cacheKey = '' } = this;
    cacheKey && (localStorage[cacheKey] = selection.join(','));
  }

  getDisplayFields(selection = this.selection): ColumnField[] {
    const { fields = defaultValue.fields } = this;
    return fields.reduce((columns, field) => {
      if (selection.indexOf(field.id) !== -1) {
        columns.push(field);
      }
      return columns;
    }, []);
  }

  private getInitSelection() {
    const { fields = [], cacheKey = '' } = this;
    let selection = localStorage[cacheKey] && localStorage[cacheKey].split(',');

    if (selection) {
      const fieldsMap = {};
      fields.forEach(field => {
        // hide属性为true的则不再默认勾选
        if (!field.hide) {
          fieldsMap[field.id] = true;
        }
      });
      selection = selection.filter(key => !!fieldsMap[key]);
      selection = fields.reduce((selection, field) => {
        return field.required ? selectionInsert(fields, selection, field) : selection;
      }, selection);
    }

    if (!selection) {
      selection = fields.reduce((selection, field) => {
        if (!field.hide) {
          selection.push(field.id);
        }
        return selection;
      }, []);
    }
    return selection;
  }
}

/**
 * 自定义列弹框
 */
interface MyDialogProps extends Omit<Props, 'fields' | 'cacheKey'>, DuckCmpProps<DialogDuck> {
  model: CustomFields;
}

class FieldManager extends Component<MyDialogProps, State> {
  state: State = {
    selection: this.props.model.selection,
  };

  getSelection() {
    const { selection = [] } = this.state || {};
    return selection;
  }

  setSelection(selection: string[] = []) {
    this.props.model.setSelection(selection);
    notification.success({ description: '设置成功' });
  }

  renderCol() {
    const count = this._countFields();
    const { maxLimit = count.count, mandatoryMaxLimit } = this.props;
    const { eachColNum = defaultValue.eachColNum } = this.props;
    const { fields = defaultValue.fields as ColumnField[] } = this.props.model;
    const { selection = [] } = this.state || {};
    const uls = [];
    fields.forEach((field, index) => {
      const required = !!field.required;
      const indexUl = Math.floor(index / eachColNum);
      if (!uls[indexUl]) {
        uls[indexUl] = [];
      }
      const isChecked = required || selection.indexOf(field && String(field.id)) > -1;
      uls[indexUl].push(
        <li key={field.id}>
          <input
            type='checkbox'
            className={classnames('tc-15-checkbox', {
              required: required,
            })}
            id={`customize_field_${field.id}`}
            value={field.id + ''}
            disabled={required || (!isChecked && mandatoryMaxLimit && count.selectedCount >= maxLimit)}
            checked={isChecked}
            onChange={e => {
              const checked = e.target.checked;
              const action = checked ? selectionInsert : selectionRemove;
              this._updateSelection(action(fields, selection, field));
            }}
          />
          <label htmlFor={`customize_field_${field.id}`}>{field.headTitle}</label>
        </li>,
      );
    });
    return uls.map((ul, index) => {
      return (
        <ul className='list-mod' key={index}>
          {ul}
        </ul>
      );
    });
  }

  renderContent() {
    const count = this._countFields();
    const { tipText = '请选择您想显示的列表详细信息。', maxLimit = count.count } = this.props;
    return (
      <div className='customize-column' id='customizeColumn' style={{ fontSize: '14px' }}>
        <div className='tc-15-msg'>
          {tipText}
          {maxLimit > 0 && (
            <span id='limitTip'>{`根据您的分辨率，最多可勾选${maxLimit}个字段，已勾选${count.selectedCount}个。`}</span>
          )}
        </div>
        <div className='list-wrap clearfix'>{this.renderCol()}</div>
      </div>
    );
  }

  render() {
    const self = this;
    const {
      duck: { creators, selectors },
      dispatch,
      store,
      size = 540,
      title = '自定义列表字段',
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      onChange = fields => {},
      model,
    } = this.props;
    if (!selectors.visible(store)) {
      return <noscript />;
    }
    return (
      <Modal
        caption={title}
        size={size}
        visible={true}
        disableEscape
        onClose={() => {
          dispatch(creators.hide());
        }}
      >
        <Modal.Body>{this.renderContent()}</Modal.Body>
        <Modal.Footer>
          <button
            className='tc-15-btn'
            onClick={() => {
              const selection = self.getSelection();
              const fields = model.getDisplayFields(selection);
              model.setSelection(selection);
              dispatch(creators.hide());
              onChange(fields);
            }}
          >
            {'确定'}
          </button>
        </Modal.Footer>
      </Modal>
    );
  }

  private _countFields() {
    const { fields = [] } = this.props.model;
    const { selection } = this.state;
    const selected = new Set(selection);
    let count = 0;
    let requiredCount = 0;
    let defaultsCount = 0;
    let selectedCount = 0;
    fields.forEach(field => {
      count++;
      if (field.required) {
        requiredCount++;
      }
      if (field.defaults) {
        defaultsCount++;
      }
      if (selected.has(String(field.id))) {
        selectedCount++;
      }
    });
    return {
      count: count,
      requiredCount,
      defaultsCount,
      selectedCount,
    };
  }

  private _updateSelection(selection) {
    this.setState({
      selection,
    });
  }
}
