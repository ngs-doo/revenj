export const waitForDuration = (duration: number): Promise<void> =>
  new Promise((resolve) => setTimeout(resolve, duration));

export const waitForTick = () => waitForDuration(0);

// Returns a function, that, as long as it continues to be invoked, will not
// be triggered. The function will be called after it stops being called for
// N milliseconds. If `immediate` is passed, trigger the function on the
// leading edge, instead of the trailing.
export function debounce<A, R>(
  fn: (x: A) => R,
  duration: number,
  immediate?: boolean
): (x: A) => R;
export function debounce<A, B, R>(
  fn: (x: A, y: B) => R,
  duration: number,
  immediate?: boolean
): (x: A) => R;
export function debounce<A, B, C, R>(
  fn: (x: A, y: B, z: C) => R,
  duration: number,
  immediate?: boolean
): (x: A) => R;
export function debounce(
  func: IFunctionAny,
  wait: number,
  immediate?: boolean
) {
  let timeout: NodeJS.Timeout | undefined;
  return function (...args: any[]) {
    const later = () => {
      timeout = undefined;
      if (!immediate) {
        func.apply(undefined, args);
      }
    };
    const callNow = immediate && !timeout;
    clearTimeout(timeout!);
    timeout = setTimeout(later, wait);
    if (callNow) {
      func.apply(undefined, args);
    }
  };
}

export function throttle<A, R>(fn: (x: A) => R, duration: number): (x: A) => R;
export function throttle<A, B, R>(
  fn: (x: A, y: B) => R,
  duration: number
): (x: A) => R;
export function throttle<A, B, C, R>(
  fn: (x: A, y: B, z: C) => R,
  duration: number
): (x: A) => R;
export function throttle(fn: IFunctionAny, threshhold: number) {
  let last: number;
  let deferTimer: NodeJS.Timeout;
  return function (...args: any[]) {
    const now = Date.now();

    if (last && now < last + threshhold) {
      // hold on to it
      clearTimeout(deferTimer);
      deferTimer = setTimeout(() => {
        last = now;
        fn.apply(undefined, args);
      }, threshhold + last - now);
    } else {
      last = now;
      fn.apply(undefined, args);
    }
  };
}
