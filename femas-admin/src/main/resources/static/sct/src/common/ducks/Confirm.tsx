import * as React from 'react';
import Base from './DialogPure';
import { showDialog } from '../helpers/showDialog';
import ConfirmDialog from '../duckComponents/Confirm';
import { DialogProps } from '../duckComponents/Dialog';

export interface Data extends Omit<DialogProps, 'defaultSubmitText' | 'defaultCancelText'> {
  content?: React.ReactChild;
  yesText?: string;
  noText?: string;
}

interface ConfirmParam<TSubmitData, TData> extends Data {
  fn?: (submitData: TSubmitData, data: TData) => any;
}

export default class Confirm extends Base {
  Data: Data;

  static show({ fn = () => {}, ...rest }: ConfirmParam<Confirm['SubmitData'], Confirm['Data']>) {
    return new Promise(resolve => {
      showDialog(ConfirmDialog, Confirm, function*(duck) {
        const result = yield duck.show(rest, fn);
        resolve(!!result);
      });
    });
  }

  /**
   * 确认框
   * @param {*} param
   * @return {SagaEffect<confirmed>}
   */
  confirm({ size = 'm', fn, ...rest }: ConfirmParam<this['SubmitData'], this['Data']>) {
    return this.show(
      {
        size,
        ...rest,
      } as this['Data'],
      fn,
    );
  }
}
