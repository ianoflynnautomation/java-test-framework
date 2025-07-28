@api @feature:gym
Feature: BJJ Gym Management
  As an admin,
  I want to manage BJJ gyms through the API,
  So that I can maintain accurate and up-to-date gym listings.

  Background:
    Given I am authenticated as an admin user

  @smoke @regression @priority:high @Requirement=145 @TestCase=189
  Scenario: Create a new Gym successfully
    Given I have valid details for a new Bjj Gym named "Carlson Gracie Dublin"
    When I create the Bjj Gym
    Then the gym is created successfully
    And the gym details include:
      | Name                     | Carlson Gracie Dublin |