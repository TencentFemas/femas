import * as React from 'react';
import { Component } from 'react';
import moment, { Moment } from 'moment';
import { DatePicker, Segment } from 'tea-component';
import insertCSS from '../helpers/insertCSS';

const { RangePicker } = DatePicker;
const curDate = new Date();

insertCSS(
  'date-picker',
  `.date-picker input{
  border-left: 0;
}`,
);

interface Tab {
  text: string;
  date: Moment[];
  value?: number;
}

export interface Range {
  min: Moment;
  max: Moment;
  maxLength?: number;
}

interface TimeSelectProps {
  tabs?: Array<Tab>;
  initTabs?: (current) => Array<any>;
  defaultIndex?: number;
  from?: string;
  to?: string;
  changeDate: (value) => void;
  range?: Range;
  style?: React.CSSProperties;
  header?: React.ReactNode;
}

export default class TimeSelect extends Component<TimeSelectProps, any> {
  constructor(props) {
    super(props);
    this.state = {
      pickerIndex: 0,
      dateValue: [],
      current: 0,
    };
  }

  get current() {
    let { current } = this.state;
    if (current) {
      return current;
    }
    current = new Date().getTime();
    this.setState({ current });
    return current;
  }

  get pickerTabs() {
    let { tabs } = this.props;
    const { initTabs } = this.props;
    if (tabs) {
      return tabs.map((tab, i) => ({ ...tab, value: i }));
    }
    if (initTabs) {
      return initTabs(this.current);
    }
    tabs = [
      {
        text: '近24小时',
        date: [moment().subtract(1, 'd'), moment()],
        value: 0,
      },
      {
        text: '近3天',
        date: [moment().subtract(3, 'd'), moment()],
        value: 1,
      },
      {
        text: '近7天',
        date: [moment().subtract(7, 'd'), moment()],
        value: 2,
      },
    ];
    return tabs;
  }

  setPickerIndex(pickerIndex) {
    this.setState({
      pickerIndex,
    });
  }

  setDateValue(dateValue) {
    this.setState({
      dateValue,
    });
  }

  UNSAFE_componentWillReceiveProps(nextProps) {
    // from&to 置空时，重写处理Tab
    if (!nextProps.from && nextProps.from !== this.props.from && !nextProps.to && nextProps.to !== this.props.to) {
      this.handleInitData(nextProps.from, nextProps.to);
    }
  }

  componentDidMount() {
    const { from, to } = this.props;
    // 某些情况不需要初始化
    this.handleInitData(from, to);
  }

  handleInitData(from, to) {
    const { defaultIndex } = this.props;
    if (from && to) {
      // 如果初始化时传递了from，to，则直接设置dateValue
      this.setPickerIndex(null);
      this.setDateValue([moment(from), moment(to)]);
      return;
    }
    const defaultDate = this.pickerTabs[defaultIndex || 0].date;
    this.setPickerIndex(defaultIndex || 0);
    this.setDateValue(defaultDate);
    setTimeout(
      () =>
        this.props.changeDate &&
        this.props.changeDate({
          from: defaultDate[0].toDate(),
          to: defaultDate[1].toDate(),
        }),
    );
  }

  flush(silent = false) {
    const { pickerIndex } = this.state;
    // silent为true，不更新时间
    if (pickerIndex === null || silent) {
      return;
    }
    // 处于tab，则根据index更新tab时间
    const date = [...this.pickerTabs[pickerIndex].date];
    this.setDateValue(date);
    this.props.changeDate &&
      this.props.changeDate({
        from: date[0].toDate(),
        to: date[1].toDate(),
      });
  }

  range(s, t) {
    return Array(t - s)
      .fill(0)
      .map((_, i) => s + i + 1);
  }

  disabledDate(date, start) {
    /**
     * range: {max: Date, min: Date , maxLength: number}
     * max默认当前时间
     * maxLength默认30天跨度
     */
    const { range } = this.props;
    const { max, maxLength } = range || ({} as any);
    // 选择范围在今天之前，且选择跨度不大于三十天
    const isAfter = date.isAfter(max || moment(curDate), 'day');
    if (moment.isMoment(start)) {
      return (
        !isAfter &&
        !(
          moment(date)
            .subtract(maxLength || 30, 'day')
            .isAfter(start, 'day') ||
          moment(date)
            .add(maxLength || 30, 'day')
            .isBefore(start, 'day')
        )
      );
    }
    return !isAfter;
  }

  judgeRange(dates, type = 'hour') {
    const [start, end] = dates;
    const { range } = this.props;
    if (!range) return [];
    // 判断range
    const { maxLength } = range;
    if (maxLength && end.diff(start, 'd') === maxLength) {
      switch (type) {
        case 'minute':
          return this.range(start.minute(), 59);
        case 'second':
          return this.range(start.second(), 59);
        default:
          return this.range(start.hour(), 23);
      }
    }
    return [];
  }

  disabledTime(dates, partial) {
    if (partial === 'start') {
      return;
    }
    return {
      disabledHours: () => this.judgeRange(dates, 'hour'),
      disabledMinutes: () => this.judgeRange(dates, 'minute'),
      disabledSeconds: () => this.judgeRange(dates, 'second'),
    };
  }

  render() {
    const { pickerIndex, dateValue } = this.state;
    const { range, header } = this.props;

    return (
      <div style={this.props.style}>
        <Segment
          style={{ marginRight: 0 }}
          value={pickerIndex}
          options={this.pickerTabs}
          onChange={index => {
            this.setPickerIndex(index);
            const dateValue = this.pickerTabs[index].date;
            this.setDateValue(dateValue);
            this.props.changeDate &&
              this.props.changeDate({
                from: dateValue[0].toDate(),
                to: dateValue[1].toDate(),
              });
          }}
        />
        <RangePicker
          className='date-picker'
          value={dateValue}
          disabledDate={this.disabledDate.bind(this)}
          disabledTime={this.disabledTime.bind(this)}
          range={range ? [range.min, range.max] : null}
          showTime={{ format: 'HH:mm:ss' }}
          onChange={value => {
            this.setPickerIndex(null);
            this.setDateValue(value);
            this.props.changeDate &&
              this.props.changeDate({
                from: value[0].toDate(),
                to: value[1].toDate(),
              });
          }}
          header={header}
        />
      </div>
    );
  }
}
