
  @Type:API @Feature:EventManagement
  Feature: BJJ Event Management
  In order to provide users with accurate and up-to-date event listings,
  As an event organizer or user,
  I want to manage events.

  Background:
    Given Admin is authenticated

  @Type:Smoke @Type:Regression @Priority:High @TestCase:C789
  Scenario: Admin creates a new BJJ event with valid details
    Scenario: Admin creates a new BJJ event with valid details
      Given a new event has been prepared
      When Admin adds the new event
      Then the event should be successfully added

  @Type:Negative @Type:Regression @Priority:Medium @TestCase:C790
  Scenario Outline: Admin attempts to create a BJJ event with invalid data
    Given Admin has a BJJ event with "<Field>" set to "<InvalidValue>"
    When Admin attempts to create the BJJ event
    Then Admin is notified that the event creation failed for "<Field>" with message "<ErrorMessage>"

    Examples:
      | Field           | InvalidValue | ErrorMessage                        |
      | Data.Name            |              | Event Name is required.             |
      | Data.Pricing.Amount  | -10.00       | Amount must be greater than 0.      |