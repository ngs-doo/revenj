import React from 'react';
import { createStore, combineReducers } from 'redux';
import { Provider } from 'react-redux';
import { Route } from 'react-router';
import {
  BrowserRouter,
  Redirect,
  Switch,
} from 'react-router-dom';
import { reducer as formReducer } from 'redux-form';
import { DslApplication } from 'revenj';

import { ForFakeUser } from './components/Auth';
import { FieldControls } from './components/Fields';
import { Loading } from './components/Loading';
import { Layout } from './components/Layout';
import { IPackageMixin } from './dsl/interface/demo.PackageMixin';
import { CreatePackage } from './pages/Package/Create';
import { ListPackages } from './pages/Package/List';
import { EditPackage, ViewPackage } from './pages/Package/ViewEdit';
import { api, onExport, ExportButton } from './util/Api';
import { notifyError } from './util/notify';
import * as validators from './util/validators';

import 'revenj/dist/index.css';
import 'react-toastify/dist/ReactToastify.css';

// No extra configuration
const emptyConfig = {};
const getS3FileUrl = (_s3: S3) => {
  throw new Error('S3 not supported in demo app');
}

const visibility = {
  hasReturnAddress: (values: IPackageMixin) => values?.returnAddress != null,
};

const store = createStore(combineReducers({
  form: formReducer,
}), (window as any).__REDUX_DEVTOOLS_EXTENSION__ && (window as any).__REDUX_DEVTOOLS_EXTENSION__());

const App = () => {
  return (
    <Provider store={store}>
      <DslApplication
        api={api}
        marshalling={emptyConfig}
        Fields={FieldControls}
        validators={validators}
        defaults={emptyConfig}
        visibility={visibility}
        getS3DownloadUrl={getS3FileUrl}
        notifyError={notifyError}
        ExportButton={ExportButton}
        onExport={onExport}
        LoadingComponent={Loading}
      >
        <BrowserRouter>
          <Layout>
            <ForFakeUser>
              <Switch>
                <Route path='/package/create' component={CreatePackage} />
                <Route path='/package/list' component={ListPackages} />
                <Route path='/package/:id/edit' component={EditPackage} />
                <Route path='/package/:id/dashboard' component={ViewPackage} />
                <Redirect to='/package/list' />
              </Switch>
            </ForFakeUser>
          </Layout>
        </BrowserRouter>
      </DslApplication>
    </Provider>
  );
};

export default App;
