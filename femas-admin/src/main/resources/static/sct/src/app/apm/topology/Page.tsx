import * as React from 'react';
import { DuckCmpProps } from 'saga-duck';
import Duck from './PageDuck';
import { generateNodeId } from './util';
import { Topology } from '@src/common/qcm-chart';
import { Button, Card, ExternalLink, Form, FormItem, FormText, Text } from 'tea-component';
import { SPAN_LS_KEY, TAB, TRACE_LS_KEY } from '@src/app/trace/types';
import { BIG_ICON_MAP, Disable, SERVICE_TYPE, TraceShowMode, TraceShowModeUnit } from './types';
import { getCurServiceTooltip } from './TraceGraphTooltip';

interface Props extends DuckCmpProps<Duck> {
  height?: number;
  maxHeight?: number;
  disabledOpt?: Disable;
  onSvgClick?: (e) => void;
  hideZoom?: boolean;
  topoScale?: number;
  onScaleChange?: (scale) => void;
}

// const getHandlers = memorize(({ creators }: Duck, dispatch) => ({}));

export default function Page(props: Props) {
  const { duck, store, disabledOpt, onSvgClick, ...rest } = props;
  const { selectors } = duck;
  const data = selectors.data(store);
  const { startTime, endTime, namespaceId, serviceName } = selectors.searchParams(store);
  // const handlers = getHandlers(props);
  const loading = selectors.loading(store);
  const curData = selectors.curData(store);
  const layoutType = selectors.layoutType(store);
  const nodeCalMode = selectors.nodeCalMode(store);
  const traceShowMode = selectors.traceShowMode(store);
  const grafanaUrl = selectors.grafanaUrl(store);

  const bindService = item => {
    // 节点 查看调用链
    localStorage.setItem(TRACE_LS_KEY.service, item.name);
    localStorage.setItem(TRACE_LS_KEY.startTime, item.startTime);
    localStorage.setItem(TRACE_LS_KEY.endTime, item.endTime);
    // 新页面打开
    window.open(`/trace?namespaceId=${namespaceId}&needLS=1`);
  };

  const bindLink = item => {
    // 查看调用链
    localStorage.setItem(SPAN_LS_KEY.startTime, item.startTime);
    localStorage.setItem(SPAN_LS_KEY.endTime, item.endTime);

    // 新页面打开
    window.open(`/trace?namespaceId=${namespaceId}&needLS=1&tab=${TAB.SPAN}`);
  };

  return (
    <div className='tc-g server-case-list tsf-topology'>
      <div style={{ position: 'relative', height: '100%' }}>
        <Topology
          curData={curData}
          nodeCalMode={nodeCalMode}
          data={
            data
              ? {
                  nodes: data.nodes,
                  traces: data.traces?.map(d => ({
                    ...d,
                    linkShowText: () =>
                      traceShowMode !== TraceShowMode.none
                        ? `${d[traceShowMode] ?? '-'} ${TraceShowModeUnit[traceShowMode]}`
                        : '',
                  })),
                }
              : null
          }
          loading={loading}
          layoutType={layoutType}
          curNodeId={serviceName ? generateNodeId(namespaceId, serviceName) : ''}
          convertRingData={d => {
            if (!d) return [0];
            return [d.errorRate, 1 - d.errorRate];
          }}
          svgClickEvent={{
            clickCallback: e => {
              onSvgClick && onSvgClick(e);
            },
          }}
          renderCurNodeTooltip={item => ({
            content: getCurServiceTooltip(item),
            width: 120,
            height: 100,
            clickCallback: (item, event) => {
              if (event.target.classList?.contains('service-cur-detail-btn')) {
                bindService({
                  ...item,
                  startTime,
                  endTime,
                });
              }
            },
          })}
          renderNodeTooltip={{
            render: item => (
              <Card>
                <Card.Body title={'依赖详情'}>
                  <Form>
                    <FormItem label={'服务名'}>
                      <FormText>
                        {item.type === SERVICE_TYPE ? (
                          <Button type='link' className='service-detail-btn'>
                            {item.name}
                          </Button>
                        ) : (
                          item.name
                        )}
                      </FormText>
                    </FormItem>
                    <FormItem label={'命名空间'}>
                      <FormText>
                        {item.namespaceName}({item.namespaceId})
                      </FormText>
                    </FormItem>
                    <FormItem label={'调用数'}>
                      <FormText>{item.requestVolume !== null ? `${item.requestVolume} ${'次'}` : '-'}</FormText>
                    </FormItem>
                    <FormItem label={'调用成功率'}>
                      <FormText>
                        {item.errorRate !== null ? `${((1 - item.errorRate) * 100).toFixed(2)} %` : '-'}
                      </FormText>
                    </FormItem>
                    <FormItem label={'平均调用延时'}>
                      <FormText>{item.averageDuration !== null ? `${item.averageDuration} ms` : '-'}</FormText>
                    </FormItem>
                    {!BIG_ICON_MAP[item.type] && (
                      <FormItem
                        label={
                          disabledOpt?.disabled ? (
                            <Text theme='label' data-title={disabledOpt?.disabledPromt}>
                              {'查看调用链'}
                            </Text>
                          ) : (
                            <ExternalLink href={null}>
                              <Button type='link' className='service-trace-btn'>
                                {'查看调用链'}
                              </Button>
                            </ExternalLink>
                          )
                        }
                      >
                        {!serviceName && (
                          <FormText>
                            {disabledOpt?.disabled ? (
                              <Text theme='label' data-title={disabledOpt?.disabledPromt || ''}>
                                {'查看监控'}
                              </Text>
                            ) : (
                              <ExternalLink
                                href={`${grafanaUrl}/d/t_cI60n7k/fu-wu-liu-liang-jian-kong?orgId=1&var-namespaceId=${item.namespaceId}&var-serviceName=${item.name}`}
                              >
                                <Button type='link' className='service-monitor-btn'>
                                  {'查看监控'}
                                </Button>
                              </ExternalLink>
                            )}
                          </FormText>
                        )}
                      </FormItem>
                    )}
                  </Form>
                </Card.Body>
              </Card>
            ),
            clickCallback: (item, event) => {
              if (event.target.classList?.contains('service-detail-btn')) {
                // 节点如果是点击了服务名
                window.open(
                  `/service/detail?id=${item.name}&namespaceId=${item.namespaceId}&registryId=${item.registryId}`,
                );
              }
              if (event.target.classList?.contains('service-trace-btn')) {
                bindService({
                  ...item,
                  startTime,
                  endTime,
                });
              }
              if (event.target.classList?.contains('service-monitor-btn')) {
                // 节点 查看监控
              }
            },
          }}
          renderLinkTooltip={{
            render: item => (
              <Card>
                <Card.Body title={'依赖详情'}>
                  <Form>
                    <FormItem label={'调用服务名'}>
                      <FormText>
                        {item.source.type === SERVICE_TYPE ? (
                          <Button type='link' className='service-source-detail-btn'>
                            {item.source.name}
                          </Button>
                        ) : (
                          item.source.name
                        )}
                      </FormText>
                    </FormItem>
                    <FormItem label={'主调方命名空间'}>
                      <FormText>
                        {item.source.namespaceName}({item.source.namespaceId})
                      </FormText>
                    </FormItem>
                    <FormItem label={'被调服务名'}>
                      <FormText>
                        {item.target.type === SERVICE_TYPE ? (
                          <Button type='link' className='service-target-detail-btn'>
                            {item.target.name}
                          </Button>
                        ) : (
                          item.target.name
                        )}
                      </FormText>
                    </FormItem>
                    <FormItem label={'被调方命名空间'}>
                      <FormText>
                        {item.target.namespaceName}({item.target.namespaceId})
                      </FormText>
                    </FormItem>
                    {/* <FormItem label={"调用数"}>
                      <FormText>
                        {item.reqTotalQty !== null
                          ? `${item.reqTotalQty} ${"次"}`
                          : "-"}
                      </FormText>
                    </FormItem>
                    <FormItem label={"调用成功率"}>
                      <FormText>
                        {item.reqSuccessRate !== null
                          ? `${(item.reqSuccessRate * 100).toFixed(2)} %`
                          : "-"}
                      </FormText>
                    </FormItem>
                    <FormItem label={"Apdex"}>
                      <FormText>{item.apdex}</FormText>
                    </FormItem>
                    <FormItem label={"平均调用延时"}>
                      <FormText>
                        {item.reqAvgDuration !== null
                          ? `${item.reqAvgDuration} ms`
                          : "-"}{" "}
                      </FormText>
                    </FormItem> */}
                    {/* 屏蔽span */}
                    {/* {!(
                      BIG_ICON_MAP[item.target.type] ||
                      BIG_ICON_MAP[item.source.type]
                    ) && (
                      <FormItem
                        label={
                          disabledOpt?.disabled ? (
                            <Text
                              theme="label"
                              data-title={disabledOpt?.disabledPromt || ""}
                            >
                              {"查看调用链"}
                            </Text>
                          ) : (
                            <ExternalLink href={null}>
                              <Button type="link" className="link-trace-btn">
                                {"查看调用链"}
                              </Button>
                            </ExternalLink>
                          )
                        }
                      >
                        {!serviceName && (
                          <FormText>
                            {disabledOpt?.disabled ? (
                              <Text
                                theme="label"
                                data-title={disabledOpt?.disabledPromt}
                              >
                                {"查看监控"}
                              </Text>
                            ) : (
                              <ExternalLink href={null}>
                                <Button
                                  type="link"
                                  className="link-monitor-btn"
                                >
                                  {"查看监控"}
                                </Button>
                              </ExternalLink>
                            )}
                          </FormText>
                        )}
                      </FormItem>
                    )} */}
                  </Form>
                </Card.Body>
              </Card>
            ),
            clickCallback: (item, event) => {
              if (event.target.classList?.contains('service-target-detail-btn')) {
                // 如果是点击了被调服务名
                window.open(
                  `/service/detail?id=${item.target.name}&namespaceId=${item.target.namespaceId}&registryId=${item.target.registryId}`,
                );
              }
              if (event.target.classList?.contains('service-source-detail-btn')) {
                // 如果是点击了调用服务名
                window.open(
                  `/service/detail?id=${item.source.name}&namespaceId=${item.source.namespaceId}&registryId=${item.source.registryId}`,
                );
              }
              if (event.target.classList?.contains('link-trace-btn')) {
                // 查看调用链
                bindLink({
                  ...item,
                  startTime,
                  endTime,
                });
              }
              if (event.target.classList?.contains('link-monitor-btn')) {
                // 查看监控
              }
            },
          }}
          {...rest}
        />
      </div>
    </div>
  );
}
