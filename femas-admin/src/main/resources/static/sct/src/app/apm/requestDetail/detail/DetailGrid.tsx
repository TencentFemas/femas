import * as React from 'react';
import { DuckCmpProps, memorize } from 'saga-duck';
import Duck from './DetailGridDuck';
import { Button, Icon, Text } from 'tea-component';
import { getDurationRender } from './DurationChart';
import { Resizable } from 'react-resizable';
import insertCSS from '@src/common/helpers/insertCSS';
import WithLoadingTip from '@src/common/components/WithLoadingTip';

insertCSS(
  'menu-tabel-hd',
  `.menu-tabel-hd .react-resizable .react-resizable-handle{
    width: 40px;
    height: 40px;
  }`,
);

interface Props extends DuckCmpProps<Duck> {
  description?: React.ReactNode;
}

// 判断是否属于levelId
const judgeItemSelected = (levelIds, item) => {
  return levelIds.indexOf(item.levelId) > -1;
};

const getHandlers = memorize(({ creators }: Duck, dispatch) => ({
  showSpanDetail: x => dispatch(creators.showSpanDetail(x)),
  toggleFold: x => dispatch(creators.toggleFold(x)),
}));

export default class TraceDetail extends React.Component<Props, { width: number; height: number }> {
  constructor(props) {
    super(props);
    this.state = {
      width: 260,
      height: 46,
    };
  }

  render() {
    const props = this.props;
    const { duck, store, description } = props;
    const { selectors } = duck;
    const data = selectors.data(store);
    const levelIds = selectors.levelIds(store);
    const handlers = getHandlers(props);
    const { width, height } = this.state;

    const renderTableColumns = x => {
      const unfold = judgeItemSelected(levelIds, x);
      const client = x.isClient ? x : x.client;
      const server = x.isClient ? x.server : x;

      // level 从0开始的，重构给的是1开始
      return (
        <ul className='menu-tabel-info large'>
          <li
            style={{ width }}
            className={`tree-li li-lv${x.level + 1} ${unfold && x.children?.length ? 'selected' : ''}`}
            onClick={
              x.children?.length
                ? () =>
                    handlers.toggleFold({
                      data: x,
                      status: unfold,
                    })
                : null
            }
          >
            <div>
              <div className='_tsf-zhan'>
                {x.children?.length ? <Button className='_tsf-icon' icon={unfold ? 'arrowdown' : 'arrowup '} /> : null}
              </div>
              <div
                style={{
                  position: 'relative',
                  width: 'calc(100% - 16px)',
                  lineHeight: '20px',
                }}
              >
                <Text parent='p'>
                  <Text overflow tooltip={client?.endpointName}>
                    {client?.endpointName || '-'}
                  </Text>
                </Text>
                <Text parent='p'>
                  <Text overflow tooltip={server?.endpointName}>
                    {server?.endpointName || '-'}
                  </Text>
                </Text>
              </div>
            </div>
          </li>
          <li className='middle-col'>
            <ul className='middle-col-ul'>
              <li>
                <div className='_tsf-function-name'>
                  <Text parent='p' tooltip={client?.serviceCode}>
                    {client?.serviceCode || '-'}
                  </Text>
                  <Text parent='p' tooltip={server?.serviceCode}>
                    {server?.serviceCode || '-'}
                  </Text>
                </div>
              </li>
              <li>
                <div className='_tsf-function-name'>
                  <Text parent='p' tooltip={client?.localIp}>
                    {client?.localIp || '-'}
                  </Text>
                  <Text parent='p' tooltip={server?.localIp}>
                    {server?.localIp || '-'}
                  </Text>
                </div>
              </li>
              <li>
                <Text parent='p' tooltip={x.duration}>
                  {x.isClient ? x.duration : x?.client?.duration ?? '-'} /&nbsp;
                  {x.isClient ? x?.server?.duration ?? '-' : x.duration}
                </Text>
              </li>
              <li style={{ width: 80 }}>
                <Button type='link' onClick={() => handlers.showSpanDetail(x)}>
                  {'详情'}
                </Button>
              </li>
            </ul>
          </li>
          <li>{getDurationRender(x, () => handlers.showSpanDetail(x))}</li>
        </ul>
      );
    };

    const renderTableHeaders = () => {
      return (
        <ul className='menu-tabel-hd large'>
          <Resizable
            axis='x'
            width={width}
            height={height}
            onResize={(event, { size }) => {
              this.setState({ width: size.width, height: size.height });
            }}
            minConstraints={[260, 100]}
          >
            <li style={{ width }}>
              <div>{'客户端/服务端方法名'}</div>
              <Icon type='transfer' />
            </li>
          </Resizable>
          <li className='middle-col'>
            <ul className='middle-col-ul'>
              <li>{'客户端/服务端服务名'}</li>
              <li>
                <Text overflow tooltip={'客户端/服务端IP'}>
                  {'客户端/服务端IP'}
                </Text>
              </li>
              <li>
                <Text overflow tooltip='客户端/服务端耗时(ms)'>
                  {'客户端/服务端耗时(ms)'}
                </Text>
              </li>
              <li style={{ width: 80 }}>{'操作'}</li>
            </ul>
          </li>
          <li>
            <span>0ms</span>
            <span>{(data.duration / 2).toFixed(2)}ms</span>
            <span>{data.duration}ms</span>
          </li>
        </ul>
      );
    };

    const renderTraceItem = traceData => {
      return traceData?.map(d => (
        <li key={d.levelId}>
          <div className='menu-item'>{renderTableColumns(d)}</div>
          {judgeItemSelected(levelIds, d) && d.children && (
            <ul className={`menu-sub menu-sub-${d.level + 2}`}>{renderTraceItem(d.children)}</ul>
          )}
        </li>
      ));
    };

    return (
      <WithLoadingTip loading={!data}>
        {data && data.list && data.list.length ? (
          <>
            {description}
            <div className='_tsf-chain'>
              <div className='_tsf-chain-list'>
                {renderTableHeaders()}
                <dl>
                  {data.list.map(x => {
                    const unfold = judgeItemSelected(levelIds, x);
                    return (
                      <dd key={x.levelId} className={unfold && x.children?.length ? 'selected' : ''}>
                        <div className='menu-item'>{renderTableColumns(x)}</div>
                        {unfold && x.children?.length ? (
                          <ul className={`menu-sub menu-sub-${x.level + 2}`}>{renderTraceItem(x.children)}</ul>
                        ) : null}
                      </dd>
                    );
                  })}
                </dl>
              </div>
            </div>
          </>
        ) : (
          <Text parent='p' align='center' style={{ minHeight: 200, paddingTop: 90 }}>
            {'暂无数据'}
          </Text>
        )}
      </WithLoadingTip>
    );
  }
}
