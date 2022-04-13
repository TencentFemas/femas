import { showDialog } from '@src/common/helpers/showDialog';
import Page from './Page';
import PageDuck, { DialogOptions, Values } from './PageDuck';

export default function create(instance: Values, options: DialogOptions) {
  return new Promise(resolve => {
    showDialog(Page, PageDuck, function*(duck: PageDuck) {
      try {
        resolve(yield* duck.execute(instance, options));
      } finally {
        resolve(false);
      }
    });
  });
}
