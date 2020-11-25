# Backbase Plaid Proof of Concept
##NOTE: This is a Proof of Concept! No secure storage for access tokens. DO NOT USE IN PRODUCTION 

- This project makes use of Plaid a third party software to aggregate external accounts. It gets accounts from a core 
banking system through this third party and then ingests them into backbase dbs to be processed. This processed data can
then be displayed to a user through our banking app. 

- This is created for Navy Federal credit union initially. They wanted a system that integrated with Plaid, where plaid 
connects the institutions core bank to our DBS platform.
  
- It consists of several micro services, each handling a different aspect of the ingestion of account data.

### What is plaid
- SAAS, Fintech Provider offering a single API for developers for banks worldwide

### Scope
- Link external accounts with Plaid Link
- Ingest External Accounts using Backbase Stream
- Ingest Transactions for Linked Accounts
- Register Webhook for continuous updates
- Enrich Transactions
---
## Plaid Service

- Communicates with Plaid using Plaid clientId and secret
- Provides Plaid Link Services
- Persists Access Token
- Accepts Webhook Requests
- Ingests Arrangements & Transactions with Backbase Stream
### Sequence Diagram
[sequence diagram for plaid service](plaid-service/docs/plaid_sequence_diagram.puml)
### Services
- Access Token
  - Retrieves the access token for a users given Item
- Account
  - Maps and ingests accounts from plaid to Backbase BDS
- Institution
  - Maps and ingests the Institution from plaid to Backbase BDS
- Item
  - Manages Items 
- Plaid Link
  - Sets up a plaid link and creates an item
- Plaid Transactions
  - Maps and ingests accounts from plaid to Backbase BDS
- Webhook
  - Sets up a webhook and processes updates

---
### APIs
The APIs below can be found in the [plaid-api](plaid-api) directory
- plaid-client-api:\
endpoints set up for managing the linking and unlinking of user items to and from plaid
- plaid-service-api:\
endpoints for manging, invoking the enrichment of transactions and the retrieval of that categories used to enrich 
- plaid-webhook-api:\
endpoints used to invoke the actions of webhooks that are used for the maintenance and update of items
---
### Database Settings  
Database settings are used to set up and access the repositories where Plaid data is stored.
For the [application.yaml](plaid-service/src/main/resources/application.yml) files some local environment variables will
 need to be set. This is very easy, simply go to edit configurations in the mvn run dropdown and add the Environment 
 variables here with values for your datasource.
How to configure this for the Kubernetes manifests is explained below.
#### Configurations
The configurations for this are found in the Kubernetes-manifests directories in each project, this is where the 
plaid-configmap.yaml files can be found.
It is in these files that datasource url, driver class, username and plaform values are set. You must remember to set 
the in all the projects, here are the links to the files where they should be confiured:
- [plaid service config map](plaid-service/kubernetes-manifests/plaid-configmap.yaml)

There are already some example values set in here.\
You can fill them out with our own database credentials however any passwords for your database for privicy reasons 
should be stored in a secret key ref which will be explained in the next section. 
#### Secrets
Secret key refs are where you can map your passwords from, in these key refs the values are kept encoded and therefore 
secure.
You must make your own for this with your data source passwords saved in them. This is explained here:
 [creating kubernetes secret configs](https://kubernetes.io/docs/concepts/configuration/secret/#opaque-secrets), [managing secrets config file](https://kubernetes.io/docs/tasks/configmap-secret/managing-secret-using-config-file/)
The config file should look something like this in the end.
````
apiVersion: v1
kind: Secret
metadata:
  name: activemq-password
  namespace: exampleNamespace
type: Opaque
data:
  ACTIVEMQ_PASSWORD: examplePasswordEncrypted

````



