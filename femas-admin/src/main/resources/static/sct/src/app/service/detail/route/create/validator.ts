import { nameTipMessage } from '@src/common/types';

export const validator = {
  // eslint-disable-next-line prettier/prettier
  ruleName: function (v) {
    if (!v) {
      return '路由规则名称不能为空。' + nameTipMessage;
    }
    if (v?.length > 60 || !/^[a-z0-9]([-_a-z0-9]*[a-z0-9])?$/.test(v)) {
      return nameTipMessage;
    }
  },
};
