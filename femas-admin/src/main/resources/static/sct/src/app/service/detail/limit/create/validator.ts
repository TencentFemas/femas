import { nameTipMessage } from '@src/common/types';
import { descriptionKey, durationQuotaKey, durationSecondKey, nameKey } from '../types';

const numberTipMessage = '请输入正整数或正浮点数';

export const validator = {
  [nameKey](v) {
    if (!v) {
      return '不能为空。' + nameTipMessage;
    }
    if (v?.length > 60 || !/^[a-z0-9]([-_a-z0-9]*[a-z0-9])?$/.test(v)) {
      return nameTipMessage;
    }
  },
  [durationSecondKey](v) {
    if (v === null) {
      return '不能为空。' + numberTipMessage;
    }
    if (!/^[1-9][0-9]*$/.test(v)) {
      return numberTipMessage;
    } else if (v > 3600) {
      return '单位时间范围限制为1s(秒)到1h(时)之间哦';
    }
  },
  [durationQuotaKey](v) {
    if (v === null) {
      return '不能为空。' + numberTipMessage;
    }
    if (!/^[1-9][0-9]{0,7}$/.test(v)) {
      return '请求数数值范围：1~ 99999999（8位）';
    }
  },
  [descriptionKey](v) {
    if (!v) {
      return;
    }
    if (v.length > 200) {
      return '不能超过200个字符';
    }
  },
};
