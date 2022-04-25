import * as React from 'react';
import insertCSS from '../helpers/insertCSS';
import { getClosest } from '../util/common';

insertCSS(
  'sortlist',
  `
    li.sortlist_item {
      cursor: move;
    }
    .dragMask .sortlist_item{
      background-color: #f7f7f7
    }
    .sort-opt{
      padding: 0 10px 5px 0;
      text-align: right;
      border-bottom:1px solid #ddd;
    }
    .sort-opt a{
      margin-left: 10px;
    }
    .sort-opt a:first-child{
      margin-left: 10px;
    }
    .dragMask .sortlist_item .param-box{
      margin:0;
    }
  `,
);

export interface SortListProps<T> {
  list: Array<T>;
  canSort?: boolean;

  getSummery?(item: T, index: number): string;

  renderItem(list: Array<T>, item: T, index: number): string;

  onChange?(list: ReadonlyArray<T>): void;

  onSave?(list: ReadonlyArray<T>): void;
}

interface SortListState {
  sortMode?: boolean;
  isDragging?: boolean;
  leftOffset?: number;
  clickOffset?: number;
  dragIndex?: number;
  maskTop?: number;
}

export default class SortList<T> extends React.Component<SortListProps<T>, SortListState> {
  constructor(props: SortListProps<T>) {
    super(props);

    this.state = {
      sortMode: false,
      isDragging: false,
    };
  }

  getSummery(item, index) {
    const { renderItem } = this.props;
    return renderItem(this.props.list, item, index);
  }

  render() {
    const { list, canSort = true } = this.props;
    const { isDragging, sortMode, dragIndex, leftOffset, maskTop } = this.state;
    const sort = this.sort.bind(this);
    const finish = this.finish.bind(this);
    return (
      <div className='sortlist'>
        {canSort && (
          <p className='sort-opt'>
            {!sortMode && (
              <a href='javascript:void(0)' onClick={sort}>
                {'排序'}
              </a>
            )}
            {sortMode && (
              <a href='javascript:void(0)' onClick={finish}>
                {'完成'}
              </a>
            )}
          </p>
        )}
        <ul className='sortlist_ul'>{this.renderList(list, sortMode, true)}</ul>
        {isDragging && (
          <ul
            className='dragMask'
            style={{
              position: 'fixed',
              top: maskTop,
              left: leftOffset,
              opacity: 1,
            }}
          >
            {this.renderList([list[dragIndex]], true, false)}
          </ul>
        )}
      </div>
    );
  }

  renderList(list, sortMode, mark) {
    const { getSummery = this.getSummery.bind(this), renderItem } = this.props;
    return list.map((item, key) =>
      sortMode ? (
        <li
          key={key}
          id={'' + key}
          className='sortlist_item'
          onMouseDown={this.dragStart.bind(this)}
          style={{
            visibility: mark && this.state.isDragging && this.state.dragIndex === key ? 'hidden' : 'visible',

            marginTop: '20px',
          }}
        >
          <div>{getSummery(item, key)}</div>
        </li>
      ) : (
        <li style={{ marginTop: '20px' }} key={key}>
          <div>{renderItem(this.props.list, item, key)}</div>
        </li>
      ),
    );
  }

  dragStart(event) {
    event.preventDefault();

    const { isDragging } = this.state;
    // 获取数据
    if (isDragging) {
      return;
    }

    // 获取拖拽行位置
    let $targetRow;
    if ($(event.target).closest && $(event.target).closest('.sortlist_item')) {
      $targetRow = $(event.target).closest('.sortlist_item');
    } else {
      $targetRow = getClosest($(event.target), '.sortlist_item');
    }
    const dragIndex = +$targetRow.attr('id');

    if (dragIndex === -1) {
      return;
    }
    // 计算位置
    const clickOffset = event.clientY - $targetRow.offset().top;
    const clientY = event.clientY;
    this.setState(
      {
        isDragging: true,
        dragIndex,
        leftOffset: $targetRow.offset().left,
        clickOffset,
      },
      () => {
        this.updateMask(clientY);
        // 监听鼠标移动&释放
        $(document)
          .on('mouseup.SortList', event => {
            if (this.state.isDragging) {
              this.dragEnd(event);
            }
            this.setState(preState => Object.assign(preState, { isDragging: false }));
          })
          .on('mousemove.SortList', event => {
            if (this.state.isDragging) {
              this.dragMove(event);
            }
          });
      },
    );
  }

  dragEnd(event) {
    event.preventDefault();
    this.setState(preState => Object.assign(preState, { isDragging: false }));
  }

  dragMove(event) {
    event.preventDefault();
    this.updateMask(event.clientY);
    this.exchangeData(event);
  }

  /**
   * 更新拖动缩略图
   * @param event 数据移动事件
   */
  updateMask(clientY) {
    this.setState({ maskTop: clientY - this.state.clickOffset });
  }

  /**
   * 交换数据和行
   * @param event 鼠标移动事件
   */
  exchangeData(event) {
    const { dragIndex } = this.state;
    if (dragIndex === -1) {
      return;
    }
    const targetRow = $(`.sortlist_ul #${dragIndex}.sortlist_item`);
    const targetRowOffset = {
      top: targetRow.offset().top,
      bottom: targetRow.offset().top + targetRow.outerHeight(),
    };
    const { list } = this.props;
    if (event.clientY < targetRowOffset.top) {
      if (dragIndex - 1 < 0) return;
      this._moveData(-1);
    }
    if (event.clientY > targetRowOffset.bottom) {
      if (dragIndex + 1 >= list.length) return;
      this._moveData(+1);
    }
    this._onChange();
  }

  sort() {
    this.setState({ sortMode: true });
  }

  finish() {
    const { onSave } = this.props;
    onSave && onSave(this.props.list);
    this.setState({
      sortMode: false,
    });
  }

  _moveData(offset: number) {
    const { dragIndex } = this.state;
    const { list } = this.props;

    const tmp = list[dragIndex];
    list[dragIndex] = list[dragIndex + offset];
    list[dragIndex + offset] = tmp;

    this.setState({
      dragIndex: dragIndex + offset,
    });
  }

  _onChange() {
    const { onChange, list } = this.props;
    onChange && onChange(Object.freeze(list));
  }
}
