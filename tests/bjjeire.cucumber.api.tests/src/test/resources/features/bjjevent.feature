@API @Feature:BJJ-Events
Feature: BJJ Event Management
  As a user of the BJJ app,
  I want to create and retrieve BJJ events
  so that I can ensure the event listings are accurate and up-to-date.

  Background:
    Given I am an authenticated admin user
    And the API service is available

  @Smoke @Regression @Priority:High @Requirement=345 @TestCase=789
  Scenario: Successfully create a new, complex BJJ event
    Given I have the details for a new event named "Berlin BJJ Seminar Series"
    When I create the new BJJ event
    Then the system should confirm the event was created successfully
    And the event details in the response should contain:
      | path                      | value                           |
      | data.name                 | Dublin BJJ Masterclass Series   |
      | data.location.venue       | Dublin Grappling Hub            |
      | data.organiser.name       | Dublin Grappling Hub            |

  @Regression @Priority:High @Requirement=345 @TestCase=801 @Ignore
  Scenario: Successfully retrieve an existing BJJ event
    Given a BJJ event already exists with the name "IBJJF Pan Ams"
    When I request the details for the "IBJJF Pan Ams" event
    Then the API should respond with the complete details for that event
    And the event's name in the response should be "IBJJF Pan Ams"

  @Regression @Priority:Medium @Requirement=346 @Ignore
  Scenario Outline: Attempt to create a BJJ event with invalid data
    Given I have a BJJ event payload that is invalid because of "<InvalidReason>"
    When I attempt to create the event
    Then the API should respond with a bad request error
    And the error message should be "<ExpectedError>"

    @TestCase=790 @Ignore
    Examples: Invalid Data
      | InvalidReason         | ExpectedError             |
      |a missing name         | Event name is mandatory   |
      | a negative price      | Price cannot be negative  |