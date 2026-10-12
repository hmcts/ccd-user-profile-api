@F-099 @CCD-4657
Feature: F-099: Create User Profile

Background:
    Given an appropriate test context as detailed in the test data source

@S-099.1
Scenario: should create a user profile with jurisdictions and workbasket defaults
     When a request is prepared with appropriate values
      And it is submitted to call the [Create User Profile] operation of [CCD User Profile API]
     Then the response [has the 201 Created code]
      And the response has all other details as expected
      And a call [to fetch the created user profile] will get the expected response as in [F-099_Verify_Created_Profile]

@S-099.2
Scenario: should reject a user profile without an id
     When a request is prepared with appropriate values
      And it is submitted to call the [Create User Profile] operation of [CCD User Profile API]
     Then a negative response is received
      And the response [has the 400 Bad Request code]
      And the response has all other details as expected

@S-099.3
Scenario: should reject a user profile without associated jurisdictions
     When a request is prepared with appropriate values
      And it is submitted to call the [Create User Profile] operation of [CCD User Profile API]
     Then a negative response is received
      And the response [has the 400 Bad Request code]
      And the response has all other details as expected

@S-099.4
Scenario: should reject a jurisdiction without an id
     When a request is prepared with appropriate values
      And it is submitted to call the [Create User Profile] operation of [CCD User Profile API]
     Then a negative response is received
      And the response [has the 400 Bad Request code]
      And the response has all other details as expected
