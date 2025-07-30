@api @feature:BJJ-Events
Feature: BJJ Event Management
  As an event organizer,
  I want to manage BJJ events through the API,
  So that I can ensure event listings are accurate and up-to-date.

  Background:
    Given I am authenticated as an admin user

  @smoke @regression @priority:high @Requirement=345 @TestCase=789
  Scenario: Create a new BJJ event successfully
    Given I have valid details for a new BJJ event named "Dublin BJJ Seminar Series"
    When I create the BJJ event
    Then the event is created successfully
    And the event details include:
      | Name                     | Dublin BJJ Seminar Series |
      | Location                 | Dublin Grappling Hub      |

  @regression @priority:high @Requirement=345 @TestCase=801
  Scenario: Retrieve an existing BJJ event by county
    Given a BJJ event exists with the name "IBJJF Pan Ams" in county "Dublin"
    When I retrieve all BJJ events for county "Dublin"
    Then the response contains the event "IBJJF Pan Ams"
    And the event details include:
      | Name                     | IBJJF Pan Ams             |


  @Negative @regression @priority:medium @Requirement=346 @TestCase=790
  Scenario Outline: Attempt to create a BJJ event with invalid data
    Given I have a BJJ event with invalid "<InvalidField>"
    When I attempt to create the BJJ event
    Then the API returns a bad request error with message "<ErrorMessage>"

    Examples:
      | InvalidField     | ErrorMessage                     |
      | missing name     | Event Name is required.          |
      | negative price   | Amount must be greater than 0.   |