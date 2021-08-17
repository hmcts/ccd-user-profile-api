#===================================================
@F-098
Feature: F-098: Get User Profiles by Email Id
#===================================================

Background:
    Given an appropriate test context as detailed in the test data source

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
@S-098.1
Scenario: should fetch a user profile for a given email id in request header

    Given a successful call [to create user profiles] as in [Standard_User_Profiles_Creation_Data]

     When a request is prepared with appropriate values
      And the request [contains in the related header the email id of the user whose profile was created above]
      And it is submitted to call the [Get User Profiles by Email Id] operation of [CCD User Profile API]

     Then the response [contains the user profile queried successfully returned]
      And the response has all other details as expected

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
@S-098.2
Scenario: should fetch no user profiles for some unknown email ids

    Given a user with [an active profile to play this scenario],

     When a request is prepared with appropriate values
      And it is submitted to call the [Get User Profiles by Email Id] operation of [CCD User Profile API]

     Then the response [contains an empty user profile list successfully returned]
      And the response has all other details as expected

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
@S-098.3
Scenario: should fetch a user profile for a given email id in query parameter

    Given a successful call [to create user profiles] as in [Standard_User_Profiles_Creation_Data]

     When a request is prepared with appropriate values
      And the request [contains in the related query parameter the email id of the user whose profile was created above]
      And it is submitted to call the [Get User Profiles by Email Id] operation of [CCD User Profile API]

     Then the response [contains the user profile queried successfully returned]
      And the response has all other details as expected

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
