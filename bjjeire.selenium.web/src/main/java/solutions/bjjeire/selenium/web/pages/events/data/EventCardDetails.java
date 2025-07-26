package solutions.bjjeire.selenium.web.pages.events.data;

import solutions.bjjeire.core.data.events.BjjEventType;

import java.util.List;

public record EventCardDetails(
        String name,
        String county,
        List<BjjEventType> eventTypes,
        String address,
        String addressUrl,
        String organiser,
        String organiserUrl,
        String cost
) { }
