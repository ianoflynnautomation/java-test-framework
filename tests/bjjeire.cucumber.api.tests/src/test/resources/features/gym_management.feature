@api @feature:gym
Feature: BJJ Gym Management
  In order to provide users with accurate and up-to-date gym listings,
  As an admin,
  I want to manage BJJ gyms.

  Background:
    Given Admin is authenticated

  @Type:Smoke @Type:Regression @Priority:High @TestCase:C189
  Scenario: Admin creates a new BJJ gym with valid details
    Given a new BJJ gym has been prepared
    When Admin adds the new BJJ gym
    Then the gym should be successfully added