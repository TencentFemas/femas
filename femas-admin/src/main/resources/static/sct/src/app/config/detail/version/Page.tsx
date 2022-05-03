import * as React from 'react';
import { DuckCmpProps, memorize, purify } from 'saga-duck';
import Duck from './PageDuck';
import { Card, Col, Row, Text } from 'tea-component';
import getColumns from './getColumns';
import GridPageGrid from '@src/common/duckComponents/GridPageGrid';
import GridPagePagination from '@src/common/duckComponents/GridPagePagination';
import { filterable, radioable, scrollable, sortable } from 'tea-component/lib/table/addons';
import CodeMirrorBox from '@src/common/components/CodeMirrorBox';
import { RELEASE_STATUS, RELEASE_STATUS_NAME } from '../../types';

export const getHandlers = memorize(({ creators }: Duck, dispatch) => ({
  selectVersion: apiId => dispatch(creators.selectVersion(apiId)),
  setSort: v => dispatch(creators.setSort(v)),
  setStatus: v => dispatch(creators.setStatus(v)),
}));

// eslint-disable-next-line prettier/prettier
export default purify(function (props: DuckCmpProps<Duck>) {
  const { duck, store, dispatch } = props;
  const {
    selectors,
    ducks: { grid },
  } = duck;
  const handlers = getHandlers(props);
  const currentVersion = selectors.currentVersion(store);
  const { configType } = selectors.data(store);
  const list = grid.selectors.list(store);
  const sortValue = selectors.sortValue(store);
  const releaseStatus = selectors.releaseStatus(store);

  return (
    <Card>
      <Card bordered={true} className='_tsf-version-box'>
        <Row gap={0} showSplitLine={true}>
          <Col span={12} style={{ minHeight: 400 }}>
            <GridPageGrid
              duck={duck}
              store={store}
              dispatch={dispatch}
              columns={getColumns(props)}
              addons={[
                radioable({
                  value: currentVersion.configVersionId,
                  onChange: key => {
                    const seleted = list.find(item => item.configVersionId === key) || {};
                    handlers.selectVersion(seleted);
                  },
                  rowSelect: true,
                  render: () => {
                    return <noscript />;
                  },
                }),
                scrollable({ maxHeight: 550, minHeight: 550 }),
                sortable({
                  columns: [
                    { key: 'createTime', prefer: 'desc' },
                    { key: 'configVersion', prefer: 'desc' },
                    { key: 'releaseTime', prefer: 'desc' },
                  ],
                  value: sortValue,
                  onChange: value => handlers.setSort(value.length ? [value[0]] : []),
                }),
                filterable({
                  all: {
                    text: '所有',
                    value: '',
                  },
                  type: 'single',
                  column: 'status',
                  value: releaseStatus,
                  onChange: value => handlers.setStatus(value),
                  options: Object.values(RELEASE_STATUS).map(value => ({
                    value,
                    text: RELEASE_STATUS_NAME[value],
                  })),
                }),
              ]}
            />
            <GridPagePagination duck={duck} dispatch={dispatch} store={store} />
          </Col>
          <Col span={12}>
            {currentVersion && (
              <>
                <section
                  style={{
                    backgroundColor: '#f1f1f1',
                    padding: '13px 10px',
                    borderBottom: '1px solid #ddd',
                  }}
                >
                  <Text
                    theme='label'
                    reset={true}
                    style={{
                      wordBreak: 'break-all',
                      width: 'calc(100% - 32px)',
                      marginLeft: 8,
                    }}
                    tooltip={currentVersion.configVersion}
                    overflow
                  >
                    {'当前版本号'}: {currentVersion.configVersion || '-'}
                  </Text>
                </section>
                <CodeMirrorBox
                  style={{ borderBottom: 'none' }}
                  height={600}
                  value={currentVersion.configValue || ''}
                  options={{ readOnly: true, mode: configType }}
                />
              </>
            )}
          </Col>
        </Row>
      </Card>
    </Card>
  );
});
