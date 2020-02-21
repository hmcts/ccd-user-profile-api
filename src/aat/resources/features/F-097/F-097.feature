@F-097
Feature: F-097: Get Users

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-424
  Scenario: should fetch user profiles by jurisdiction
    Given a successful call [to create user profiles] as in [Standard_User_Profiles_Creation_Data]
    When a request is prepared with appropriate values
    And it is submitted to call the [Retrieve user profiles by jurisdiction id] operation of [CCD User Profile]
    Then the response [has the 200 OK code]
    And the response has all other details as expected

  @S-467
  Scenario: should fetch all user profiles
    Given a successful call [to create user profiles] as in [Standard_User_Profiles_Creation_Data]
    When a request is prepared with appropriate values
    And it is submitted to call the [Retrieve user profiles] operation of [CCD User Profile]
    Then the response [has the 200 OK code]
    And the response has all other details as expected

  @S-504
  Scenario: must return 403 when request provides authentic credentials without authorized access to the operation
    Given a request is prepared with appropriate values
    When the request [does not provide a valid authentication credentials]
    And it is submitted to call the [Retrieve user profiles] operation of [CCD User Profile]
    Then a negative response is received
    And the response [contains a HTTP 403 Forbidden]
    And the response has all other details as expected
