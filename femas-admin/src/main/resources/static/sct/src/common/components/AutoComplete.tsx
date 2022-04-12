/* eslint-disable prettier/prettier */
import React, {useEffect, useState} from 'react';
import {Bubble, Select, Text} from 'tea-component';

export interface Item {
  value: string;
  text: string;
}

interface ItemForSearch extends Item {
  searchText: string;
}

interface Props {
  value: string;
  dataSource: Item[];
  className?: string;
  style?: React.CSSProperties;
  placeholder?: string;
  needBlank?: boolean;
  useCache?: boolean;
  onItemSelect?: (item) => void;
  onItemSearch?: (item) => void;
  groups?: {
    [groupKey: string]: React.ReactNode;
  };
  disabled?: boolean;
  itemRender?: (item: Item) => React.ReactElement;
  showBubble?: boolean;
}

export default function (props: Props) {
  const {
    style,
    value,
    groups,
    dataSource,
    placeholder,
    useCache,
    onItemSelect,
    onItemSearch,
    itemRender = (item: Item) =>
        props.showBubble ? (
            <Bubble content={item.text} placement='right'>
              <Text overflow>{item.text}</Text>
            </Bubble>
        ) : (
            item.text
        ),
    ...rest
  } = props;

  const options = React.useMemo(() => {
    return dataSource.map((item: Item) => ({
      ...item,
      searchText: item.text,
      text: itemRender(item),
    }));
  }, [dataSource]);

  const [withInputOption, setOptions] = useState(options);

  useEffect(() => {
    setOptions(options);
  }, [options]);

  const onSearch = React.useCallback(
      (value = '') => {
        if (useCache) {
          const filteredOptions = options.filter(opt => opt.searchText.includes(value));
          const isExists = options.some(opt => opt.searchText === value);
          if (value !== '' && !isExists) {
            filteredOptions.unshift({
              value,
              searchText: value,
              text: itemRender({text: value, value}),
            });
          }
          setOptions(filteredOptions);
        }
        onItemSearch && onItemSearch(value);
      },
      [onItemSearch, itemRender],
  );
  const filter = React.useCallback((value, option: ItemForSearch) => {
    return option.searchText.includes(value);
  }, []);

  const onChange = React.useCallback(
      value => {
        let matched;
        if (useCache) {
          matched = withInputOption.find(item => item.value === value);
        } else {
          matched = dataSource.find(item => item.value === value);
        }
        onItemSelect && matched && onItemSelect(matched);
      },
      [onItemSelect, withInputOption, dataSource],
  );

  return (
      <Select
          style={style}
          placeholder={placeholder}
          searchable
          boxSizeSync
          size='m'
          type='simulate'
          appearance='button'
          options={useCache ? withInputOption : options}
          value={value}
          groups={groups}
          onChange={onChange}
          onSearch={onSearch}
          onOpen={onSearch}
          filter={useCache ? () => true : filter}
          {...rest}
      />
  );
}
