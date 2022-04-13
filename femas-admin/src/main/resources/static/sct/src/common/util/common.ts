/* eslint-disable prettier/prettier */

// 为node节点或master节点生成id
export function genIdForNode() {
  let d = new Date().getTime();
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    const r = (d + Math.random() * 16) % 16 | 0;
    d = Math.floor(d / 16);
    return (c === 'x' ? r : (r & 0x3) | 0x8).toString(16);
  });
}

//polyfill closest方法
export function getClosest(element: any, selector: string) {
  const matchesSelector =
      element.matches || element.webkitMatchesSelector || element.mozMatchesSelector || element.msMatchesSelector;
  if (matchesSelector) {
    while (element) {
      if (matchesSelector.call(element, selector)) {
        break;
      }
      element = element.parentElement;
    }
    return element;
  }
  return null;
}

export function isObject(obj) {
  return obj === Object(obj);
}

export function changeLetterLowCase(obj) {
  if (Array.isArray(obj)) {
    obj.forEach(function (item, i, arr) {
      if (isObject(item) || Array.isArray(item)) {
        arr[i] = changeLetterLowCase(item);
      }
    });
  } else if (isObject(obj)) {
    for (const prop in obj) {
      if (obj.hasOwnProperty(prop)) {
        let newProp = prop;
        const firstLetter = prop.substr(0, 1);
        newProp = firstLetter.toLowerCase() + prop.slice(1);

        if (newProp !== prop) {
          obj[newProp] = JSON.parse(JSON.stringify(obj[prop]));
          delete obj[prop];
        }
        if (isObject(obj[newProp]) || Array.isArray(obj[newProp])) {
          obj[newProp] = changeLetterLowCase(obj[newProp]);
        }
      }
    }
  }
  return obj;
}
