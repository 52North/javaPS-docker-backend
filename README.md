# JavaPS Docker Backend Setup Guide

This page aims to guide developers on how to create and add new custom processes to JavaPS using Docker images.

# How can the javaPS Docker backend be extended with additional Docker-based processes?

To add a new processes to the javaPs Docker backend you need to create a JSON file that contains a process description and the docker image that contains your service. Then you need to add some configuration to enable the  javaPS Docker backend.

 javaPS Docker backend will parse the json file to get the image that you specified ,create a container form it and to create a dynamic description of your process.

## docker-backend

JavaPs depends on a docker-backend that is responsible for adding new services using docker containers. It automates the process of adding new services to the javaPs by parsing a json file that contains a description of the added service.

# **javaps-rest**

**javaps-rest** REST/JSON Binding for javaPS and responsible for the documentation of the Web service .it provides information dynamically through hypermedia concerning the existing services. and gets the required information from  the json file which contains the service description

## Service Description Example

``` json
{
  "processDescription": {
    "process": {
      "id": "org.n52.docker.StringReplace",
      "title": "StringReplace",
      "description": "this process replaces he wps in a text to a javaPs",
      "version": "1.0.0",
      "keywords": [
        "string"
      ],
      "inputs": [
        {
          "id": "source",
          "title":"Source input text",
          "description": "Source input text to be replaced",
          "minOccurs": 1,
          "maxOccurs": 1,
          "input": {
            "formats": [
              {
                "mimeType": "text/plain",
                "default": true
              }
            ]
          }
        }],
      "outputs": [
        {
          "id": "output",
          "title":  "text after processing",
          "output": {
            "formats": [
              {
                "mimeType": "text/plain",
                "default": true
              }
            ]
          }
        }
      ]
    },
    "jobControlOptions": [
      "sync-execute"
    ],
    "outputTransmission": [
      "value"
    ]
  },
  "immediateDeployment": true,
  "executionUnit": [
    {
      "unit": {
        "type": "docker",
        "image": "mohammedsaad/service:2.0"
      }
    }
  ],
  "deploymentProfileName": "http://www.opengis.net/profiles/eoc/dockerizedApplication"
}
```

so all what is required after creating the json file are the following steps of configration

- creating a DockerConfiguration that implements DockerEnvironmentConfigurer  as in the following example `@Value("${scihub.password}")`  will be included from the environment variables
- then Initial Algorithm Configuration that  implements TransactionalAlgorithmConfigurer  as in the following example

```java
package org.n52.javaps.eopad;

import org.n52.faroe.ConfigurationError;
import org.n52.javaps.docker.DockerEnvironmentConfigurer;
import org.n52.javaps.docker.Environment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
public class DockerConfiguration implements DockerEnvironmentConfigurer {
    private static final String SCIHUB_PASSWORD = "SCIHUB_PASSWORD";
    private static final String SCIHUB_USERNAME = "SCIHUB_USERNAME";

    private String username;
    private String password;

    @Value("${scihub.username}")
    public void setUsername(String username) {
        this.username = Objects.requireNonNull(username);
    }

    @Value("${scihub.password}")
    public void setPassword(String password) {
        this.password = Objects.requireNonNull(password);
    }

    @Override
    public void configure(Environment environment) {
        if (username == null || username.isEmpty() ||
            password == null || password.isEmpty()) {
            throw new ConfigurationError("missing SCIHUB credentials");
        }
        environment.put(SCIHUB_USERNAME, username);
        environment.put(SCIHUB_PASSWORD, password);
    }
}
```

you can add different processes using   `configuration.addAlgorithmFromResource(algorithmSource)`

```java
package org.n52.javaps.eopad;

import org.n52.javaps.transactional.TransactionalAlgorithmConfiguration;
import org.n52.javaps.transactional.TransactionalAlgorithmConfigurer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InitialAlgorithmConfiguration implements TransactionalAlgorithmConfigurer {

    private static final String NDVI
            = "https://raw.githubusercontent.com/52North/eopad-docker/master/ndvi/application-package.json";
    private static final String QUALITY
            = "https://raw.githubusercontent.com/52North/eopad-docker/master/quality/application-package.json";

    @Override
    public void configure(TransactionalAlgorithmConfiguration configuration) {
        configuration.addAlgorithmFromResource(NDVI).addAlgorithmFromResource(QUALITY);
    }
}
```

## Request/Response pattern

### Request:

- request:

```
POST <http://localhost:8080/rest/processes/org.n52.docker.StringReplace/jobs> HTTP/1.1
Content-Type: application/json
```

- request body:

```json
{
    "inputs":[
      {
          "id":"source",
          "input":{
            "format":{
                "mimeType":"text/plain"
            },
            "value": "The string 'WPS is the best' can be replaced"
          }
      }
    ],
    "outputs":[
      {
          "id":"output",
          "format":{
            "mimeType":"text/plain"
          },
          "transmissionMode":"value"
      }
    ]
}
```

### Response:

the response to the previous request will contain a URL with the job id in the response header .  

after requesting the result from the provided URL in the header the response pattern will look like:

```json
{
    "outputs":[
      {
          "id":"output",
          "value":{
            "inlineValue":"The string 'javaPS is the best' can be replaced"
          }
      }
    ]
}
```
