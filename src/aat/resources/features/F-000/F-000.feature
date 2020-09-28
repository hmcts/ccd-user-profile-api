#=============================
@F-000
Feature: Healthcheck Operation
#=============================

Background:
    Given an appropriate test context as detailed in the test data source

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
@S-000
Scenario: must return an all-healthy response from the Healthcheck Operation

     When a request is prepared with appropriate values
      And it is submitted to call the [Healthcheck] operation of [CCD User Profile API]

     Then a positive response is received
      And the response [has the 200 OK code]
      And the response has all other details as expected

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
