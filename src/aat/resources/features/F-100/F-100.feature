@F-100 @CCD-4657
Feature: F-100: Populate User Profiles

Background:
    Given an appropriate test context as detailed in the test data source

@S-100.1
Scenario: should create multiple user profiles and associate their default jurisdictions
     When a request is prepared with appropriate values
      And it is submitted to call the [Populate User Profiles] operation of [CCD User Profile API]
     Then the response [has the 200 OK code]
      And the response has all other details as expected
      And a call [to fetch the first saved user profile] will get the expected response as in [F-100_Verify_First_Profile]
      And a call [to fetch the second saved user profile] will get the expected response as in [F-100_Verify_Second_Profile]

@S-100.2
Scenario: should update workbasket defaults while retaining existing jurisdiction membership
    Given a successful call [to create an existing user profile] as in [F-100_Create_Existing_Profile]
     When a request is prepared with appropriate values
      And it is submitted to call the [Populate User Profiles] operation of [CCD User Profile API]
     Then the response [has the 200 OK code]
      And the response has all other details as expected
      And a call [to fetch the updated user profile] will get the expected response as in [F-100_Verify_Updated_Profile]
