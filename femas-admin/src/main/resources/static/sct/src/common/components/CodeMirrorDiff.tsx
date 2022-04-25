/* eslint-disable prettier/prettier */
import React from 'react';
import CodeMirror from 'codemirror'; //引入codeMirror
// 引入主题
import 'codemirror/theme/neat.css';
import 'codemirror/addon/merge/merge.css'; //引入codeMirror样式
import 'codemirror/addon/merge/merge.js';
import {MergeViewConfiguration} from 'codemirror/addon/merge/merge.js';
import * as d3 from 'd3';
import insertCSS from '../helpers/insertCSS';

insertCSS(
    'tsf-diff-section',
    `.tsf-diff-code .CodeMirror-merge-2pane{
  height: 100%;
  overflow: hidden;
}
.tsf-diff-code .CodeMirror-merge-2pane .CodeMirror-merge-pane{
  width: 49%;
}
.tsf-diff-code .CodeMirror-merge-2pane .CodeMirror-merge-gap{
  width: 2%;
}
.tsf-diff-code .CodeMirror-merge-scrolllock.CodeMirror-merge-scrolllock-enabled:after{
  content: '';
}
.tsf-diff-code .CodeMirror-dialog{
  opacity: 1 !important;
  position: relative;
}
.tsf-diff-code .CodeMirror-linewidget{
  background-color: #fff;
}
.tsf-diff-code .CodeMirror-merge-spacer{
  background-image: linear-gradient(-45deg, rgba(34, 34, 34, 0.2) 12.5%, #0000 12.5%, #0000 50%, rgba(34, 34, 34, 0.2) 50%, rgba(34, 34, 34, 0.2) 62.5%, #0000 62.5%, #0000 100% );
  background-size: 8px 8px;
  opacity: 0.5;
}
.tsf-diff-code .CodeMirror-merge-r-chunk-end{
  border-bottom: none;
}
.tsf-diff-code .CodeMirror-merge-r-chunk-start{
  border-top: none;
}
.tsf-diff-code .CodeMirror-merge-r-chunk{
  background: #F8E9EB;
}
.tsf-diff-code .CodeMirror-merge-right .CodeMirror-merge-r-chunk{
  background: #EFFCF0;
}
.tsf-diff-code .CodeMirror-merge-l-deleted, .tsf-diff-code .CodeMirror-merge-r-deleted{
  background: #dbe6c1;
}
.tsf-diff-code .CodeMirror-merge-r-inserted, .tsf-diff-code .CodeMirror-merge-l-inserted{
  background: #ffa3a2;
}`,
);

interface Props {
  /**
   * 原始文本
   */
  originValue: string;
  /**
   * 变更文本
   */
  value: string;
  /**
   * 编辑器高度
   *
   * @default 300
   */
  height?: React.CSSProperties['height'];
  /**
   * 编辑器options
   */
  options?: MergeViewConfiguration;
}

export default function CodeMirrorDiff(props: Props) {
  const {height = 300, originValue, value, options} = props;
  const ref = React.useRef<HTMLElement>(null);
  const initUI = () => {
    ref.current.innerHTML = ''; //每次dom元素的内容清空
    CodeMirror.MergeView(ref.current, {
      readOnly: true, //只读
      lineNumbers: true, // 显示行号
      theme: 'neat', //设置主题
      // 手动改方向
      value: originValue, //左边的内容（旧内容）
      orig: value, //右边的内容（新内容）
      mode: 'yaml',
      revertButtons: false,
      extraKeys: {'Ctrl-F': 'findPersistent', 'Cmd-F': 'findPersistent'},
      // connect: "align",
      ignoreWhitespace: true,
      ...options,
    } as MergeViewConfiguration);
    ref.current.addEventListener('keydown', function (event) {
      const {key, code, ctrlKey, metaKey} = event;
      const isCmd = metaKey || ctrlKey;
      if (!isCmd) {
        return;
      }
      const isF = key === 'f' || code === 'KeyF';
      if (!isF || !ref.current) {
        return;
      }
      const dialog = d3.select('section.tsf-diff-code div.CodeMirror-dialog-top');
      dialog.lower();
      const node = dialog?.node() as any;
      const height = node?.getBoundingClientRect()?.height;
      d3.select(node.parentNode)
      .select('.CodeMirror-scroll')
      .style('height', `calc(100% - ${height}px)`);
      // 搜索框聚焦
      (dialog.select('.CodeMirror-search-field').node() as any)?.focus();
      event.preventDefault();
    });
    ref.current.addEventListener('focusin', function () {
      // height还原
      d3.selectAll('.tsf-diff-code .CodeMirror-scroll').style('height', '100%');
    });
  };
  React.useEffect(() => {
    initUI();
    insertCSS(
        'tsf-diff-code',
        `.tsf-diff-code .CodeMirror-merge, .CodeMirror-merge .CodeMirror{
        height: ${height}px;
      }`,
    );
  }, [originValue, value, height]);

  return (
      <section style={{height}} className='tsf-diff-section'>
        <section className='tsf-diff-code' ref={v => (ref.current = v)}
                 style={{height: '100%'}}></section>
      </section>
  );
}
