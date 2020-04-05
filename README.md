shopping-cart
=============

![CI Status](https://github.com/Bunyod/PracticalFPinScala/workflows/Build/badge.svg)
[![MergifyStatus](https://img.shields.io/endpoint.svg?url=https://gh.mergify.io/badges/Bunyod/PracticalFPinScala&style=flat)](https://mergify.io)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-brightgreen.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org) <a href="https://typelevel.org/cats/"><img src="https://typelevel.org/cats/img/cats-badge.svg" height="40px" align="right" alt="Cats friendly" /></a>

See the [docker-compose.yml](app/docker-compose.yml) file for more details.

## Tests

To run Unit Tests:

```
sbt test
```

To run Integration Tests we need to run both `PostgreSQL` and `Redis`:


```
docker-compose up
sbt it:test
docker-compose down
```

## Build Docker image

```
sbt docker:publishLocal
```

Our image should now be built. We can check it by running the following command:

```
> docker images | grep shopping-cart
REPOSITORY                    TAG                 IMAGE ID            CREATED             SIZE
shopping-cart                 latest              646501a87362        2 seconds ago       138MB
```

To run our application using our Docker image, run the following command:

```
cd /app
docker-compose up
```

## Payments Client

The configured test payment client is a fake API that always returns 200 with a Payment Id. Users are encouraged to make modifications, e.g. return 409 with another Payment Id (you can create one [here](https://www.uuidgenerator.net/api/version1/1)) or any other HTTP status to see how our application handles the different cases.

This fake API can be modified at: [https://beeceptor.com/console/payments](https://beeceptor.com/console/payments)


TODO add project structure explanation