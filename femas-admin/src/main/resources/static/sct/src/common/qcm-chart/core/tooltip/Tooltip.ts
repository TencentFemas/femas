export const generateLineTooltip = (labels, data, colors, additionalTip = [], showSplitLine = false) => {
  return `<div class="tea-chart-tooltip" style="position: absolute; z-index: 99; background: rgb(255, 255, 255); white-space: nowrap; box-shadow: rgba(0, 0, 0, 0.15) 0px 4px 8px 0px; right: unset;">
    <div style="padding: 15px; padding-bottom: 3px;">
    ${labels.length === 1 ? `<h4 style="font-size:14px;color:#333;padding-bottom:10px">${labels[0]}</h4>` : ''}
    ${data
      .map((d, i) =>
        d && d.value !== null
          ? `<div style="padding: 1px 0;margin-bottom: 5px;">
            ${labels.length > 1 ? `<h4 style="font-size:14px;color:#333;padding-bottom:10px">${labels[i]}</h4>` : ''}
            <p style="height:20px;line-height:20px;display:table-row;display: inline-table;width: 100%;" key="${i}">
              <span style="display:table-cell;line-height:20px;vertical-align:middle;padding-right:10px;width: 18px;">
                <i style="display:inline-block;height:4px;width:16px;background:${colors[d.name]};"></i>
              </span>
              <span style="display:table-cell;font-size:14px;color:#333;">${d.tipName || d.name}</span>
              <span style="display:table-cell;font-weight:600;color:#333;font-size:14px;padding-left:15px;text-align:right;">${d.tipValue ??
                d.value} ${d.unit ? d.unit : ''}</span>
            </p>
            ${d.additionalTip ? addtionalTipRender(d.additionalTip) : ''}
          </div>`
          : null,
      )
      .join('')}
    </div>
    ${addtionalTipRender(additionalTip, showSplitLine)}
  </div>`;
};

export const generatePieTooltip = (data, colors) => {
  return `<div class="tea-chart-tooltip" style="position: absolute; padding: 15px; z-index: 99; background: rgb(255, 255, 255); white-space: nowrap; box-shadow: rgba(0, 0, 0, 0.15) 0px 4px 8px 0px; right: unset;">
    <h4 style="font-size:14px;color:#333;padding-bottom:10px">${data.tipLabel ?? data.label}</h4>
    <div style="display:table;">
      <p style="height:20px;line-height:20px;display:table-row;">
        <span style="display:table-cell;line-height:20px;vertical-align:middle;padding-right:10px;">
          <i style="display:inline-block;height:4px;width:16px;background:${colors[data.label]};"></i>
        </span>
        <span style="display:table-cell;font-weight:600;color:#333;font-size:14px;text-align:right;">${data.tipValue ??
          data.value} ${data.unit ? data.unit : ''}</span>
      </p>
      ${data.additionalTip ? addtionalTipRender(data.additionalTip) : ''}
    </div>
  </div>`;
};

export const generateBarTooltip = (data, colors, additionalTip = [], formateBarColor, showSplitLine = false) => {
  return `<div class="tea-chart-tooltip" style="position: absolute;  z-index: 99; background: rgb(255, 255, 255); white-space: nowrap; box-shadow: rgba(0, 0, 0, 0.15) 0px 4px 8px 0px; right: unset;">
    <h4 style="font-size:14px;color:#333;padding: 10px 15px;padding-bottom:0">${data?.value?.[0]?.tipName ??
      data.convertedName}</h4>
    <div style="display:table;padding: 5px 15px;">
      ${data.value
        .map(
          (
            d,
            i,
          ) => `<div style="padding: 1px 0;"><p style="height:20px;line-height:20px;display:table-row;display: inline-table;width: 100%;" key="${i}">
        <span style="display:table-cell;line-height:20px;vertical-align:middle;padding-right:10px;width: 18px;">
          <i style="display:inline-block;height:4px;width:16px;background:${
            formateBarColor ? formateBarColor(d) : colors[d.label]
          };"></i>
        </span>
        <span style="display:table-cell;font-size:14px;color:#333;">${d.tipLabel ?? d.label}</span>
        <span style="display:table-cell;font-weight:600;color:#333;font-size:14px;padding-left:15px;text-align:right;">${d.tipValue ??
          d.value} ${d.unit ? d.unit : ''}</span>
      </p>
      ${d.additionalTip ? addtionalTipRender(d.additionalTip) : ''}
      </div>`,
        )
        .join('')}
    </div>
    ${addtionalTipRender(additionalTip, showSplitLine)}
  </div>`;
};

export const generateScatterTooltip = (data, color, additionalTip = [], showSplitLine = false) => {
  return `<div class="tea-chart-tooltip" style="position: absolute; z-index: 99; background: rgb(255, 255, 255); white-space: nowrap; box-shadow: rgba(0, 0, 0, 0.15) 0px 4px 8px 0px; right: unset;">
    <div style="display:table; padding: 15px; padding-bottom: 3px;">
      <div style="padding: 1px 0;margin-bottom: 5px;">
        <h4 style="font-size:14px;color:#333;padding-bottom:10px">${data.label}</h4>
        <p style="height:20px;line-height:20px;display:table-row;display: inline-table;width: 100%;">
          <span style="display:table-cell;line-height:20px;vertical-align:middle;padding-right:10px;width: 18px;">
            <i style="display:inline-block;height:10px;width:10px;background:${color};border-radius:50%;vertical-align:middle;"></i>
          </span>
          <span style="display:table-cell;font-size:14px;color:#333;">${data.tipName || data.name}</span>
          <span style="display:table-cell;font-weight:600;color:#333;font-size:14px;padding-left:15px;text-align:right;">${data.tipValue ??
            data.value} ${data.unit ? data.unit : ''}</span>
        </p>
        ${data.additionalTip ? addtionalTipRender(data.additionalTip) : ''}
      </div>
    </div>
    ${addtionalTipRender(additionalTip, showSplitLine)}
  </div>`;
};

const addtionalTipRender = (additionalTip, showBorder = false) => {
  if (typeof additionalTip === 'string') {
    return `<p style="height:17px;line-height:15px;padding-top: 10px;font-size:14px;color:#333;">${additionalTip}</p>`;
  }
  const content = additionalTip
    .map(
      (tip, i) =>
        `<p style="height:17px;line-height:15px;margin-top: 6px;font-size:14px;color:#333;" key=${i}>${tip}</p>`,
    )
    .join('');

  return showBorder
    ? `<div style='border-top: 1.5px solid #E5E5E5; padding: 6px 15px;'>
    ${content}
    </div>`
    : `<div style="padding: 6px 15px; margin-top: -10px">${content}</div>`;
};

export const generateCustomTooltip = (formatter, data, colors, colorMap = {}) => {
  return `<div class="tea-chart-tooltip" style="position: absolute; padding: 15px; z-index: 99; background: rgb(255, 255, 255); white-space: nowrap; box-shadow: rgba(0, 0, 0, 0.15) 0px 4px 8px 0px; right: unset;">
      ${formatter(data, colors, colorMap)}
  </div>`;
};

export const generateBrushTooltip = (formatter, xValues, yValues, mx, my) => {
  return `<div class="tea-chart-tooltip" style="position: absolute; padding: 15px; z-index: 98; background: rgb(255, 255, 255); white-space: nowrap; box-shadow: rgba(0, 0, 0, 0.15) 0px 4px 8px 0px; right: unset;">
      ${formatter(xValues, yValues, mx, my)}
  </div>`;
};
