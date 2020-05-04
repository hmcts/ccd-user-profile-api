# ccd-user-profile-api
[![API Docs](https://img.shields.io/badge/API%20Docs-site-e140ad.svg)](https://hmcts.github.io/reform-api-docs/swagger.html?url=https://hmcts.github.io/reform-api-docs/specs/ccd-user-profile-api.json)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build Status](https://api.travis-ci.org/hmcts/ccd-user-profile-api.svg?branch=master)](https://travis-ci.org/hmcts/ccd-user-profile-api)
[![Docker Build Status](https://img.shields.io/docker/build/hmcts/ccd-user-profile-api.svg)](https://hub.docker.com/r/hmcts/ccd-user-profile-api)
[![codecov](https://codecov.io/gh/hmcts/ccd-user-profile-api/branch/master/graph/badge.svg)](https://codecov.io/gh/hmcts/ccd-user-profile-api)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/3798a9145e064d71bcf5fc7c89c74013)](https://www.codacy.com/app/adr1ancho/ccd-user-profile-api?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=hmcts/ccd-user-profile-api&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/3798a9145e064d71bcf5fc7c89c74013)](https://www.codacy.com/app/adr1ancho/ccd-user-profile-api?utm_source=github.com&utm_medium=referral&utm_content=hmcts/ccd-user-profile-api&utm_campaign=Badge_Coverage)
[![Known Vulnerabilities](https://snyk.io/test/github/hmcts/ccd-user-profile-api/badge.svg)](https://snyk.io/test/github/hmcts/ccd-user-profile-api)
[![HitCount](http://hits.dwyl.io/hmcts/ccd-user-profile-api.svg)](#ccd-user-profile-api)

UI preferences for Core Case Data users.
____

## Getting started

### Prerequisites

- [Open JDK 8](https://openjdk.java.net/)
- [Docker](https://www.docker.com)

#### Environment variables

The following environment variables are required:

| Name | Default | Description |
|------|---------|-------------|
| USER_PROFILE_DB_USERNAME | - | Username for database |
| USER_PROFILE_DB_PASSWORD | - | Password for database |
| USER_PROFILE_S2S_AUTHORISED_SERVICES | ccd_data,ccd_definition,ccd_admin | Authorised micro-service names for S2S calls |
| IDAM_S2S_URL | - | Base URL for IdAM's S2S API service (service-auth-provider). `http://localhost:4502` for the dockerised local instance or tunneled `dev` instance. |
| AZURE_APPLICATIONINSIGHTS_INSTRUMENTATIONKEY | - | For CNP environment this is provided by the terraform scripts. However any value would do for your local environment. |
### Building

The project uses [Gradle](https://gradle.org/).

To build project please execute the following command:

```bash
./gradlew clean build
```

### Gradle Upgrades
It is important to run the following command and commit the updated `gradle-wrapper.jar` to the repository for proper gradle upgrades

```bash
# this sample is for v4.9 replace with correct upgrade version
./gradlew wrapper --gradle-version 4.9 --distribution-type all
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

### Functional Tests
The functional tests are located in `aat` folder. The tests are written using 
befta-fw library. To find out more about BEFTA Framework, see the repository and its README [here](https://github.com/hmcts/befta-fw).

## LICENSE
This project is licensed under the MIT License - see the [LICENSE](LICENSE.md) file for details.

