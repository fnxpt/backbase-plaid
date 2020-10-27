# Backbase Account eXternal Aggregation

```
                               
               /\_[]_/\        
              |] _||_ [|              
       ___     \/ || \/                                    
      /___\       ||                       /\              
     (|0 0|)      ||                      /  \   __  _____ 
   __/{\U/}\_ ___/vvv                    / /\ \  \ \/ / _ \
  / \  {~}   / _|_P|                    / ____ \  >  <  __/
  | /\  ~   /_/   []                   /_/    \_\/_/\_\___|
  |_| (____)                                               
  \_]/______\        -sj-          *** temporary ASCII ART ***                        
     _\_||_/_                  
kbv (_,_||_,_)            
                             
```

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
---
## Plaid Service

- Communicates with Plaid using Plaid clientId and secret
- Provides Plaid Link Services
- Persists Access Token
- Accepts Webhook Requests
- Ingests Arrangements & Transactions with Backbase Stream

### Services
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
  





