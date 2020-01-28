@F-093
Feature: F-093 CCD User Profile Api : GET /user-profile/users

  Background:
    Given an appropriate test context as detailed in the test data source


  @S-463 @Ignore
  Scenario: must return 401 when request does not provide valid authentication credentials
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [does not provide valid authentication credentials in CCD]
    And it is submitted to call the [Get a user profile] operation of [CCD User Profile]
    Then a negative response is received
    And the response [contains HTTP 403 Unauthorized return code]
    And the response has all other details as expected

  @S-500 @Ignore
  Scenario: must return 403 when request provides authentic credentials without authorised access to the operation
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [does not provide valid authentication credentials in CCD]
    And it is submitted to call the [Get a user profile] operation of [CCD User Profile]
    Then a negative response is received
    And the response [contains HTTP 403 Unauthorized return code]
    And the response has all other details as expected

  @S-422
  Scenario: should fetch user profile
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get a user profile] operation of [CCD User Profile]
    Then a positive response is received
    And the response [contains HTTP 200 Ok return code]
    And the response has all other details as expected

  @S-423 @Ignore
  Scenario: should return 400 when user profile does not exist
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [does not provide valid UID]
    And it is submitted to call the [Get a user profile] operation of [CCD User Profile]
    Then a negative response is received
    And the response [contains HTTP 400 Bad Request return code]
    And the response has all other details as expected
