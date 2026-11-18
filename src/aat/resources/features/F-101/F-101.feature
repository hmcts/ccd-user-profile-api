@F-101 @CCD-4658
Feature: F-101: Save and delete user profile jurisdiction associations

Background:
    Given an appropriate test context as detailed in the test data source

@S-101.1
Scenario: should save a new user profile through the legacy users endpoint
     When a request is prepared with appropriate values
      And it is submitted to call the [Save User Profile] operation of [CCD User Profile API]
     Then the response [has the 200 OK code]
      And the response has all other details as expected

@S-101.2
Scenario: should save an existing user profile through the legacy users endpoint
    Given a successful call [to create a user profile] as in [F-101_Create_Profile]
     When a request is prepared with appropriate values
      And it is submitted to call the [Save User Profile] operation of [CCD User Profile API]
     Then the response [has the 200 OK code]
      And the response has all other details as expected

@S-101.3
Scenario: should delete a non-default jurisdiction from a user profile
    Given a successful call [to create a user profile with two jurisdictions] as in [F-101_Create_Profile_Two_Jurisdictions]
     When a request is prepared with appropriate values
      And it is submitted to call the [Delete User Profile Jurisdiction] operation of [CCD User Profile API]
     Then the response [has the 204 No Content code]
      And the response has all other details as expected

@S-101.4
Scenario: should reject deleting the user's default jurisdiction
    Given a successful call [to create a user profile with two jurisdictions] as in [F-101_Create_Profile_Two_Jurisdictions]
     When a request is prepared with appropriate values
      And it is submitted to call the [Delete User Profile Jurisdiction] operation of [CCD User Profile API]
     Then a negative response is received
      And the response [has the 400 Bad Request code]
      And the response has all other details as expected

@S-101.5
Scenario: should reject deleting a jurisdiction not associated with a user
    Given a successful call [to create a user profile] as in [F-101_Create_Profile]
     When a request is prepared with appropriate values
      And it is submitted to call the [Delete User Profile Jurisdiction] operation of [CCD User Profile API]
     Then a negative response is received
      And the response [has the 400 Bad Request code]
      And the response has all other details as expected
