import * as React from 'react';
import { DuckCmpProps, memorize } from 'saga-duck';
import Duck from './PageDuck';
import GridPageGrid from '@src/common/duckComponents/GridPageGrid';
import GridPagePagination from '@src/common/duckComponents/GridPagePagination';
import { Justify, Text } from 'tea-component';
import DetailGrid from './detail/DetailGrid';
import insertCSS from '@src/common/helpers/insertCSS';
import { expandable } from 'tea-component/lib/table/addons';

insertCSS(
  'atom-trace-info',
  `.atom-trace-info .atom-trace-info-item{
  display: inline-block;
  width: 20px;
  height: 6px;
  border-radius: 5px;
  margin-right: 2px;
}`,
);

const getHandlers = memorize(({ creators }: Duck, dispatch) => ({
  unfold: value => dispatch(creators.unfold(value)),
}));

interface Props extends DuckCmpProps<Duck> {
  bordered?: boolean;
  addons?: Array<any>;
}

export default class Page extends React.Component<Props, {}> {
  state = {
    expandedKeys: [],
    init: true,
  };

  updateExpandedKeys(expandedKeys) {
    this.setState({
      expandedKeys,
    });
  }

  render() {
    const { duck, store, dispatch, bordered, addons = [] } = this.props;
    const { ducks } = duck;
    const { selectors } = duck;
    const handlers = getHandlers(this.props);
    const { expandedKeys } = this.state;

    return (
      <>
        <GridPageGrid
          bordered={bordered}
          duck={duck}
          store={store}
          dispatch={dispatch}
          columns={selectors.columns(store)}
          addons={[
            expandable({
              expandedKeys,
              onExpandedKeysChange: (keys, { event, operateType, operateRecord }) => {
                event.stopPropagation();
                this.updateExpandedKeys(keys);
                if (operateType === 'expand') {
                  // 获取调用链详情
                  handlers.unfold(operateRecord.traceId);
                }
              },
              render(record) {
                const duckId = `subGrid-${record.traceId}`;
                const subGridDuck = ducks.subGrid.getDuck(duckId);
                if (!subGridDuck) return <noscript />;
                const data = subGridDuck.selectors.data(store);
                return (
                  <div className='_tsf-call-chain'>
                    <DetailGrid
                      duck={subGridDuck}
                      store={store}
                      dispatch={dispatch}
                      description={
                        data && (
                          <Justify
                            left={
                              <Text reset theme='label'>
                                开始时间: {data.startTime} 丨 请求耗时: {data.duration}
                                ms 丨 总服务数: {data.serviceNum} 丨 层次: {data.depth} 丨 总SPAN数: {data.spanNum}
                              </Text>
                            }
                            right={
                              <Text className='atom-trace-info' reset>
                                <Text style={{ marginRight: 10 }}>
                                  <Text className='atom-trace-info-item' style={{ background: '#A5E06C' }}></Text>
                                  <Text verticalAlign='middle' theme='label'>
                                    客户端耗时
                                  </Text>
                                </Text>
                                <Text style={{ marginRight: 10 }}>
                                  <Text className='atom-trace-info-item' style={{ background: '#4f9afa' }}></Text>
                                  <Text verticalAlign='middle' theme='label'>
                                    服务端耗时
                                  </Text>
                                </Text>
                                <Text>
                                  <Text className='atom-trace-info-item' style={{ background: 'red' }}></Text>
                                  <Text verticalAlign='middle' theme='label'>
                                    失败调用
                                  </Text>
                                </Text>
                              </Text>
                            }
                          />
                        )
                      }
                    />
                  </div>
                );
              },
              gapCell: 1,
              rowExpand: true,
            }),
            ...addons,
          ]}
        />
        <GridPagePagination duck={duck} store={store} dispatch={dispatch} />
      </>
    );
  }
}
