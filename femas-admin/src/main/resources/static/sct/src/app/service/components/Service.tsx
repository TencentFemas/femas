import * as React from 'react';
import SearchableSelect, { Props as SearchableSelectProps } from '@src/common/duckComponents/SearchableSelect';
import { Bubble, Text } from 'tea-component';
import ServiceSelectorDuck from './ServiceSelectDuck';
import { DuckCmpProps } from 'saga-duck';

export default class Select extends React.Component<DuckCmpProps<ServiceSelectorDuck> & SearchableSelectProps> {
  render() {
    const { duck, store, dispatch, emptyItem, placeholder, showEmptyItem, ...rest } = this.props;
    const { selector } = duck;
    const { param } = selector(store);
    /**
     * 优先param中的设置
     */
    const showEmpty = param?.needEmptyItem ?? showEmptyItem;
    const empty = (param?.emptyItem ?? emptyItem) || {
      text: '全部',
      value: '',
    };

    return (
      <SearchableSelect
        duck={duck}
        store={store}
        dispatch={dispatch}
        {...rest}
        emptyItem={{
          ...empty,
          serviceName: '',
        }}
        placeholder={placeholder || empty?.text || (showEmpty ? '全部' : '请选择')}
        showEmptyItem={showEmpty}
        itemRenderer={(item: ServiceSelectorDuck['Item']) => (
          <Bubble placement='right' content={item.serviceName}>
            <Text overflow>{item.serviceName || empty.text}</Text>
          </Bubble>
        )}
      />
    );
  }
}
