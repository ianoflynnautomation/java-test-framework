@ui @web @feature-events @Lifecycle:REUSE_IF_STARTED
Feature: Event Filtering
  In order to find relevant open mats, seminars and tournaments
  As a user of the BJJ app
  I want to filter events by county and type

  Background:
    Given I am an authenticated user
    And I am on the BJJ app events page

  @smoke @regression @priority-high @Requirement-501 @TestCase-1002
  Scenario Outline: Filter BJJ events by county
    Given the following BJJ events exist:
      | Name               | County   | Type       |
      | <County> Seminar 1 | <County> | Seminar    |
      | <County> Seminar 2 | <County> | Seminar    |
      | Dublin IBJJF       | Dublin   | Tournament |
    When I filter events by county "<County>"
    Then the displayed events include only those for county "<County>"
    And the event list contains exactly <ExpectedCount> events

    Examples:
      | County  | ExpectedCount |
      | Cork    |             2 |
      | Kildare |             2 |

  @regression @priority-high @Requirement-502 @TestCase-1003
  Scenario: Filter BJJ events by event type
    Given the following BJJ events exist:
      | Name           | County | Type       |
      | Cork Seminar 1 | Cork   | Seminar    |
      | Cork Seminar 2 | Cork   | Seminar    |
      | Cork IBJJF     | Cork   | Tournament |
    When I filter events by type "Seminar"
    Then the displayed events include only those of type "Seminar"
    And the event list contains exactly 2 events

  @negative @regression @priority-medium @Requirement-504 @TestCase-1005
  Scenario: No events found for a county with no events
    Given the following BJJ events exist:
      | Name              | County | Type       |
      | Dublin Tournament | Dublin | Tournament |
    When I filter events by county "Clare"
    Then the event list is empty

  @negative @regression @priority-medium @Requirement-505 @TestCase-1006
  Scenario: No events found when none exist in the system
    Given no BJJ events exist
    When I filter events by county "Wexford"
    Then the event list is empty
