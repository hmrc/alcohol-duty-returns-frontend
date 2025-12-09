# alcohol-duty-returns-frontend

This is the frontend microservice that handles Returns related operations for Alcohol Duty Service.

## Creating pages

This project uses [hmrc-frontend-scaffold.g8](https://github.com/hmrc/hmrc-frontend-scaffold.g8) to create frontend
pages.

Please see this [wiki page](https://github.com/hmrc/hmrc-frontend-scaffold.g8/wiki/Usage) for guidance around how to
create new pages.

## Running the service

1. Make sure you run all the dependant services through the service manager:

   > `sm2 --start ALCOHOL_DUTY_ALL`

2. Stop the frontend microservice from the service manager and run it locally:

   > `sm2 --stop ALCOHOL_DUTY_RETURNS_FRONTEND`

   > `sbt run`

The service runs on port `16000` by default.

## Navigating the service

### Claim enrolment journey

1. Navigate to [http://localhost:16000/manage-alcohol-duty/start](http://localhost:16000/manage-alcohol-duty/start)

2. When redirected to auth-login-stub, change the following:
    - **Affinity group**: Organisation

### Other journeys/sections

1. Navigate to relevant url:
    - [Returns journey](http://localhost:16000/manage-alcohol-duty/before-you-start-your-return/24AF)
    - [View payments screen](http://localhost:16000/manage-alcohol-duty/view-payments)
    - [View returns screen](http://localhost:16000/manage-alcohol-duty/check-your-returns)

2. When redirected to auth-login-stub, change the following fields:
    - **Affinity group**: Organisation
    - **Enrolments**: Add an enrolment with:
        - **Enrolment Key**: HMRC-AD-ORG
        - **Identifier Name**: APPAID
        - **Identifier Value**: * *Provide from stub data* *

### Test only

When running the frontend microservice, allow routes in test.routes to be called:

> `sbt "run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes"`

#### Clear user answers

This endpoint clears all data in the user answers repository (in alcohol-duty-returns):
http://localhost:16000/manage-alcohol-duty/test-only/clear-all

#### Clear historic payments data

This endpoint clears all data in the user historic payments repository (in alcohol-duty-account):
http://localhost:16000/manage-alcohol-duty/test-only/clear-user-historic-payments

#### Clear fulfilled obligation data

This endpoint clears all data in the user fulfilled obligations repository (in alcohol-duty-account, for completed
returns):
http://localhost:16000/manage-alcohol-duty/test-only/clear-user-fulfilled-obligations

#### Create existing user answers

To create existing user answers with specific alcohol regimes before starting the returns journey, change the following
fields on the auth-login-stub:

- Redirect URL: Specify the alcohol regimes as query parameters (if any regimes are missing, they are set to false by
  default), e.g.
  http://localhost:16000/manage-alcohol-duty/test-only/create-user-answers/24AJ?beer=true&cider=true&wine=true&spirits=true&OFP=true
- **Affinity group**: Organisation
- **Enrolments**: Add an enrolment with:
    - **Enrolment Key**: HMRC-AD-ORG
    - **Identifier Name**: APPAID
    - **Identifier Value**: * *Provide from stub data* *

If the user answers are created successfully, this is shown on a simple confirmation page with a link to 'Before you
start your
return'.

Note: At least one alcohol regime must be approved, otherwise a BAD_REQUEST is returned.

## Running tests

### Unit tests

> `sbt test`

### Integration tests

> `sbt it/test`

## Scalafmt

To check if all the scala files in the project are formatted correctly:
> `sbt scalafmtCheckAll`

To format all the scala files in the project correctly:
> `sbt scalafmtAll`

## All tests and checks

This is an sbt command alias specific to this project. It will run a scala format
check, run a scala style check, run unit tests, run integration tests and produce a coverage report:
> `sbt runAllChecks`

## License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
