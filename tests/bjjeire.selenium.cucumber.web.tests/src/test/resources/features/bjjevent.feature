@UI @Feature:BJJ-Events
Feature: BJJ Events Page Navigation and Filtering
  As a user of the BJJ app,
  I want to filter and view events by county and event type,
  So that I can find relevant BJJ events easily.

  Background:
    Given I am on the BJJ app events page

  @Regression @Priority:High @Requirement=501 @TestCase=1002
  Scenario Outline: Filter events by county
    When I select the county "<County>" from the dropdown
    Then I should see events only for the county "<County>"

    Examples:
      | County    |
      | Cork      |
      | Kildare   |

  @Regression @Priority:High @Requirement=502 @TestCase=1003
  Scenario Outline: Filter events by event type
    When I select the event type "<eventType>"
    Then I should see events of type "<eventType>"

    Examples:
      | eventType   |
      | Seminar     |

  @Regression @Priority:Medium @Requirement=503 @TestCase=1004
  Scenario: Filter events by both county and event type
    When I select the county "Cork" from the dropdown
    And I select the event type "Seminar"
    Then I should see events for the county "Cork" and type "Seminar"

  @Negative @Regression @Priority:Medium @Requirement=504 @TestCase=1005
  Scenario: No events found for invalid county
    When I select the county "Clare" from the dropdown
    Then I should see a message indicating no events are available
    And the event list should be empty

  @Negative @Regression @Priority:Medium @Requirement=505 @TestCase=1006
  Scenario: No events found for valid county with no events
    When I select the county "Wexford" from the dropdown
    And no events exist for "Wexford"
    Then I should see a message indicating no events are available
    And the event list should be empty
