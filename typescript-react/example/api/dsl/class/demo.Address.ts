
import Role from '../security/Role';
import { IAddress as demoIAddress } from '../interface/demo.Address';
export class Address implements demoIAddress {
  public static domainObjectName: string = 'demo.Address';

  public static roles: Role[] = [
  ];




  street: string;
  zipCode: string;
  city: string;
  region: string;
  country: string;

  constructor(data: demoIAddress) {
	  this.street = data.street;
	  this.zipCode = data.zipCode;
	  this.city = data.city;
	  this.region = data.region;
	  this.country = data.country;
  }

}
