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
    When the Admin adds the new event
    Then the event should be successfully added

  @Type:Negative @Type:Regression @Priority:Medium @TestCase:C790
  Scenario Outline: Admin attempts to create a BJJ event with invalid data
    Given the Admin has an event with "<Field>" set to "<InvalidValue>"
    When the Admin attempts to add the new event
    Then the Admin should be notified that adding the event failed for "<Field>" with message "<ErrorMessage>"

    Examples:
      | Field               | InvalidValue | ErrorMessage                   |
      | Data.Name           |              | Event Name is required.        |
      | Data.Pricing.Amount |       -10.00 | Amount must be greater than 0. |
