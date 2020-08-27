import * as CryptoUtils from '../CryptoUtils';

it('Encrypted text should not be the same as original clear text - short text', () => {
  const key = 'dummyKey';
  const crypto = new CryptoUtils.CryptographyUtil(key);
  const shortText = '__ChuckIpsum__';
  expect(crypto.encrypt(shortText)).not.toEqual(shortText);
});

it('Encrypted text should not be the same as original clear text - JSON', () => {
  const key = 'dummyKey';
  const crypto = new CryptoUtils.CryptographyUtil(key);
  const jsonText = JSON.stringify({ prop1: 'test' });
  expect(crypto.encrypt(jsonText)).not.toEqual(jsonText);
});

it('Given the same key, the decyphered text should be the same as the original clear text - short text', () => {
  const key = 'dummyKey';
  const cryptoOne = new CryptoUtils.CryptographyUtil(key);
  const cryptoTwo = new CryptoUtils.CryptographyUtil(key);
  const shortText = '__ChuckIpsum__';
  const cypherText = cryptoOne.encrypt(shortText);
  expect(cryptoOne.decrypt(cypherText)).toEqual(shortText);
  expect(cryptoTwo.decrypt(cypherText)).toEqual(shortText);
});

it('Given the same key, the decyphered text should be the same as the original clear text - JSON', () => {
  const key = 'dummyKey';
  const cryptoOne = new CryptoUtils.CryptographyUtil(key);
  const cryptoTwo = new CryptoUtils.CryptographyUtil(key);
  const shortText = JSON.stringify({ prop1: 'test' });
  const cypherText = cryptoOne.encrypt(shortText);
  expect(cryptoOne.decrypt(cypherText)).toEqual(shortText);
  expect(cryptoTwo.decrypt(cypherText)).toEqual(shortText);
});
