# P6 Deep Lynx Adapter

This repo is a Deep Lynx adapter for [Primavera P6](https://www.oracle.com/industries/construction-engineering/primavera-p6/). It synchronizes cost, scheduling, and resource data between projects in P6 to containers in [DeepLynx](https://github.com/idaholab/Deep-Lynx/wiki). The user provides a project Id for the adapter to target, and the adapter will automatically (on a specified interval) pull from P6 and import to DeepLynx the project's: activity data, activity relationships, and work breakdown structure (WBS). After this initial import to DeepLynx, the user should use DeepLynx's [typemapping](https://github.com/idaholab/Deep-Lynx/wiki/Type-Mapping) system create nodes in the user's DeepLynx container. The adapter will continue to regularly pull the specified P6 project's data into DeepLynx on the specified interval, and assuming the DeepLynx typemapping has been created, DeepLynx nodes will reflect any P6 data changes. New P6 activities will create new DeepLynx nodes, changed P6 activities will update the corresponding DeepLynx nodes, and deleted P6 activities will delete the corresponding nodes in DeepLynx.

## Instructions for Deployment
### System requirements
[Docker](https://docs.docker.com/get-docker/) and [`compose`](https://docs.docker.com/compose/).

### P6 Environment
This adapter is designed to extract data from a given Primavera P6 EPPM database which has had web services enabled. Instructions on how to enable web services can be found [here](https://docs.oracle.com/cd/F37125_01/English/Integration_Documentation/p6_eppm_web_services_programming/helpmain.htm?toc.htm?34309.htm).

### Configuration
In development
- change the file `.env-sample` to `.env`.
- set the variable `P6_ADAPTER_PORT` to any four-digit port value not allocated by the host (the value set in .env-sample works well).
- set the variable `P6_ADAPTER_URL` to the address where the adapter is hosted (the value set in .env-sample works well).
- set the value for the variable `P6_DB_LOC` (the value set in .env-sample works well).
- [generate a 128-bit](https://www.ibm.com/docs/en/imdm/12.0?topic=encryption-generating-aes-keys-password) key and assign it to the `P6_ENCRYPTION_KEY` variable.
- set the variables `P6_HOSTNAME` and `P6_IP_ADDRESS` if you know the values for the given P6 database that you would like to connect to, otherwise contact the developers for those values.
- set the variable `DL_URL` to the DeepLynx address that you wish to connect to.
- `DL_URL_INTERNAL`: if you are running DeepLynx locally, this needs to be set to `http://host.docker.internal:8090`; otherwise this should be set to `DL_URL`.
- Create a [DeepLynx OAuth app](https://github.com/idaholab/Deep-Lynx/wiki/DeepLynx-Enabled-OAuth-Application) and uses the generated key and secret to set `DL_APP_ID` and `DL_APP_SECRET`.


In production
- set the `P6_ADAPTER_PORT`, `P6_ADAPTER_URL`, `P6_DB_LOC`, `P6_ENCRYPTION_KEY`, `P6_HOSTNAME`, `P6_IP_ADDRESS`, `DL_URL`, `DL_URL_INTERNAL`, `DL_APP_ID`, and `DL_APP_SECRET` environment variables in the resource or pipeline through which the service is deployed.

#### Adapter Interval
The time interval that the adapter runs on can be adjusted with `fixed-rate.in.milliseconds` in `src\main\resources\application.properties`.

### Running
Once Docker and `compose` are installed and configuration variables are set, launch `docker-compose build` then `docker-compose up`. The service should be accessible on the port you've specified.

### Use
To use the adapter, a P6 data source should be created in DeepLynx.
![image](https://media.github.inl.gov/user/13/files/702b2d05-8183-4ad7-89ee-abf17550558f)

The following fields should be entered when creating this data source:
- `Endpoint` should look like this `http://p6-prd1-mw12c.inel.gov:8206/p6ws/services/`; the exact address and port will depend on where your P6 web services are hosted.
- `Project ID` is the Id for P6 project that you wish to connect to; it will be similar to this `SUFO.N-Z.23`.
- `Username` and `Password` are your P6 credentials which need `read` access to the P6 project that you entered in `Project ID`.

The adapter also needs to be authorized either through your DeepLynx container's UI, or by going to the following address in your browser `P6_ADAPTER_URL`+`/redirect/`+ your DeepLynx container ID (`http://localhost:8181/redirect/154` for example).

### Developer Endpoints
There are several P6 adapter endpoints defined in `P6Controller.java` that developers may find useful for interacting with the adapter.
    
### Interval Import Logic
- On some fixed rate interval, the adapter connects to the sqlite database p6.db
  - For each entry in the p6.db connections table:
    - the adapter gets the unique serviceUserKey/serviceUserSecret pair
    - the adapter creates a new DeepLynxService instance where it first tries to authenticate with the key/secret pair and then tries to get a list of accessible containers
      - if either the authentication fails or the list of accessible containers is empty then that entry in the p6.db connections table is deleted and the for loop moves to the next entry in the connections table
    - DeepLynxService uses DL’s “/import/datasources?decrypted=true” endpoint to get the datasource config info
      - For each datasource config info, a new ReadActivitiesWrapper instance is created and the importP6Data method is performed

### Debugging
A log file with adapter function information, caught exception messages, and possible error messages can be found in `src\sqlite\Log.txt`


## Contact

### Primary developers:
- Jaren Brownlee (jaren.brownlee@inl.gov)
- Jack Cavaluzzi (jack.cavaluzzi@inl.gov)
- Brennan Harris (brennan.harris@inl.gov)

### Feature requests or bugs
Please file an issue in [GitHub](https://github.inl.gov/Digital-Engineering/p6_deeplynx_adapter/issues) to report errors or request new features.
