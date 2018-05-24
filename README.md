# user-profile-api
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build Status](https://travis-ci.org/hmcts/ccd-user-profile-api.svg?branch=master)](https://travis-ci.org/hmcts/ccd-user-profile-api)
[![codecov](https://codecov.io/gh/hmcts/ccd-user-profile-api/branch/master/graph/badge.svg)](https://codecov.io/gh/hmcts/ccd-user-profile-api)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/3798a9145e064d71bcf5fc7c89c74013)](https://www.codacy.com/app/adr1ancho/ccd-user-profile-api?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=hmcts/ccd-user-profile-api&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/3798a9145e064d71bcf5fc7c89c74013)](https://www.codacy.com/app/HMCTS/ccd-user-profile-api)
[![Known Vulnerabilities](https://snyk.io/test/github/hmcts/ccd-user-profile-api/badge.svg)](https://snyk.io/test/github/hmcts/ccd-user-profile-api)

UI preferences for Core Case Data users.
____

## Getting started

### Prerequisites

- [JDK 8](https://www.oracle.com/java)
- [Docker](https://www.docker.com)

#### Environment variables

The following environment variables are required:

| Name | Default | Description |
|------|---------|-------------|
| USER_PROFILE_DB_USERNAME | - | Username for database |
| USER_PROFILE_DB_PASSWORD | - | Password for database |
| USER_PROFILE_S2S_AUTHORISED_SERVICES | ccd_data,ccd_definition | Authorised micro-service names for S2S calls |
| IDAM_S2S_URL | - | Base URL for IdAM's S2S API service (service-auth-provider). `http://localhost:4502` for the dockerised local instance or tunneled `dev` instance. |
| APPINSIGHTS_INSTRUMENTATIONKEY | - | For CNP environment this is provided by the terraform scripts. However any value would do for your local environment. |
| USER_PROFILE_DB_USE_SSL | - | Mandated by Cloud Native Platform.  For local testing, set this variable to false |
### Building

The project uses [Gradle](https://gradle.org/).

To build project please execute the following command:

```bash
./gradlew clean build
```

When build finishes successfully, a jar file can be found in

```bash
build/libs/user-profile-$(./gradlew -q projectVersion)-all.jar
```

### Running

If you want your code to become available to other Docker projects (e.g. for local environment testing), you need to build the image:

```bash
docker-compose build
```

The above will build both the application and database images.  
If you want to build only one of them just specify the name assigned in docker compose file, e.g.:

```bash
docker-compose build ccd-user-profile-api
```

When the project has been packaged in `target/` directory,
you can run it by executing following command:

```bash
docker-compose up
```

As a result the following containers will get created and started:

 - Database exposing port `5453`
 - API exposing ports `4453`

#### Handling database

Database will get initiated when you run `docker-compose up` for the first time by execute all scripts from `database` directory.

You don't need to migrate database manually since migrations are executed every time `docker-compose up` is executed.

You can connect to the database at `http://localhost:5453` with the username and password set in the environment variables.

### API
Their are currently 2 endpoints:
- POST /user-profile/users - this is used to create a User Profile as well as it's associated Jurisdictions. At least one Jurisdiction must be defined in order to create a User Profile. Also, there is currently no way to amend a User's Jurisdictions other than manually removing them from the database and creating a new User.
- GET /user-profile/users/{uid}/jurisdictions - this is used to retrieve a String[] of Jurisdictions ID for the given User ID.

#### Create a User
To create a user the following example JSON can be used:
```json
{
	"id" : "user1",
	"jurisdictions" : [{
			"id" : "TEST1"
		}, {
			"id" : "TEST2"
		}
	]
}
```

## LICENSE

This project is licensed under the MIT License - see the [LICENSE](LICENSE.md) file for details.

