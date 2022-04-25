import PageDuck from '../common/ducks/Page';
import { createToPayload, reduceFromPayload } from 'saga-duck';
import { delay } from 'redux-saga';
import { takeLatest } from 'redux-saga-catch';
import LoginFormDuck from './LoginFormDuck';
import { put, select } from 'redux-saga/effects';
import { resolvePromise } from 'saga-duck/build/helper';
import { authRequest } from '../common/util/apiRequest';
import { AuthenticationKey, defaultMenu } from '../config';
import resolvePath from '../common/util/resolvePath';

export default class LoginPageDuck extends PageDuck {
  get baseUrl() {
    return '/login';
  }

  get params(): this['Params'] {
    return [
      ...super.params,
      {
        key: 'backurl',
        route: 'backurl',
        order: -2,
        defaults: '',
        type: this.types.SET_BACKURL,
        selector: g => {
          return this.selector(g).backurl;
        },
      },
    ];
  }

  get quickDucks() {
    return {
      ...super.quickDucks,
      form: LoginFormDuck,
    };
  }

  get quickTypes() {
    enum Types {
      SET_BACKURL,
      LOGIN,
    }

    return {
      ...super.quickTypes,
      ...Types,
    };
  }

  get reducers() {
    const { types } = this;
    return {
      ...super.reducers,
      backurl: reduceFromPayload<string>(types.SET_BACKURL, ''),
    };
  }

  get rawSelectors() {
    type State = this['State'];
    return {
      ...super.rawSelectors,
      backurl: (state: State) => state.backurl,
    };
  }

  get creators() {
    const { types } = this;
    return {
      ...super.creators,
      setBackUrl: createToPayload<string>(types.SET_BACKURL),
      login: createToPayload<void>(types.LOGIN),
    };
  }

  *saga() {
    yield* super.saga();
    const {
      types,
      ducks: { form },
    } = this;
    const duck = this;
    yield takeLatest(types.LOGIN, function*() {
      yield delay(300);
      yield* duck.beforeSubmit();
      const { selectors } = form;
      const { username, password } = selectors.values(yield select());
      const { backurl } = duck.selector(yield select());
      const token = yield resolvePromise(
        authRequest<string>({
          action: 'login',
          data: {
            username: username,
            password: password,
          },
        }),
      );
      window.sessionStorage.setItem(AuthenticationKey, token);
      window.location.href = backurl || resolvePath(window['FEMAS_BASE_PATH'], 'femas/' + defaultMenu);
    });
  }

  *beforeSubmit() {
    const {
      ducks: { form },
    } = this;
    const { creators, selectors } = form;
    const invalid = selectors.firstInvalid(yield select());
    if (invalid) {
      yield put(creators.setAllTouched());
      throw invalid;
    }
  }
}
