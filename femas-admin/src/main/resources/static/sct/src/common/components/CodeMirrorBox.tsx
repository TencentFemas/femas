/* eslint-disable prettier/prettier */
import React from 'react';
import CodeMirror from '@uiw/react-codemirror';
// 引入codemirror核心css,js文件
import 'codemirror/lib/codemirror.css';
import 'codemirror/lib/codemirror.js';
// 代码模式 引入 yaml javascript  依赖
import 'codemirror/mode/yaml/yaml';
import 'codemirror/mode/javascript/javascript'; //js
//代码高亮
import 'codemirror/addon/selection/active-line';
// 检索
import 'codemirror/addon/scroll/annotatescrollbar.js';
import 'codemirror/addon/search/matchesonscrollbar.js';
import 'codemirror/addon/search/match-highlighter.js';
import 'codemirror/addon/search/jump-to-line.js';
import 'codemirror/addon/dialog/dialog.js';
import 'codemirror/addon/dialog/dialog.css';
import 'codemirror/addon/search/searchcursor.js';
import 'codemirror/addon/search/search.js';
// 引入主题
import 'codemirror/theme/neat.css';

import { Alert, Button, CodeEditorOptions, StyledProps, Text } from 'tea-component';
import * as d3 from 'd3';
import insertCSS from '../helpers/insertCSS';
import { isNumeric } from '../util/check';

insertCSS(
  'tsf-code-mirror',
  `.tsf-code-mirror .CodeMirror-dialog{
    opacity: 1 !important;
    position: relative;
}`,
);

interface Props extends StyledProps {
  /**
   * 编辑器文本变化回调
   */
  onChange?: (value: string) => void;
  /**
   * 编辑器失焦回调
   */
  onBlur?: () => void;
  /**
   * 编辑器高度
   *
   * @default 300
   */
  height?: React.CSSProperties['height'];
  /**
   * 编辑器宽度
   *
   * @default 100%
   */
  width?: React.CSSProperties['width'];
  /**
   * 编辑器文本
   */
  value?: string;
  /**
   * 编辑器options
   */
  options?: CodeEditorOptions;
}

const EditorNoTipKey = 'editor_no_tips';

const TIPS_HEIGHT = '40px';

const EditorTips = '当鼠标焦点在编辑器中，可以按下 Ctrl/CMD + F 进行搜索，按下 Ctrl/CMD + C 进行复制';
export default function CodeMirrorBox({ style, ...props }: Props) {
  const [noTip, setNoTip] = React.useState(() => localStorage[EditorNoTipKey] || false);
  const { height = 300, value, width, onChange, options = {}, onBlur } = props;
  const ref = React.useRef(null);
  const sectionRef = React.useRef(null);
  const editorHeight = React.useMemo(() => {
    let resHeight = height;
    if (height && !noTip) {
      if (isNumeric(height)) {
        resHeight = `${height}px`;
      }
      resHeight = `calc(${resHeight} - ${TIPS_HEIGHT})`;
    }

    return resHeight;
  }, [noTip]);
  const changeCode = instance => {
    onChange && onChange(instance.doc.getValue());
  };

  React.useEffect(() => {
    sectionRef.current.addEventListener('keydown', function (event) {
      const { key, code, ctrlKey, metaKey } = event;
      const isCmd = metaKey || ctrlKey;
      if (!isCmd) {
        return;
      }
      const isF = key === 'f' || code === 'KeyF';
      if (!isF || !ref.current?.editor) {
        return;
      }
      ref.current.editor.execCommand('findPersistent');
      const dialog = d3.select('section.tsf-code-mirror div.CodeMirror-dialog-top');
      if (!dialog) return;
      dialog.lower();
      const height = (dialog?.node() as any)?.getBoundingClientRect()?.height;
      d3.select(this)
        .select('.CodeMirror-scroll')
        .style('height', `calc(100% - ${height}px)`);

      // 搜索框聚焦
      (dialog.select('.CodeMirror-search-field')?.node() as any)?.focus();
      event.preventDefault();
      event.stopPropagation();
    });
    sectionRef.current.addEventListener('mousedown', function (event) {
      // 禁止冒泡，修复drawer close异常问题
      event.stopPropagation();
    });
  }, []);

  return (
    <section
      className='editor-section'
      style={{
        border: '1px solid #ddd',
        position: 'relative',
        ...style,
        height,
        width,
      }}
    >
      {!noTip && (
        <Alert
          hideIcon
          extra={
            <Button
              type='link'
              title={'您将不会再看到该提示'}
              onClick={() => {
                localStorage[EditorNoTipKey] = true;
                setNoTip(true);
              }}
            >
              {'不再显示'}
            </Button>
          }
          style={{ marginBottom: 0, padding: '10px 20px', width: props.width }}
        >
          <Text>{EditorTips}</Text>
        </Alert>
      )}
      <section
        ref={r => (sectionRef.current = r)}
        style={{ height: editorHeight, overflow: 'hidden' }}
        className='tsf-code-mirror'
      >
        <CodeMirror
          ref={r => (ref.current = r)}
          value={value || ''}
          options={{
            lineNumbers: true, // 显示行号
            theme: 'neat', // 设置主题
            readOnly: false, // 只读(有用)
            mode: 'yaml',
            ...options,
          }}
          onChange={changeCode}
          onFocus={() => {
            d3.selectAll('.tsf-code-mirror .CodeMirror-scroll').style('height', '100%');
            ref.current?.editor && ref.current?.editor.execCommand('clearSearch');
          }}
          onBlur={() => onBlur && onBlur()}
        />
      </section>
    </section>
  );
}
