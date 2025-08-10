@ui @web @feature-gyms @Lifecycle:REUSE_IF_STARTED
Feature: Gym Filtering
  In order to find a local bjj gym to train
  As a user of the BJJ app
  I want to filter gyms by county

  Background:
    Given I am a user of the BJJ app

  Rule: Users can search gyms by county

    @regression @priority-high @Requirement-101 @TestCase-1101
    Scenario Outline: Filter gyms by county
      Given the following BJJ gyms exist:
        | Name             | County   |
        | <County> Gym 1   | <County> |
        | <County> Gym 2   | <County> |
        | Other County Gym | Kildare  |
      When I search gyms by county "<County>"
      Then I should only see gyms for county "<County>"

      Examples:
        | County |
        | Dublin |
        | Cork   |

    @negative @regression @priority-medium @Requirement-102 @TestCase-1102
    Scenario: No gyms found for a county with no gyms
      Given the following BJJ gyms exist:
        | Name       | County |
        | Dublin Gym | Dublin |
      When I search gyms by county "Clare"
      Then I should not see any gyms
