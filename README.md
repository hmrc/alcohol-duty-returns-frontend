
# alcohol-duty-returns-frontend

This is the frontend microservice that handles Returns related operations for Alcohol Duty Service.

## Creating pages
This project uses [hmrc-frontend-scaffold.g8](https://github.com/hmrc/hmrc-frontend-scaffold.g8) to create frontend pages. 

Please see this [wiki page](https://github.com/hmrc/hmrc-frontend-scaffold.g8/wiki/Usage) for guidance around how to create new pages.

## Running the service

Make sure you run all the dependant services through the service manager:

> `sm2 --start ALCOHOL_DUTY_ALL`

Stop the frontend microservice from the service manager and run it locally:

> `sm2 --stop ALCOHOL_DUTY_RETURNS_FRONTEND`
> `sbt run`

The service runs on port `16000` by default.

## Navigating the service

1. Navigate to [http://localhost:16000/manage-alcohol-duty/start](http://localhost:16000/manage-alcohol-duty/start)

2. When redirected to auth-login-stub, change the following: 
    - **For returns journey**:

      - Change the affinity group:
        - **Affinity group**: Organisation
      - Add enrolment:
        - **Enrolment Key**: HMRC-AD-ORG
        - **Identifier Name**: APPAID
        - **Identifier Value**: * *Provide from stub data* *
   
    - **For claim enrolment journey**:

      - Change the affinity group:
        - **Affinity group**: Organisation
  
## Running tests

### Unit tests

> `sbt test`

### Integration tests

> `sbt it/test`

## Scalafmt and Scalastyle

To check if all the scala files in the project are formatted correctly:
> `sbt scalafmtCheckAll`

To format all the scala files in the project correctly:
> `sbt scalafmtAll`

To check if there are any scalastyle errors, warnings or infos:
> `sbt scalastyle`

## All tests and checks

This is an sbt command alias specific to this project. It will run a scala format
check, run a scala style check, run unit tests, run integration tests and produce a coverage report:
> `sbt runAllChecks`

## License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
