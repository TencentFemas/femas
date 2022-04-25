import * as React from 'react';
import { DuckCmpProps, memorize } from 'saga-duck';
import Duck from './PageDuck';
import formatDate from '@src/common/util/formatDate';
import TimeSelect from '@src/common/components/TimeSelect';
import moment from 'moment';
import { Button, Card, Icon, Justify, Segment, Text } from 'tea-component';
import BasicLayout from '@src/common/components/BaseLayout';
import { LAYOUT_ICON_MAP, LayoutTypeText } from '../apm/topology/types';
import TopoSettings from './TopoSettings';
import { LayoutType } from '@src/common/qcm-chart';
import insertCSS from '@src/common/helpers/insertCSS';
import SearchableTeaSelect from '@src/common/duckComponents/SearchableTeaSelect';
import TopologyContent from '@src/app/apm/topology/Page';
import AutoComplete from '@src/common/components/AutoComplete';

insertCSS(
  'topology',
  `.fullScreen{
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 1000;
}
#topology, #topology .topology-full, #topology .topology-full .tea-card__content{
  height: 100%;
}
#topology .tsf-topology{
  height: 95%;
}`,
);

declare const ActiveXObject: (type: string) => void;

const getHandlers = memorize(({ creators }: Duck, dispatch) => ({
  changeDate: ({ from, to }) =>
    dispatch(
      creators.setFilterTime({
        startTime: formatDate(from),
        endTime: formatDate(to),
      }),
    ),
  changeService: item => dispatch(creators.changeService({ value: item.value })),
  onSvgClick: e => dispatch(creators.onSvgClick(e)),
}));

export default class Page extends React.Component<
  DuckCmpProps<Duck>,
  { isFullScreen: boolean; toggleScreenLoading: boolean }
> {
  constructor(props) {
    super(props);
    this.state = {
      isFullScreen: false,
      toggleScreenLoading: false,
    };
    this.toggleFullScreen = this.toggleFullScreen.bind(this);
    this.changeScreenStatus = this.changeScreenStatus.bind(this);
  }

  toggleFullScreen() {
    const { isFullScreen } = this.state;

    this.changeScreenStatus(!isFullScreen);

    let el = null;
    let rfs = null;

    if (isFullScreen) {
      el = document as any;
      // 退出全屏
      rfs = el.cancelFullScreen || el.webkitCancelFullScreen || el.mozCancelFullScreen || el.exitFullScreen;
    } else {
      el = document.documentElement as any;
      // 进入全屏
      rfs = el.requestFullScreen || el.webkitRequestFullScreen || el.mozRequestFullScreen || el.msRequestFullScreen;
    }

    if (typeof rfs !== 'undefined' && rfs) {
      rfs.call(el);
    } else if (typeof (window as any).ActiveXObject !== 'undefined') {
      //for IE，这里其实就是模拟了按下键盘的F11，使浏览器全屏/退出全屏
      const wscript = new ActiveXObject('WScript.Shell');
      if (wscript !== null) {
        wscript.SendKeys('{F11}');
      }
    }
  }

  changeScreenStatus(isFullScreen) {
    const handlers = getHandlers(this.props);
    document.querySelector('#topology').setAttribute('class', isFullScreen ? 'fullScreen' : '');

    this.setState({
      isFullScreen,
      toggleScreenLoading: true,
    });

    // 切换全屏时，重置选择
    handlers.changeService({ value: '' });

    setTimeout(() => {
      this.setState({
        toggleScreenLoading: false,
      });
    }, 500);
  }

  componentDidMount() {
    this.watchFullScreen();
  }

  watchFullScreen() {
    ['fullscreenchange', 'webkitfullscreenchange', 'mozfullscreenchange', 'msfullscreenchange'].forEach(eventType =>
      document.addEventListener(
        eventType,
        () => {
          const el = document as any;
          const fullscreenElement =
            el.fullscreenElement || el.msFullscreenElement || el.mozFullScreenElement || el.webkitFullscreenElement;
          this.changeScreenStatus(fullscreenElement !== null);
        },
        false,
      ),
    );
  }

  render() {
    const { duck, store, dispatch } = this.props;
    const {
      ducks: { namespace, topology },
    } = duck;
    const { selectors } = duck;
    const endTime = selectors.endTime(store);
    const handlers = getHandlers(this.props);
    const { isFullScreen } = this.state;
    const serviceList = selectors.serviceList(store);
    const curService = selectors.curService(store);
    const layoutType = topology.selectors.layoutType(store);

    return (
      <BasicLayout
        title='服务依赖拓扑'
        header={
          <>
            <Text reset verticalAlign='middle' theme='label'>
              命名空间：
            </Text>
            <SearchableTeaSelect
              duck={namespace}
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
          </>
        }
        selectors={selectors}
        store={store}
      >
        <section id='topology'>
          <Card className='topology-full'>
            <Card.Body className='topology-full'>
              {endTime !== null && (
                <Justify
                  left={
                    <>
                      <AutoComplete
                        showBubble
                        dataSource={serviceList}
                        value={curService.value}
                        onItemSelect={handlers.changeService}
                      />
                      <TimeSelect
                        tabs={[
                          {
                            text: '近30分钟',
                            date: [moment().subtract(30, 'minutes'), moment()],
                          },
                          {
                            text: '近10分钟',
                            date: [moment().subtract(10, 'minutes'), moment()],
                          },
                          {
                            text: '近5分钟',
                            date: [moment().subtract(5, 'minutes'), moment()],
                          },
                        ]}
                        defaultIndex={1}
                        changeDate={handlers.changeDate}
                        range={{
                          min: moment().subtract(30, 'd'),
                          max: moment(),
                          maxLength: 7,
                        }}
                        style={{ display: 'inline-block' }}
                        header={
                          <>
                            <Icon type='infoblue' />
                            <Text theme='primary' verticalAlign='middle' style={{ marginLeft: 10 }}>
                              服务依赖拓扑图仅能查询近1个月的数据，同时查询时间跨度不能超过7天。
                            </Text>
                          </>
                        }
                      />
                    </>
                  }
                  right={
                    <>
                      <Segment
                        options={Object.values(LayoutType).map(value => ({
                          value,
                          text: (
                            <img
                              src={LAYOUT_ICON_MAP[value]}
                              width={16}
                              height={16}
                              style={{ display: 'inline-block', marginTop: 6 }}
                            />
                          ),
                          tooltip: LayoutTypeText[value],
                        }))}
                        value={layoutType}
                        onChange={value => dispatch(topology.creators.setLayoutType(value))}
                      />
                      <Button
                        type='icon'
                        style={{ margin: '0 10px' }}
                        icon={isFullScreen ? 'fullscreenquit' : 'fullscreen'}
                        tooltip={isFullScreen ? '退出全屏' : '全屏'}
                        onClick={this.toggleFullScreen}
                      />
                    </>
                  }
                />
              )}
              <TopologyContent
                duck={topology}
                dispatch={dispatch}
                store={store}
                maxHeight={isFullScreen ? 4000 : null}
                disabledOpt={{
                  disabled: isFullScreen,
                  disabledPromt: '全屏模式不支持查看调用链和图表，请退出全屏后操作',
                }}
                onSvgClick={handlers.onSvgClick}
              />
              <TopoSettings duck={duck} dispatch={dispatch} store={store} />
            </Card.Body>
          </Card>
        </section>
      </BasicLayout>
    );
  }
}
