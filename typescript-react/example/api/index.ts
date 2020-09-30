/// <reference path="dsl/dsl.d.ts" />

import { json } from 'body-parser';
import express from 'express';
import * as path from 'path';

import { ICreatePackage } from './dsl/interface/demo.CreatePackage';
import { CreatePackage } from './dsl/class/demo.CreatePackage';
import { IEditPackage } from './dsl/interface/demo.EditPackage';
import { EditPackage } from './dsl/class/demo.EditPackage';
import { ILookupPackage } from './dsl/interface/demo.LookupPackage';
import { LookupPackage } from './dsl/class/demo.LookupPackage';
import { IMarkPackageDelivered } from './dsl/interface/demo.MarkPackageDelivered';
import { MarkPackageDelivered } from './dsl/class/demo.MarkPackageDelivered';
import { IMarkPackageInDelivery } from './dsl/interface/demo.MarkPackageInDelivery';
import { MarkPackageInDelivery } from './dsl/class/demo.MarkPackageInDelivery';
import { IMarkPackageReturned } from './dsl/interface/demo.MarkPackageReturned';
import { MarkPackageReturned } from './dsl/class/demo.MarkPackageReturned';
import { ISearchPackages } from './dsl/interface/demo.SearchPackages';
import { SearchPackages } from './dsl/class/demo.SearchPackages';
import { search, create, edit, lookup, markDelivered, markInDelivery, markReturned } from './model/domain';


const app = express();
const PORT = 8080;

app.use(json());

export const registerSubmitHandler = <T>(app: express.Application, name: string, handler: (command: T) => T) => {
  app.post<{}, T, T>(`/submit/${name}`, (req, res) => {
    try {
      res.json(handler(req.body));
    } catch (error) {
      console.error(error);
      res.status(400).json(error);
    }
  })

  app.post<{}, string, T>(`/export/:template/${name}`, (req, res) => {
    try {
      handler(req.body);
      res.download(path.resolve(__dirname, './export/dummy.xlsx'));
    } catch (error) {
      console.error(error);
      res.status(400).json(error);
    }
  });
}

registerSubmitHandler<ISearchPackages>(app, SearchPackages.domainObjectName, search);
registerSubmitHandler<ICreatePackage>(app, CreatePackage.domainObjectName, create);
registerSubmitHandler<IEditPackage>(app, EditPackage.domainObjectName, edit);
registerSubmitHandler<ILookupPackage>(app, LookupPackage.domainObjectName, lookup);
registerSubmitHandler<IMarkPackageInDelivery>(app, MarkPackageInDelivery.domainObjectName, markInDelivery);
registerSubmitHandler<IMarkPackageDelivered>(app, MarkPackageDelivered.domainObjectName, markDelivered);
registerSubmitHandler<IMarkPackageReturned>(app, MarkPackageReturned.domainObjectName, markReturned);

app.listen(PORT, () => {
  console.log(`Running on port ${PORT}`);
});
