import * as React from 'react';
import { Bubble, Icon } from 'tea-component';

const progressValueStyle = {
  minWidth: '1px',
  cursor: 'pointer',
  marginTop: 0,
  borderRadius: 10,
};

export const getDurationRender = (record, showSpanDetail) => {
  const theme = record.isError ? 'is-danger' : 'is-blue ';
  const clientProgressStyle = {};
  const serverProgressStyle = {};

  // 计算client
  const client = record.isClient ? record : record.client;
  calcuteProgress(client, clientProgressStyle);
  // 计算server
  const server = record.isClient ? record.server : record;
  calcuteProgress(server, serverProgressStyle);

  const serverDur = +server?.timestamp + +server?.duration || 0;
  const clientDur = +client?.timestamp + +client?.duration || 0;

  const showError = server?.timestamp - client?.timestamp < 0 || (client && clientDur - serverDur < 0);

  return (
    <div onClick={showSpanDetail}>
      <span className='_tsf-line'></span>
      {client && (
        <Bubble content={`客户端耗时: ${client.duration} ms`}>
          <div
            className={`_tsf-progress ${theme}`}
            style={{
              ...progressValueStyle,
              ...clientProgressStyle,
              background: record.isError && !server ? '#e54545' : '#A5E06C',
              top: 13,
              height: 16,
            }}
          >
            {showError && serverDur <= clientDur && (
              <Bubble content='您的机器时间戳可能出现问题，这将会影响到当前页面的展示效果。建议您同步相关机器的时间戳。'>
                <Icon type='error' style={{ marginLeft: 5, marginTop: -5 }} className='_tsf-progress-num' />
              </Bubble>
            )}
          </div>
        </Bubble>
      )}
      {server && (
        <Bubble content={`服务端耗时: ${server.duration} ms`}>
          <div
            className={`_tsf-progress ${theme}`}
            style={{
              ...progressValueStyle,
              ...serverProgressStyle,
              height: client ? 12 : 16,
              top: client ? -1 : 15,
              borderRadius: client ? 5 : 10,
            }}
          >
            {showError && serverDur > clientDur && (
              <Bubble content='您的机器时间戳可能出现问题，这将会影响到当前页面的展示效果。建议您同步相关机器的时间戳。'>
                <Icon type='error' style={{ marginLeft: 5, marginTop: -8 }} className='_tsf-progress-num' />
              </Bubble>
            )}
          </div>
        </Bubble>
      )}
    </div>
  );
};

const calcuteProgress = (record, style) => {
  if (!record) return;
  if (Number(record.beginRatio) < 0) {
    //如果子span的开始时间小于父span的开始时间，用黄色标识
    style.marginLeft = 0;
  } else if (Number(record.beginRatio) + Number(record.durationRatio) > 1) {
    // 超出长度, 也用黄色标识
    style.width = `${(1 - record.beginRatio) * 100}%`;
  } else {
    // 正常
    style.width = `${record.durationRatio * 100}%`;
    style.marginLeft = `${record.beginRatio * 100}%`;
  }
};
