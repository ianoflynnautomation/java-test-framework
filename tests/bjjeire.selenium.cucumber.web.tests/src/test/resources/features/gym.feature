@UI @Feature:Gyms
Feature: Gyms Navigation and Filtering
  As a user of the BJJ app,
  I want to filter and view gyms by county,
  So that I can find relevant BJJ gym easily.

  Background:
    Given I am on the BJJ app gyms page

  @Regression @Priority:High @Requirement=101 @TestCase=1101
  Scenario Outline: Filter gyms by county
    When I filter gyms by "<County>" from the dropdown
    Then I should see gyms only for the county "<County>"

    Examples:
      | County |
      | Dublin |
      | Cork   |

  @Negative @Regression @Priority:Medium @Requirement=102 @TestCase=1102
  Scenario: No gyms found for invalid county
    When I filter gyms by "Clare" from the dropdown
    Then I should see a message indicating no gyms are available
    And the gyms list should be empty