import * as React from 'react';
import { DuckCmpProps, memorize } from 'saga-duck';
import Duck from './PageDuck';
import { TabPanel, Tabs, Text } from 'tea-component';
import Search from './search/Page';
import Detail from './detail/Page';
import { TAB } from './types';
import BasicLayout from '@src/common/components/BaseLayout';
import SearchableTeaSelect from '@src/common/duckComponents/SearchableTeaSelect';

const TAB_LABLES = {
  [TAB.SEARCH]: 'Trace 查询',
  [TAB.SPAN]: 'Span 查询',
};

const tabs = [TAB.SEARCH].map(id => ({
  id,
  label: TAB_LABLES[id],
}));

const getHandlers = memorize(({ creators }: Duck, dispatch) => ({
  onSwitch: tab => dispatch(creators.switch(tab.id)),
}));

export default class Page extends React.Component<DuckCmpProps<Duck>, {}> {
  render() {
    const { duck, store, dispatch } = this.props;
    const handlers = getHandlers(this.props);
    const { selectors, ducks } = duck;
    const { namespace } = ducks;

    const tab = selectors.tab(store);

    return (
      <BasicLayout
        title={'调用链查询'}
        selectors={selectors}
        store={store}
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
      >
        <Tabs ceiling tabs={tabs} activeId={tab} onActive={handlers.onSwitch}>
          <TabPanel id={TAB.SEARCH}>
            <Search duck={ducks.search} store={store} dispatch={dispatch} />
          </TabPanel>
          <TabPanel id={TAB.SPAN}>
            <Detail duck={ducks.span} store={store} dispatch={dispatch} />
          </TabPanel>
        </Tabs>
      </BasicLayout>
    );
  }
}
