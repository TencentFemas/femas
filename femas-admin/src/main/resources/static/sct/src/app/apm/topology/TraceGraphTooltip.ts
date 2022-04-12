import { BIG_ICON_MAP } from './types';

const prefix = 'tea';

export function getCurServiceTooltip(data) {
  // position: static解决浏览器兼容问题，但失去了小三角样式
  return `<div class='tea-bubble tea-bubble--bottom' style='width: 120px;position: static;'>
  <div class='tea-bubble__inner'>
    <p>
      <span class='${prefix}-form__text tea-text-overflow'>
      当前服务
      </span>
    </p>
    <p style="padding: 2px 0;">
      <span class='${prefix}-form__text tea-text-overflow' title="${data.name}">
      ${data.name}
      </span>
    </p>
    ${
      !BIG_ICON_MAP[data.type]
        ? `<p class="${prefix}-form__text tea-text-overflow trace_text" style="padding: 2px 0;">
              <a href="javascript:;" data-service="${data.name}" class="service-cur-detail-btn">查看调用链</a>
            </p>`
        : ''
    }
  </div>
</div>
  `;
}
