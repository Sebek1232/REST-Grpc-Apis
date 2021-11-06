# REST Grpc APIs
# Sebastian Greczek
# AWS Deployment Video : https://youtu.be/LPIAiKEG-VA
## How to Run

``sbt clean compile run``

This will produce:

``Multiple main classes detected.`` 

``Select one to run:``

```[1] GrpcClient```

```[2] GrpcServer```

```[3] RestServer```

Now you can choose a number to run a specific main function. Alternately you can 
start a sbt server by typing ``sbt`` from there you can ``clean; compile``
Now while in the sbt server you can run 


``runMain GrpcServer`` : to Start Grpc Server

``runMain GrpcClient`` : to Start Grpc Client (must have grpc server running)

``runMain RestServer`` : to Start REST Server

## RESTful API Usage
After starting ``RestServer`` the server will listen on port 50051

### Get Request:
To make a get request you can use the curl command with a json string as parameter. The 
get api path is: `` ./api/logs/{"json string"}``

On a unix shell:

``curl http://localhost:50051/api/logs/'{"start": "20:37:03.324","interval": "5000"}'``

On a Windows shell:
``curl http://localhost:50051/api/logs/%7B%22start%22:%20%2220:37:03.324%22,%22interval%22:%20%225000%22%7D``

Windows requires ``" "`` and ``{}`` to be escaped. To get a valid Windows curl url you can 
put ```http://localhost:50051/api/logs/{"start": "val", "interval": "val"}``` into a web 
browser url and it will translate the url to a valid Windows shell url like above. 

### Post Request:
To make a get request you can use the curl command. The
get api path is: ``./api/logs/post``

On a unix shell:
``curl -d '{"start": "20:37:03.324","interval": "5000"}' -H 'Content-Type: application/json' http://localhost:50051/api/logs/post``

On a Windows Shell:
``curl -d "{\"start\": \"20:37:03.324\",\"interval\": \"5000\"}" -H 'Content-Type: application/json' http://localhost:50051/api/logs/post``

## RESTful Api Design

akka-http RESTful api based on tutorial from: https://www.codersbistro.com/blog/restful-apis-with-akka-http/

Files: ``LambdaController`` ``Rest Server``

The Rest Server acts as both a client and server. It can handle requests from clients and 
it acts as client to the AWS API Gateway in order to fetch information from lambda functions

LambdaController implements the parses and reads the url routes that a client can make such as 
GET and POST. It also has function that call AWS Lambda Functions with the parameters that
the client pass in. 

## Grpc Design
Uses ``scalapb`` library for GRPC based on grpc tutorial from scalapb docs: https://scalapb.github.io/docs/grpc/

``GrpcServer`` also acts as Server/Client. It handles requests from Grpc clients and also makes
request to AWS API to invoke lambda functions. awsApi.proto defines the Grpc that scalapb
compiles to make the files that allow processes to communicate with each other.

A ```GrpcClient``` passes parameters to the ``GrpcServer``. Then the server invokes 
aws Lambda functions using the AWS APIs. Once a reply is returned the server sends that 
reply back to the client that passed the parameters.

## AWS Lambda

Two functions are defined: ``doesTimeExist`` and ``findLogs``

Each function expects a json string. ``{"start": "00:00:00.000""}`` for ``doesTimeExist``
and

````{"start": "00:00:00.000"", "interval": "000"}```` for ``findLogs``

``doesTimeExist`` gets a log file from s3 storage and finds if passed time is present in
the log file 

``findLogs`` assumes that the start time exists. So ``doesTimeExist`` must be called before
``findLogs``. Retrieves file from s3 storage and performs a binary search to find the start 
time. Once found it will return all logs from start+interval.

Both functions are written in Node.js. They can be found in the project in the LambdaFunctions
directory. 









