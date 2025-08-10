@ui @web @feature-events @Lifecycle:REUSE_IF_STARTED
Feature: Event Filtering
  In order to find relevant open mats, seminars and tournaments
  As a user of the BJJ app
  I want to filter events by county and type

  Background:
    Given I am a user of the BJJ app

  Rule: Users can search events by county

    @smoke @regression @priority-high @Requirement-501 @TestCase-1002
    Scenario Outline: Filtering events by county
      Given the following BJJ events exist:
        | Name               | County   | Type       |
        | <County> Seminar 1 | <County> | Seminar    |
        | <County> Seminar 2 | <County> | Seminar    |
        | Dublin IBJJF       | Dublin   | Tournament |
      When I search for events in the county "<County>"
      Then I should see exactly <ExpectedCount> events for "<County>"

      Examples:
        | County  | ExpectedCount |
        | Cork    |             2 |
        | Kildare |             2 |

    @negative @regression @priority-medium @Requirement-504 @TestCase-1005
    Scenario: No events found for a county with no events
      Given the following BJJ events exist:
        | Name              | County | Type       |
        | Dublin Tournament | Dublin | Tournament |
      When I search events by county "Clare"
      Then I should not see any events

  Rule: Users can search events by type

    @regression @priority-high @Requirement-502 @TestCase-1003
    Scenario: Filtering events by type
      Given the following BJJ events exist:
        | Name           | County | Type       |
        | Cork Seminar 1 | Cork   | Seminar    |
        | Cork Seminar 2 | Cork   | Seminar    |
        | Cork IBJJF     | Cork   | Tournament |
      When I search for events of type "Seminar"
      Then I should see exactly 2 events of type "Seminar"

    @negative @regression @priority-medium @Requirement-505 @TestCase-1006
    Scenario: No events found when none exist
      Given no BJJ events exist
      When I search events by county "Wexford"
      Then I should not see any events
