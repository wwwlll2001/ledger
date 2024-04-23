# Introduction
*******

### Structure

# Development

`Please note that: if you are using Windows, for all commands with ./gradlew, please use ./gradlew.bat instead`

### Build
To build the project, please run following command :
```
./gradlew clean build 

```
Or if you do not want to run test , just want to conduct build action, run below command:
```bash
./gradlew clean build -x test -x checkstyleMain
```

### Integration Test
To execute `Integration` tests, please run following command:
```
./gradlew test
```
##### Prerequisite
The integration test depends on the `test container` framework as the mock service, so please prepare the relevant 
images in advance.
The dependent images are listed as below:

- image tag: mysql:8.0.28, as the database for ledger service
- image tag: confluentinc/cp-kafka:7.5.2, as the message broker for ledger service
- image tag: elasticsearch:7.14.0, as the query database for ledger service

**The test containers are configured to be reused between multiple test executions to improve the test performance.
  To implement reusable test container, global test container properties need to be configured, run below command to 
config it before run test**

```bash
cp ${YOUR_PROJECT_DIRECTORY}/config/testcontainer.properties ${YOUR_HOME_DIRECTORY}/.testcontainer.properties
```

### Code Style

To check our code style, please use following command:
```
./gradlew checkstyleMain
```

### Run

#### Prerequisite

Ledger service depends on mysql, elasticsearch and kafka, to run the service , the convenient way is to leverage 
containerization. 
Here provide a docker-compose.yml under `deploy` folder which could be used to launch the whole runtime environment.
Again, in addition to prepare images mentioned above, below images should also be ready in advance.

- image tag: confluentinc/cp-zookeeper, as the kafka coordinator for ledger service
- image tag: kibana:7.14.0, as the data visualizer for ledger service if needed

Below is the guide for launching the runtime environment

1. **Build ledger image**:

```bash
cd ${YOUR_PROJECT_DIRECTORY}/
./gradlew clean build  
docker build -t ledger:0.1 .
```
You can run `./gradlew clean build -x test -x checkStyleMain` to build the jar without executing test

2. **Go to deploy folder**:
```bash
cd ${YOUR_PROJECT_DIRECTORY}/deploy
```
3. **Modify docker-compose parameters**:


    Open the docker-compose.yml, you need to replace the ${YOUR_HOST_IP} to the ip of your machine which will run leger 


4. **Run docker-compose to launch the runtime environment**:
```bash
docker-compose up -d     
```

5. **Check container status**:
```bash
docker-compose ps     
```
### Usage

**1. Access ledger by rest api**

Please check spring openapi doc at below url and try:

`http://localhost:8080/swagger-ui/index.html`

**Prerequisite**

_**Before try the api, please prepare some basic data at first so that the ledger could works properly, the data 
preparation scripts is under `deploy` folder which name is `init.sql`, execute the script in mysql before you try the 
apis.**_

_**Leger use `flyway` to manage the database migration, so after the ledger service started, all the tables have been 
created automatically, flyway scrips is under the folder ${YOUR_PROJECT_DIRECTORY}/src/main/resources/db/migration**_

_**Create below two topics on the kafka before accessing the ledger service.**_

- transactions
- public-transactions
- wallets

**2. Access ledger by message**

The message format of ledger adopt the `CloudEvent` for more standardized event format and enhanced interoperability, more information could check below link

`https://cloudevents.io/`

Transaction could be started not only by rest api but also by message, client can send below message to topic 
`public-transactions` to start transactions
```json
{
  "specversion" : "1.0",
  "type" : "REQUEST_START",
  "source" : "",
  "id" : "C234-1234-1234",
  "time" : "2024-04-15T17:31:00",
  "data" : [
     {
        "fromWalletId": 1,
        "toWalletId": 2,
        "amount": 50
     }
  ]
}
```

in above example, you should pay attention to two fields, type and data, type will be introduced later. The data is the 
actual message payload.

Also client can subscribe topic `public-transactions` to monitor the transaction event and do the further actions. The message 
payload format are described as below

```json
   {
   "id": 48,
   "fromWalletId": 1,
   "toWalletId": 2,
   "amount": 50,
   "status": "PROCESSING",
   "errorCode": null,
   "failedReason": null,
   "createdAt": "2024-04-22T17:40:54.64604",
   "updatedAt": "2024-04-22T17:40:54.646419"
}
```
Again, the whole message format is CloudEvent format, above format is just the format of data field of CloudEvent format.

**Client should monitor below type message**:

- `COMPLETED`: when ledger capture the message in type INIT, leger will handle the transaction and send the COMPLETED
message when finish the process. The ledger client should subscribe the message to get the transaction process result.

- `UPDATE_FAILED and UPDATE_SUCCESS`: after ledger finish updating transaction, the update result will be sent in these
two types. The ledger client should subscribe the message to get the update result.

### Tech Stack

- java 17
- spring boot 3.2.4
- spring cloud 2023.0.1
- mysql 8.0.28
- flyway
- kafka 7.5.2
- elasticsearch 7.14.0
- test container 1.19.7


