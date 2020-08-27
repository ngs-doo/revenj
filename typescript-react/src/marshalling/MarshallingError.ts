export class MarshallingError extends Error {
  constructor(message: string) {
    super(message);

    const prototype = new.target.prototype;
    Object.setPrototypeOf(this, prototype);
  }
}
