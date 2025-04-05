to run:
1. run docker
```shell
docker compose up -d --remove-orphans
```
2. to check for kafka, go to http://localhost:8080
3. to check gRPC messaging, go to localhost:8091 with gRPC-supporting sending API (e.g., Postman)

after running the app, don't forget to shut down the running containers:
```shell
docker compose stop 
```