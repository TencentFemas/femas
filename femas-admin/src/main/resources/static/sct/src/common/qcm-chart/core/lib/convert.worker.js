/* eslint-disable prettier/prettier */
const workercode = () => {
  self.onmessage = function (e) {
    const data = JSON.parse(e.data);
    if (data && data.type === 'scatter') {
      postMessage(convertScatterData(data));
      return;
    }
    postMessage(convertLineData(data));
  };

  function convertDate(date) {
    const dateString = date.replace(/\-/g, '/');
    return new Date(dateString);
  }

  function convertLineData(props) {
    const data = props.data;
    const isTimeAxis = props.isTimeAxis === null || props.isTimeAxis === undefined ? true : props.isTimeAxis;
    const isCompare = props.isCompare;
    const needDoubleYAxis = props.needDoubleYAxis;
    const labels = [];
    if (data.length > 1) {
      data.forEach(d => {
        d.value.forEach(v => {
          isTimeAxis && labels.push(v.label);
          v.yIndex = d.yIndex || 0;
        });
      });
      const axisLabel = isTimeAxis
        ? Array.from(
            new Set(
              labels.sort(function (a, b) {
                return convertDate(a) > convertDate(b) ? 1 : -1;
              }),
            ),
          )
        : labels;

      !isCompare &&
        !needDoubleYAxis &&
        data.forEach(d => {
          axisLabel.forEach(label => {
            let exist = false;
            d.value.forEach(v => {
              if (exist) {
                return;
              }
              if (v.label === label) {
                exist = true;
              }
            });
            if (!exist) {
              d.value.push({
                label,
                value: null,
                yIndex: d.yIndex || 0,
              });
            }
          });
        });
    }

    // 数据排序
    // 为了保证数据hover效果的准确性，序数比例尺也需要进行排序
    data.forEach(d => {
      d.value = d.value.sort(function (a, b) {
        return (isTimeAxis ? convertDate(a.label) > convertDate(b.label) : a.label > b.label) ? 1 : -1;
      });
    });

    if (isTimeAxis) {
      data.forEach(d => {
        d.value.forEach(v => {
          v.label = convertDate(v.label);
        });
      });
    }
    return data;
  }

  function convertScatterData(props) {
    const data = props.data;
    const isTimeAxis = props.isTimeAxis === null || props.isTimeAxis === undefined ? true : props.isTimeAxis;
    const labels = [];
    if (data.length > 1) {
      data.forEach(d => {
        d.value.forEach(v => {
          isTimeAxis && labels.push(v.label);
          v.yIndex = d.yIndex || 0;
        });
      });
    }

    // 数据排序
    // 为了保证数据hover效果的准确性，序数比例尺也需要进行排序
    data.forEach(d => {
      d.value = d.value.sort(function (a, b) {
        return (isTimeAxis ? convertDate(a.label) > convertDate(b.label) : a.label > b.label) ? 1 : -1;
      });
    });
    if (isTimeAxis) {
      data.forEach(d => {
        d.value.forEach(v => {
          v.label = convertDate(v.label);
        });
      });
    }
    return data;
  }
};

let code = workercode.toString();
code = code.substring(code.indexOf('{') + 1, code.lastIndexOf('}'));

const blob = new Blob([code], { type: 'application/javascript' });
const workerContent = URL.createObjectURL(blob);

module.exports = workerContent;
