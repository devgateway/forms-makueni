import URI from 'urijs';
import _ from 'lodash';

/**
 * Returns a function that will invoke `funcName` property on its argument
 * @param {Function} funcName
 * @returns {Function}
 */
export const callFunc = (funcName) => (obj) => obj[funcName]();

export const pluck = (fieldName) => (obj) => obj[fieldName];

export const pluckImm = (fieldName, ...args) => (imm) => imm.get(fieldName, ...args);

export const asPercent = (fr) => 100 * fr.toFixed(4);

export const fetchJson = (url) => fetch(url, { credentials: 'same-origin' }).then(callFunc('json'));

export function debounce(cb, delay = 200) {
  let timeout = null;
  return () => {
    if (timeout !== null) clearTimeout(timeout);
    timeout = setTimeout(cb, delay);
  };
}

export const toK = (number) => (number >= 1000
  ? `${Math.round(number / 1000)}K`
  : number);

export const identity = (value) => value;

/**
 * Takes two strings and an array of objects, returning on object whose keys are the values of the
 * first field and whose values are the values of the second field.
 * I guess an example would be more clear
 * fieldsToObj('field1', 'field2', [{
 *   field1: 'a',
 *   field2: '1'
 * }, {
 *   field1: 'b',
 *   field2: '2'
 * }])
 * // => {a: 1, b: 2}
 */
const fieldsToObj = (keyField, valueField, arr) => arr.reduce((obj, elem) => {
  // eslint-disable-next-line no-param-reassign
  obj[elem[keyField]] = elem[valueField];
  return obj;
}, {});

export const yearlyResponse2obj = fieldsToObj.bind(null, 'year');

export const monthlyResponse2obj = fieldsToObj.bind(null, 'month');

const shallowCompArr = (a, b) => a.every((el, index) => el === b[index]);

/**
 * Same as defaultMemoize from reselect. Equality check uses reference equality.
 */
export const cacheFn = (fn) => {
  let lastArgs;
  let lastResult;
  return (...args) => {
    if (!lastArgs || !shallowCompArr(lastArgs, args)) {
      lastArgs = args;
      lastResult = fn(...args);
    }
    return lastResult;
  };
};

export const max = (a, b) => (a > b ? a : b);

// takes and URI object and makes a POST request to it's base url and query as payload
export const send = (url) => fetch(url.clone().query(''), {
  method: 'POST',
  headers: {
    'Content-Type': 'application/x-www-form-urlencoded',
  },
  credentials: 'same-origin',
  body: url.query(),
});

export const isIE = navigator.appName === 'Microsoft Internet Explorer' || !!(navigator.userAgent.match(/Trident/)
    || navigator.userAgent.match(/rv 11/));

export const download = ({
  ep, filters, years, months, t,
}) => {
  let url = new URI(`/api/ocds/${ep}`)
    .addSearch(filters)
    .addSearch('year', years)
    // this sin shall be atoned for in the future
    .addSearch('language', localStorage.oceLocale || 'en_US');

  if (years.length === 1) {
    url = url.addSearch('month', months).addSearch('monthly', true);
  }

  return send(url).then((response) => {
    const { userAgent } = navigator;
    const isSafari = userAgent.indexOf('Safari') > -1 && userAgent.indexOf('Chrom') === -1;// excludes both Chrome and Chromium

    if (isSafari || isIE) {
      window.location.href = url;
      return response;
    }
    const [, filename] = response.headers.get('Content-Disposition').split('filename=');
    response.blob().then((blob) => {
      const link = document.createElement('a');
      link.href = URL.createObjectURL(blob);
      link.download = filename;
      document.body.appendChild(link);
      link.click();
    });
    return response;
  }).catch(() => {
    alert(t('export:error'));
  });
};

export const shallowCopy = (original) => {
  const copy = {};
  Object.keys(original).forEach((key) => { copy[key] = original[key]; });
  return copy;
};

export const arrReplace = (a, b, [head, ...tail]) => (typeof head === 'undefined'
  ? tail
  : [a === head ? b : head].concat(arrReplace(a, b, tail)));

export const range = (from, to) => (from > to ? [] : [from].concat(range(from + 1, to)));

export const fetchEP = (url) => fetch(url.clone().query(''), {
  method: 'POST',
  headers: {
    'Content-Type': 'application/x-www-form-urlencoded',
  },
  credentials: 'same-origin',
  body: url.query(),
}).then(callFunc('json'));

export const sameArray = (array1, array2) => array1.length === array2.length
  && _.difference(array1, array2).length === 0;

/**
 * wrapper for useImmer update call, that receives as imput the update function and also the input value
 * it always replaces the wrapped immer object with the input value. To be used with primitives or small
 * state objects that do not need partial updates.
 *
 * @param updateFunc
 * @returns {function(*): *}
 */
export const setImmer = (updateFunc) => (p) => updateFunc(() => p);
