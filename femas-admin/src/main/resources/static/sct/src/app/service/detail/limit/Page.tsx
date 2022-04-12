import * as React from 'react';
import { DuckCmpProps, purify } from 'saga-duck';
import Duck from './PageDuck';
import getColumns from './getColumns';
import { Button, Card, Justify, SearchBox, Table, Text } from 'tea-component';
import GridPageGrid from '@src/common/duckComponents/GridPageGrid';
import GridPagePagination from '@src/common/duckComponents/GridPagePagination';
import { Link } from 'react-router-dom';
import TimeSelect from '@src/common/components/TimeSelect';
import moment from 'moment';
import { Line } from '@src/common/qcm-chart';
import formatDate from '@src/common/util/formatDate';

// eslint-disable-next-line prettier/prettier
export default purify(function (props: DuckCmpProps<Duck>) {
  const { duck, store, dispatch } = props;
  const { selectors, creators } = duck;
  const { namespaceId, serviceName, registryId } = selectors.composedId(store);
  const handlers = React.useMemo(
    () => ({
      reload: () => dispatch(creators.reload()),
      search: keyword => dispatch(creators.search(keyword)),
      inputKeyword: keyword => dispatch(creators.inputKeyword(keyword)),
      clearKeyword: () => dispatch(creators.search('')),
      setFilterTime: ({ from, to }) =>
        dispatch(
          creators.setFilterTime({
            startTime: formatDate(from),
            endTime: formatDate(to),
          }),
        ),
    }),
    [],
  );
  const url = `/service/service-limit-create?namespaceId=${namespaceId}&registryId=${registryId}&serviceName=${serviceName}`;
  const emptyTipsHtml = () => {
    return (
      <p>
        <Text verticalAlign='middle'>未配置限流规则，</Text>
        <Link to={url}>
          <Button type='link'>立即添加</Button>
        </Link>
      </p>
    );
  };
  const ruleList = selectors.list(store);
  const monitorLoading = selectors.monitorLoading(store);
  const monitorData = selectors.monitorData(store);

  return (
    <>
      <Table.ActionPanel>
        <Justify
          left={
            <Link to={url}>
              <Button type='primary'>新建限流规则</Button>
            </Link>
          }
          right={
            <>
              <SearchBox
                value={selectors.pendingKeyword(store)}
                placeholder='请输入名称搜索'
                onSearch={handlers.search}
                onChange={handlers.inputKeyword}
                onClear={handlers.clearKeyword}
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
              pointSize={1}
              lineWidth={2}
              data={monitorData?.requestMetric || []}
              title='请求数（次）'
              axisLine={{ yAxisNeedLine: false }}
            />
            <Line
              height={350}
              loading={monitorLoading}
              title='被限制请求率（%）'
              data={monitorData?.limitMetric || []}
              pointSize={1}
              lineWidth={2}
              colors={['#eb6455']}
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
