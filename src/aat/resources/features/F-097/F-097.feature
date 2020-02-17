@F-097
Feature: CCD User Profile Api :: GET /users

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-424
  Scenario: should fetch user profile
    When user profiles that have just been created as in [Standard_User_Profiles_Creation_Data]
    And a request is prepared with appropriate values
    And it is submitted to call the [Retrieve user profiles] operation of [CCD User Profile]
    Then a positive response is received
    And the response [has the 200 OK code]
    And the response has all other details as expected

  @S-504
  Scenario: must return 403 when request provides authentic credentials without authorized access to the operation
    When a request is prepared with appropriate values
    And the request [does not provide a valid authentication credentials]
    And it is submitted to call the [Retrieve user profiles] operation of [CCD User Profile]
    Then a negative response is received
    And the response [contains a HTTP 403 Forbidden]
    And the response has all other details as expected
