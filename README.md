# Minesweeper API

## [API specification link](http://155.138.208.11:8080/v1/documentation)

* Base URL for all endpoints: http://155.138.208.11:8080/v1

## Overview

* Java 11/[Vert.X](https://vertx.io/) based service with a classic layered architecture which distributes responsibilities across Controllers, Services and Repositories.
Decided to use Vert.X to take advantage of its asynchronous capabilities and resource efficiency.
* Also decided to use MongoDB as persistence engine (in-memory for this instance) in order to take advantage of its performance, scalability and flexibility to interact with stored documents that may be highly accessed in short time periods.
* Regarding the data modelling, I decided to create an entity Game with a map (Map<String, Cell>) of cells where each key identifies the unique position of the cell in the board, for example 6:10 which means row 6 and column 10.
I found this approach convenient for being able to make access to cell more performant by using the map indexing by key.
* Implemented a very basic user management logic where users must identify themselves by providing their email before starting to play and application responds with an Authorization token which expires in a day.
  The authorization token is intended to be provided by header ("Authorization") on each request which interacts with a game.
  Application will check that user exists in database and owns the game referenced by id, if this check fails application will respond a 403 Forbidden.
  Token can be renewed by calling the same endpoint that was used to identify the user, if user already exists it will just generate a new token.
* Time tracking is calculated in Game's secondsPlayed attribute. It is not a real time tracking, it just gets updated by calculating time elapsed from start date of the game excluding game paused periods.

## Building

To launch tests:
```
./gradlew clean test
```

To build application:
```
./gradlew clean build
```

To run application:
```
./gradlew clean run
```
