
# alcohol-duty-returns-frontend

This is the frontend microservice that handles Returns related operations for Alcohol Duty Service.

## API Endpoints

## Running the service

> `sbt run`

The service runs on port `16000` by default.

## Running tests

### Unit tests

> `sbt test`

### Integration tests

> `sbt it:test`

## Scalafmt and Scalastyle

To check if all the scala files in the project are formatted correctly:
> `sbt scalafmtCheckAll`

To format all the scala files in the project correctly:
> `sbt scalafmtAll`

To check if there are any scalastyle errors, warnings or infos:
> `sbt scalastyle`
>
> ### All tests and checks

This is an sbt command alias specific to this project. It will run a scala format
check, run a scala style check, run unit tests, run integration tests and produce a coverage report:
> `sbt runAllChecks`

## Building pages with the Scaffolding

1) Enter an `sbt` shell by issuing the command `sbt` in your terminal window. _(alternative, if using IntelliJ click on the `sbt shell` tab in the bottom panel)._

### Create a new section
- Once the sbt shell has initialised enter the command `g8Scaffold section`.


- The g8Scaffolding will prompt for values to properties. Such as the `sectionName`. Enter appropriate values for each prompt.


### Create a new page

- Once the sbt shell has initialised enter the command `g8Scaffold template` where `template` is replaced with one of the following available scaffolding templates:
   1) `bigDecimalPage`
   2) `checkboxPage`
   3) `contentPage`
   4) `datePage`
   5) `intPage`
   6) `multipleQuestionsPage`
   7) `radioButtonPage`
   8) `stringPage`
   9) `yesNoPage`
   10) `characterCountPage`


- The g8Scaffolding will prompt for values to properties. Such as the `className` and `section`. Enter appropriate values for each prompt.


- Once the g8Scaffold says `success :)`, exit out of the sbt shell and back to a normal terminal window.
 

- Run the script `./migrate.sh` to run the migrations _(this creates the messages entries and the routes entries - along with a few other things)_.

### License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").