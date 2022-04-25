import * as React from 'react';
import { SearchParams, TARGET_TYPE, TARGET_TYPE_NAME } from './types';
import VersionSelect from './VersionSelect';
import { Button, FormControl, Input, notification, Select, Table, Text } from 'tea-component';
import { RouteRuleDest, SYSTEM_TAG } from '../types';
import TargetGridDuck from './TargetGridDuck';
import { DuckCmpProps } from 'saga-duck';
import { genIdForNode } from '@src/common/util/common';

const VALUE_SELECT = {
  [TARGET_TYPE.VERSION]: VersionSelect,
};

interface Field<T> {
  value: T;
  option?: any;
  errValue: string;
}

interface Target {
  type: Field<string>;
  value: Field<string>;
  weight: Field<number>;
  duckIds: any;
}

interface TargetGridProps {
  list?: Array<RouteRuleDest>;
  params: SearchParams;

  onChange?(targetList: Array<RouteRuleDest>);
}

interface TargetGridState {
  dataList?: Array<Target>;
  errWord?: string;
}

const DuckTypeMap = {
  [SYSTEM_TAG.TSF_PROG_VERSION]: 'versionSelectDuck',
};

const DuckType = {
  version: 'versionSelectDuck',
};

const targetFormat = item => ({
  value: item,
  option: {},
  errValue: '',
});
export default class TargetGrid extends React.Component<
  DuckCmpProps<TargetGridDuck> & TargetGridProps,
  TargetGridState
> {
  constructor(props) {
    super(props);
    const { list = [] } = this.props;

    const tmpList = list.map(item => {
      const target = {
        type: targetFormat(TARGET_TYPE.VERSION),
        value: targetFormat(''),
        weight: targetFormat(item.weight),
        duckIds: {
          [DuckType.version]: this.getNewDuck(DuckType.version),
        },
      };
      target.value.value = item.serviceVersion;
      if (!target.duckIds[DuckType.version]) target.duckIds[DuckType.version] = this.getNewDuck(DuckType.version);
      return target as Target;
    });

    // 如果为空，默认增加一个
    if (!list.length) {
      tmpList.push(this.getNewTarget());
    }
    this.state = {
      dataList: tmpList,
      errWord: '',
    };
  }

  getNewTarget = () => {
    return {
      type: targetFormat(TARGET_TYPE.VERSION),
      value: targetFormat(''),
      weight: targetFormat(0),
      duckIds: {
        [DuckType.version]: this.getNewDuck(DuckType.version),
      },
    };
  };

  getNewDuck(type: string, appId?: string) {
    const randomIds = genIdForNode().split('-');
    const duckId = randomIds[randomIds.length - 1];
    const { duck, dispatch, params } = this.props;
    switch (type) {
      case DuckType.version:
        dispatch(
          duck.creators.createVersionDuck({
            params: { ...params, appId },
            duckId,
          }),
        );
        break;
    }
    return duckId;
  }

  isValid() {
    const { dataList } = this.state;
    let valid = true;
    for (const target of dataList) {
      for (const item in target) {
        if (item === 'duckId') continue;
        if (target[item].value === '') {
          valid = false;
          target[item].errValue = '请填写';
        } else target[item].errValue = '';
      }
    }
    this.setState({
      dataList,
    });
    let weightSum = 0;
    for (const item of dataList) {
      weightSum += +item.weight.value;
    }
    if (weightSum !== 100) {
      for (const item of dataList) {
        item.weight.errValue = '流量目的地权重总和不为100%';
      }
      return false;
    } else {
      for (const item of dataList) {
        item.weight.errValue = '';
      }
    }
    return valid;
  }

  addRow() {
    const nextDataList = this.state.dataList.slice();
    // 这里利用Object 引用将所有的type指向第一个
    nextDataList.push({ ...this.getNewTarget(), type: nextDataList[0].type });
    this.setState({ dataList: nextDataList });
  }

  removeRow(index) {
    const nextDataList = this.state.dataList.slice();
    if (nextDataList.length < 2) {
      notification.error({ description: '请保留至少一项流量目的地' });
      return;
    }
    nextDataList.splice(index, 1);
    this.setState({ dataList: nextDataList });
  }

  onChange(index, key, value, option?) {
    const { duck, dispatch } = this.props;
    let nextDataList = this.state.dataList.slice();
    if (!value) nextDataList[index][key].errValue = '请填写';
    else nextDataList[index][key].errValue = '';
    if (key === 'type') {
      nextDataList = nextDataList.map(item => {
        item.value.value = '';
        item.value.option = {};
        //取出该item对应的duck
        const prevDuckType = DuckTypeMap[item.type.value];
        const prevItemDuck = duck.ducks[prevDuckType].getDuck(item.duckIds[prevDuckType]);
        dispatch(prevItemDuck.creators.select(''));
        return {
          ...item,
          type: {
            value,
            errValue: '',
          },
        };
      });
    } else {
      if (option) {
        nextDataList[index][key].option = option;
      }
    }
    nextDataList[index][key].value = value;
    this.setState({ dataList: nextDataList });
    // 触发外界变化
    const { onChange } = this.props;
    onChange(
      nextDataList.map(item => {
        const target: RouteRuleDest = {
          weight: item.weight.value,
          serviceVersion: item.value.value,
        };
        return target;
      }),
    );
  }

  render() {
    const { dataList } = this.state;
    const { duck, store, dispatch } = this.props;
    const { ducks } = duck;
    return (
      <Table
        verticalTop
        columns={[
          {
            key: 'destination',
            header: '目的地类型',
            width: '150px',
            render: (target, rowKey) => {
              const index = Number(rowKey.split('_')[1]);
              return (
                <>
                  {index === 0 ? (
                    <Select
                      type='simulate'
                      appearance={'button'}
                      size='full'
                      options={TARGET_TYPE_NAME.map(item => {
                        return {
                          value: item.value,
                          text: item.name,
                        };
                      })}
                      value={target.type.value}
                      onChange={value => this.onChange(index, 'type', value)}
                    />
                  ) : (
                    <Text style={{ fontSize: '12px', paddingLeft: '10px' }}>
                      {'' + TARGET_TYPE_NAME.find(item => item.value === target.type.value).name}
                    </Text>
                  )}
                </>
              );
            },
          },
          {
            key: 'groupOrVersion',
            header: '版本号',
            width: '150px',
            render: (target, rowKey) => {
              const ValueSelect = VALUE_SELECT[target.type.value];
              const valueSelectDuck = ducks.versionSelectDuck.getDuck(target.duckIds[DuckType.version]);
              const index = Number(rowKey.split('_')[1]);
              if (!valueSelectDuck) return <noscript />;
              return (
                <FormControl
                  status={target.value.errValue ? 'error' : null}
                  message={target.value.errValue ? target.value.errValue : ''}
                  showStatusIcon={false}
                  style={{ width: '130px', maxWidth: '130px', padding: '0px' }}
                >
                  <ValueSelect
                    duck={valueSelectDuck}
                    store={store}
                    dispatch={dispatch}
                    onChange={value => this.onChange(index, 'value', value)}
                    error={target.value.errValue || ''}
                    params={this.props.params}
                    value={target.value.value}
                  />
                </FormControl>
              );
            },
          },
          {
            key: 'weight',
            header: '权重',
            width: '150px',
            render: (target, rowKey) => {
              const index = Number(rowKey.split('_')[1]);
              return (
                <FormControl
                  status={target.weight.errValue ? 'error' : null}
                  message={target.weight.errValue ? target.weight.errValue : ''}
                  showStatusIcon={false}
                  style={{ width: '130px', padding: '0px', maxWidth: '130px' }}
                >
                  <Input
                    size={'full'}
                    value={'' + target.weight.value}
                    type={'number'}
                    min={0}
                    max={100}
                    onBlur={e => this.onChange(index, 'weight', e.target.value)}
                    onChange={value => this.onChange(index, 'weight', value)}
                  />
                </FormControl>
              );
            },
          },
          {
            key: '',
            header: '',
            render: (target, rowKey) => {
              const index = Number(rowKey.split('_')[1]);
              return <Button icon='close' title={'删除'} onClick={() => this.removeRow(index)} />;
            },
          },
        ]}
        records={dataList}
        bottomTip={
          <Text parent={'div'} reset>
            <Button type='link' onClick={this.addRow.bind(this)}>
              新增目的地
            </Button>
          </Text>
        }
      />
    );
  }
}
