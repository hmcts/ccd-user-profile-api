#===================================================
@F-098
Feature: F-098: Get User Profiles by Email Id
#===================================================

Background:
    Given an appropriate test context as detailed in the test data source

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
@S-468
Scenario: should fetch all user profiles based on given email ids

    Given a successful call [to create user profiles] as in [Standard_User_Profiles_Creation_Data]

     When a request is prepared with appropriate values
      And it is submitted to call the [Get User Profiles by Email Id] operation of [CCD User Profile API]

     Then the response [has the 200 OK code]
      And the response has all other details as expected

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
@S-469
Scenario: should fetch no user profile for the unknown email id

    Given a successful call [to create user profiles] as in [Standard_User_Profiles_Creation_Data]

     When a request is prepared with appropriate values
      And it is submitted to call the [Get User Profiles by Unknown Email Id] operation of [CCD User Profile API]

     Then the response [has the 200 OK code]
      And the response has all other details as expected

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
