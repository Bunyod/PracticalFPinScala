shopping-cart
=============

[![CI Status](https://travis-ci.org/Bunyod/PracticalFPinScala.svg?branch=master&style=flat)](https://travis-ci.org/Bunyod/PracticalFPinScala/)

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
docker-compose up
```

## Payments Client

The configured test payment client is a fake API that always returns 200 with a Payment Id. Users are encouraged to make modifications, e.g. return 409 with another Payment Id (you can create one [here](https://www.uuidgenerator.net/api/version1/1)) or any other HTTP status to see how our application handles the different cases.

This fake API can be modified at: [https://beeceptor.com/console/payments](https://beeceptor.com/console/payments)


## Architecture
### Domain Driven Design (DDD)
Domain driven design is all about developing a _ubiquitous language_, which is a language that you can use to discuss your software with business folks (who presumably do not know programming).

DDD is all about making your code expressive, making sure that how you _talk_ about your software materializes in your code.  One of the best ways to do this is to keep you _domain_ pure.  That is, allow the business concepts and entities to be real things, and keep all the other cruft out.  However, HTTP, JDBC, SQL are not essential to domain, so we want to _decouple_ those as much as possible.

### Onion (or Hexagonal) Architecture
In concert with DDD, the [Onion Architecture](https://jeffreypalermo.com/2008/08/the-onion-architecture-part-3/) and [Hexagonal Architecture from Cockburn](https://java-design-patterns.com/patterns/hexagonal/) give us patterns on how to separate our domain from the ugliness of implementation.

We fit DDD an Onion together via the following mechanisms:

**The domain package**
The domain package constitutes the things inside our domain.  It is deliberately free of the ugliness of JDBC, JSON, HTTP, and the rest. 
We use `Services` as coarse-grained interfaces to our domain.  These typically represent real-world use cases. Often times, you see a 1-to-1 mapping of `Services` to `R` or HTTP API calls your application surfaces.

Inside of the **domain**, we see a few concepts:

1. `Service` - the coarse grained use cases that work with other domain concepts to realize your use-cases
1. `Repository` - ways to get data into and out of persistent storage.  **Important: Repositories do not have any business logic in them, they should not know about the context in which they are used, and should not leak details of their implementations into the world**.
1. `payloads` or `models` - things like `Brand`, `Category`, `Item`, etc are all domain objects.  We keep these lean (i.e. free of behavior).

**The repository package**
The repository package is where the ugliness lives.  It has JDBC things, and the like.
it contains implementations of our `Repositories`.  We may have 3 different implementations, an in-memory version, skunk version as well as a **doobie** version.

**The http package**
It contains the HTTP endpoints that we surface via **http4s**.  You will also typically see JSON things in here via **circe**

**The util package**
The util package could be considered infrastructure, as it has nothing to do with the domain.

**NOTE**
All business logic is located in `domain` package, every package inside is related to some domain.

Service classes contains high level logic that relate to data manipulation,
that means that services MUST NOT implement storage.