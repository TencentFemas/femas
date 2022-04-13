import React from 'react';
import BasicLayout from '@src/common/components/BaseLayout';
import { DuckCmpProps, purify } from 'saga-duck';
import OperationDuck from './PageDuck';
import { Button, Card, Justify, Table } from 'tea-component';
import GridPageGrid from '@src/common/duckComponents/GridPageGrid';
import GridPagePagination from '@src/common/duckComponents/GridPagePagination';
import getColumns from './getColumns';
import TimeSelect from '@src/common/components/TimeSelect';
import moment from 'moment';

export default purify(function OperationPage(props: DuckCmpProps<OperationDuck>) {
  const { duck, store, dispatch } = props;
  const { selectors, creators } = duck;
  const columns = React.useMemo(() => getColumns(), []);
  const handlers = React.useMemo(
    () => ({
      reload: () => dispatch(creators.reload()),
      changeDate: ({ from, to }) =>
        dispatch(
          creators.setFilterTime({
            startTime: from.getTime(),
            endTime: to.getTime(),
          }),
        ),
    }),
    [],
  );

  return (
    <BasicLayout store={store} selectors={selectors} title='变更记录'>
      <Table.ActionPanel>
        <Justify
          left={
            <TimeSelect
              tabs={[
                {
                  text: '近7天',
                  date: [moment().subtract(7, 'd'), moment()],
                },
                {
                  text: '近30天',
                  date: [moment().subtract(30, 'd'), moment()],
                },
              ]}
              defaultIndex={0}
              changeDate={handlers.changeDate}
              range={{
                min: moment().subtract(6, 'M'),
                max: moment(),
                maxLength: 30,
              }}
            />
          }
          right={
            <Button type='icon' icon='refresh' onClick={handlers.reload}>
              刷新
            </Button>
          }
        />
      </Table.ActionPanel>
      <Card>
        <GridPageGrid duck={duck} store={store} dispatch={dispatch} columns={columns}></GridPageGrid>
        <GridPagePagination duck={duck} store={store} dispatch={dispatch}></GridPagePagination>
      </Card>
    </BasicLayout>
  );
});
