import * as React from 'react';
import { SearchParams } from './types';
import SearchableSelect from '@src/common/duckComponents/SearchableSelect';
import { DuckCmpProps } from 'saga-duck';
import VersionSelectDuck from './VersionSelectDuck';
import { TSF_ROUTE_DEST_ELSE } from '../types';
import { Bubble, Text } from 'tea-component';

interface Props {
  params: SearchParams;
  onChange?: Function;
  value?: string;
  error?: string;
}

export interface SelectItem {
  value: string;
  text: string;
}

export default class VersionSelect extends React.Component<DuckCmpProps<VersionSelectDuck> & Props, any> {
  constructor(props) {
    super(props);
  }

  componentDidMount() {
    const { duck, dispatch, value } = this.props;
    if (value) {
      dispatch(duck.creators.select(value));
    }
  }

  render() {
    const { duck, store, dispatch, onChange } = this.props;
    const selected = duck.selectors.selected(store);
    return (
      <>
        <Bubble trigger={'hover'} content={selected && selected.text}>
          <Text parent={'div'}>
            <SearchableSelect
              size={'full'}
              duck={duck}
              store={store}
              dispatch={dispatch}
              showEmptyItem={true}
              emptyItem={{
                value: TSF_ROUTE_DEST_ELSE,
                text: '其他版本',
              }}
              // options={[...this.state.options]}
              itemRenderer={(item: SelectItem) => item.text}
              onChange={id => {
                onChange && onChange(id);
              }}
            />
          </Text>
        </Bubble>
      </>
    );
  }
}
