import {
  initialize as initMarshalling,
  IBootConfig,
  Marshaller,
} from '../marshalling';

export type { IBootConfig };

export interface IApiService {
  onSubmit: <T>(nameWithModule: string, domainObject: T) => Promise<T>;
}

class ApplicationConfiguration {
  public readonly marshaller: Marshaller;
  constructor(
    public readonly api: IApiService,
    marshallerBootConfig: IBootConfig,
  ) {
    this.marshaller = initMarshalling(marshallerBootConfig);
  }
}

let config: ApplicationConfiguration | undefined;

export const initializeApplication = (
  api: IApiService,
  marshallerBootConfig: IBootConfig,
): ApplicationConfiguration => {
  config = new ApplicationConfiguration(api, marshallerBootConfig);
  return config;
}

export const getApplicationConfiguration = (): ApplicationConfiguration => {
  if (config == null) {
    throw new Error('Application was not configured correctly. Did you wrap your app into `<DslApplication>` from \'revenj\'?');
  }

  return config;
}
