/* eslint-disable prettier/prettier */
import Base from '@src/common/ducks/FormDialog';
import SpanDetail from './SpanDetail';
import BaseForm from '@src/common/ducks/Form';
import {Annotation, ANNOTATION_MAP, ANNOTATION_SORT, Instance as Span, SPAN_TYPE} from '../types';
import {showDialog} from '@src/common/helpers/showDialog';

export interface Data {
  title: string;
  data: Array<Span>;
  client?: Span;
  server?: Span;
  annotationList?: Array<Annotation>;
  originData?: string;
}

const sortByKey = data => {
  data?.sort(function (a, b) {
    return a.key.localeCompare(b.key);
  });
};

export default class SpanDetailDuck extends Base {
  get Form() {
    return Form;
  }

  get tipError() {
    return true;
  }

  static show(instance: Data) {
    return new Promise(res => {
      showDialog(SpanDetail, SpanDetailDuck, function* (duck) {
        // 获取span详情
        const detail = instance.data as Array<any>;
        const client = detail?.find(x => {
          return x.type === SPAN_TYPE.client;
        });
        const server = detail?.find(x => {
          return x.type === SPAN_TYPE.server || x.type === SPAN_TYPE.local;
        });
        sortByKey(client?.tags);
        sortByKey(server?.tags);
        sortByKey(client?.baggages);
        sortByKey(server?.baggages);

        instance.client = client;
        instance.server = server;
        instance.originData = JSON.stringify(detail, null, 2);

        const annotationList = [] as Array<Annotation>;

        const clientCommon = {
          duration: +client?.duration,
          timestamp: +client?.startTime,
        };
        const serverCommon = {
          duration: +server?.duration,
          timestamp: +server?.startTime,
        };

        // 阶段耗时
        if (client) {
          annotationList.push({value: ANNOTATION_MAP.cs, ...clientCommon});
          annotationList.push({value: ANNOTATION_MAP.cr, ...clientCommon});
        }
        if (server) {
          annotationList.push({value: ANNOTATION_MAP.sr, ...serverCommon});
          annotationList.push({value: ANNOTATION_MAP.ss, ...serverCommon});
        }
        instance.annotationList = annotationList.sort(function (a, b) {
          return ANNOTATION_SORT.indexOf(a.value) - ANNOTATION_SORT.indexOf(b.value);
        });

        res(yield duck.show(instance, function* () {
        }));
      });
    });
  }

  * onSubmit() {
  }
}

const validator = BaseForm.combineValidators<{}>({});

class Form extends BaseForm {
  validate(v) {
    return validator(v);
  }
}
