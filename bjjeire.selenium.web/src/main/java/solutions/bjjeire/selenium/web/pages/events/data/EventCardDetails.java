package solutions.bjjeire.selenium.web.pages.events.data;

import java.util.List;

import solutions.bjjeire.core.data.events.BjjEventType;

public record EventCardDetails(
                String name,
                String county,
                List<BjjEventType> eventTypes,
                String address,
                String addressUrl,
                String organiser,
                String organiserUrl,
                String cost) {
}
