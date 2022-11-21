# P6 Deep Lynx Adapter

This repo is a Deep Lynx adapter for [Primavera P6](https://www.oracle.com/industries/construction-engineering/primavera-p6/). It synchronizes cost, scheduling, and resource data between projects in P6 to containers in Deep Lynx. Learn more about Deep Lynx [here](https://github.com/idaholab/Deep-Lynx/wiki)

## Instructions for Deployment
### System requirements
[Docker](https://docs.docker.com/get-docker/) and [`compose`](https://docs.docker.com/compose/)

### Configuration
In development
- change the file `.env-sample` to `.env`
- set the variable `P6_ADAPTER_PORT` to any four-digit port value not allocated by the host
- [generate a 128-bit](https://www.ibm.com/docs/en/imdm/12.0?topic=encryption-generating-aes-keys-password) key and assign it to the `P6_ENCRYPTION_KEY` variable

In production
- set the `P6_ADAPTER_PORT` and `P6_ENCRYPTION_KEY` environment variables in the resource or pipeline through which the service is deployed

### Running
Once Docker and `compose` are installed and configuration variables are set, launch `docker-compose build` then `docker-compose up`. The service should be accessible on the port you've specified 

### Use
The following endpoints will be used to interact with the adapter:

<table>
    <tr>
        <th>Endpoint</th><th>Method</th><th>Payload</th><th>Response</th>
    </tr>
    <tr>
        <td>/health</td><td>GET</td><td>(none)</td><td><pre>200 "OK"</pre></td>
    </tr>
    <tr>
        <td>/status</td><td>GET</td><td>(none)</td><td>
            <pre>{
    "connectionActive": "true|false"
}</pre>
        </td>
    </tr>
    <tr>
        <td>/configure</td><td>POST</td><td>
            <pre>{
    "deepLynxURL": "https://STRING.com",
    "deepLynxContainer": "STRING",
    "deepLynxDatasource": "STRING",
    "deepLynxApiKey": "STRING",
    "deepLynxApiSecret": "STRING",
    "p6URL": "https://STRING.com",
    "p6Project": "STRING",
    "p6Username": "STRING",
    "p6Password": "STRING"
}</pre>
        </td><td>
            <pre>{
    "sql_migration_success": "true|false",
    "sql_configuration_success": "true|false"
}</pre>
        </td>
    </tr>
    <tr>
        <td>/update</td><td>POST</td><td>
            <pre>{
    "deepLynxURL": "https://STRING.com",
    "deepLynxContainer": "STRING",
    "deepLynxDatasource": "STRING",
    "deepLynxApiKey": "STRING",
    "deepLynxApiSecret": "STRING",
    "p6URL": "https://STRING.com",
    "p6Project": "STRING",
    "p6Username": "STRING",
    "p6Password": "STRING"
}</pre>
        </td><td>
            <pre>{
    "sql_migration_success": "true|false",
    "sql_configuration_success": "true|false"
}</pre>
        </td>
    </tr>

<table>

Once valid configuration data has been passed into the adapter through the `/configure` endpoint, the adapter will start syncing data between the P6 project and the Deep Lynx container specified on a regular interval

### Debugging
Each access through the REST API creates a log in the sqlite table with a timestamp and log string




## Contact

### Primary developers:
- Jaren Brownlee (jaren.brownlee@inl.gov)
- Jack Cavaluzzi (jack.cavaluzzi@inl.gov)
- Brennan Harris (brennan.harris@inl.gov)

### Feature requests or bugs
Please file an issue in [GitHub](https://github.inl.gov/Digital-Engineering/p6_deeplynx_adapter/issues) to report errors or request new features.
