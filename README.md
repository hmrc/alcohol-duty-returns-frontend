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

3. When redirected to auth-login-stub, change the following fields:
    - **Affinity group**: Organisation
    - **Enrolments**: Add an enrolment with:
        - **Enrolment Key**: HMRC-AD-ORG
        - **Identifier Name**: APPAID
        - **Identifier Value**: * *Provide from stub data* *

### Test only

When running the frontend microservice, allow routes in test.routes to be called:

> `sbt "run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes"`

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

## Scalafmt and Scalastyle

To check if all the scala files in the project are formatted correctly:
> `sbt scalafmtCheckAll`

To format all the scala files in the project correctly:
> `sbt scalafmtAll`

To check if there are any scalastyle errors, warnings or infos:
> `sbt scalastyle`
>

## All tests and checks

This is an sbt command alias specific to this project. It will run a scala format
check, run a scala style check, run unit tests, run integration tests and produce a coverage report:
> `sbt runAllChecks`

## Scripts

It is expected these scripts will be run from the scripts directory where they reside

### checkWelshKeys.sh

Checks that every English key entry also has a Welsh one (and v/v). Used to detect added English keys
or Welsh keys that need removing. Thus helping keep the two files in sync.

### findMessageKeys.pl

Script to list as many (not all) message keys that are in use in the code. A number of known substitutions
are made to expand dynamic content to actual messages. However due to the nature of the code some message
keys will not be found by this script. A file 'also_used_keys' should contain keys that this script cannot
find but we know are in use.

### getUnusedKeys.sh

Script to list all keys that are not in use. Uses findMessageKeys.pl and also_used_keys to output this.

### getNotChangedWelshMessages.sh

Takes a commit id, and tries to pair up any changes to the messages.en file up to and including that
commit with changes made to messages.cy. The aim is to try to help ensure changes made to the English
have also been made to the Welsh. Although sorted, the keys are not uniquified so if two changes are
made to messages.en, but only one to messages.cy, it will show the key once (even though the change to
the Welsh file might account for both - this will need to be manually checked).

### misc

The file also_used_keys contains keys that we can't 'also_used_keys' should contain keys that the
findMessageKeys.pl script cannot find automatically from the code, but we know are in use.

## License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
