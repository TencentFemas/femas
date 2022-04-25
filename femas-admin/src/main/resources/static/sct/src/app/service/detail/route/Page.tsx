import * as React from 'react';
import { DuckCmpProps, memorize, purify } from 'saga-duck';
import Duck from './PageDuck';
import { Bubble, Button, Card, Icon, Justify, Switch, Table, Text } from 'tea-component';
import getColumns from './getColumns';
import { Link } from 'react-router-dom';
import GridPageGrid from '@src/common/duckComponents/GridPageGrid';
import GridPagePagination from '@src/common/duckComponents/GridPagePagination';
import { RULE_STATUS_MAP } from '../../types';
import TimeSelect from '@src/common/components/TimeSelect';
import moment from 'moment';
import { Line } from '@src/common/qcm-chart';
import formatDate from '@src/common/util/formatDate';

const getHandlers = memorize(({ creators }: Duck, dispatch) => ({
  //设置微服务路由保护策略
  reload: () => dispatch(creators.reload()),
  setFbRouteStatus: state => dispatch(creators.setFbRouteStatus(state)),
  setFilterTime: ({ from, to }) =>
    dispatch(
      creators.setFilterTime({
        startTime: formatDate(from),
        endTime: formatDate(to),
      }),
    ),
}));

// eslint-disable-next-line prettier/prettier
export default purify(function (props: DuckCmpProps<Duck>) {
  const { duck, store, dispatch } = props;
  const { selectors } = duck;
  const handlers = getHandlers(props);
  const { namespaceId, serviceName, registryId } = selectors.composedId(store);

  const url = `/service/service-route-create?namespaceId=${namespaceId}&registryId=${registryId}&serviceName=${serviceName}`;

  const emptyTipsHtml = () => {
    return (
      <>
        <Text parent={'p'}>
          <Text verticalAlign='middle'>未配置路由规则，</Text>
          <Link to={url}>
            <Button type='link'>立即添加</Button>
          </Link>
        </Text>
      </>
    );
  };

  const fallbackRoute = selectors.fallbackRoute(store);
  const fallbackRouteLoading = selectors.fallbackRouteLoading(store);
  const ruleList = selectors.list(store);
  const monitorLoading = selectors.monitorLoading(store);
  const monitorData = selectors.monitorData(store);

  return (
    <>
      <Table.ActionPanel>
        <Justify
          left={
            <Link to={url}>
              <Button type='primary'>新建路由规则</Button>
            </Link>
          }
          right={
            <>
              <Text reset verticalAlign={'middle'}>
                容错保护开关
              </Text>
              <Bubble
                placement={'right'}
                content={
                  <p style={{ fontWeight: 'normal' }}>
                    当流量目的地服务版本出现服务实例故障时，是否将流量导入该服务下其他服务版本
                  </p>
                }
              >
                <Icon type={'info'} />
              </Bubble>
              <Switch
                value={fallbackRoute}
                disabled={fallbackRouteLoading}
                loading={fallbackRouteLoading}
                onChange={checked => {
                  handlers.setFbRouteStatus(checked);
                }}
                tooltip={RULE_STATUS_MAP[+fallbackRoute]}
              />

              <Button type='icon' icon='refresh' onClick={handlers.reload}>
                刷新
              </Button>
            </>
          }
        />
      </Table.ActionPanel>
      <Card>
        <GridPageGrid
          duck={duck}
          store={store}
          dispatch={dispatch}
          columns={getColumns(props)}
          emptyTips={emptyTipsHtml()}
        />
        <GridPagePagination duck={duck} store={store} dispatch={dispatch} />
      </Card>
      {ruleList.length ? (
        <Card>
          <Card.Body>
            <TimeSelect
              style={{ marginBottom: 10 }}
              tabs={[
                {
                  text: '近1小时',
                  date: [moment().subtract(1, 'hour'), moment()],
                },
                {
                  text: '近6小时',
                  date: [moment().subtract(6, 'hour'), moment()],
                },
                {
                  text: '近7天',
                  date: [moment().subtract(7, 'd'), moment()],
                },
              ]}
              defaultIndex={0}
              changeDate={handlers.setFilterTime}
            />
            <Line
              height={350}
              loading={monitorLoading}
              title='流量详情（次）'
              pointSize={1}
              lineWidth={2}
              data={monitorData || []}
              axisLine={{ yAxisNeedLine: false }}
            />
          </Card.Body>
        </Card>
      ) : (
        <noscript />
      )}
    </>
  );
});
