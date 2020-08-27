import aes from 'crypto-js/aes';
import core from 'crypto-js/core';

export class CryptographyUtil {
  private key: string;

  constructor(encryptionKey: string) {
    this.key = encryptionKey;
  }

  encrypt(clearText: string): string {
    return aes.encrypt(clearText, this.key).toString();
  }

  decrypt(cypherText: string): string {
    return aes.decrypt(cypherText, this.key).toString(core.enc.Utf8);
  }
}
