import GridPageGrid from '@src/common/duckComponents/GridPageGrid';
import GridPagePagination from '@src/common/duckComponents/GridPagePagination';
import React from 'react';
import { DuckCmpProps } from 'saga-duck';
import { Card, Justify, Table } from 'tea-component';
import BaseInfoDuck from './PageDuck';
import getColumns from './getColumns';
import moment from 'moment';
import TimeSelect from '@src/common/components/TimeSelect';
import { filterable } from 'tea-component/lib/table/addons';

export default function BaseInfo(props: DuckCmpProps<BaseInfoDuck>) {
  const { duck, store, dispatch } = props;
  const { selectors, creators } = duck;
  const eventTypeList = selectors.eventTypeList(store);
  const columns = React.useMemo(() => getColumns(props), [eventTypeList]);
  const eventType = selectors.eventType(store);

  const handlers = React.useMemo(
    () => ({
      reload: () => dispatch(creators.reload()),
      search: keyword => dispatch(creators.search(keyword)),
      inputKeyword: keyword => dispatch(creators.inputKeyword(keyword)),
      clearKeyword: () => dispatch(creators.search('')),
      changeDate: ({ from, to }) =>
        dispatch(
          creators.setFilterTime({
            startTime: from.getTime(),
            endTime: to.getTime(),
          }),
        ),
      setEventType: value => dispatch(creators.setEventType(value)),
    }),
    [],
  );

  return (
    <>
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
        />
      </Table.ActionPanel>
      <Card>
        <GridPageGrid
          duck={duck}
          dispatch={dispatch}
          store={store}
          columns={columns}
          addons={[
            filterable({
              type: 'single',
              column: 'eventType',
              value: eventType,
              onChange: value => handlers.setEventType(value),
              options: eventTypeList,
              all: {
                value: '',
                text: '所有',
              },
            }),
          ]}
        />
        <GridPagePagination duck={duck} dispatch={dispatch} store={store} />
      </Card>
    </>
  );
}
