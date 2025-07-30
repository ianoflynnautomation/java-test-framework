package solutions.bjjeire.core.data.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.javafaker.Faker;
import org.bson.types.ObjectId;
import solutions.bjjeire.core.data.common.County;
import solutions.bjjeire.core.data.common.GeoCoordinates;
import solutions.bjjeire.core.data.common.Location;
import solutions.bjjeire.core.data.common.SocialMedia;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class BjjEventFactory {

    private static final Faker faker = new Faker();
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .registerModule(new JavaTimeModule());

    public static CreateBjjEventCommand getValidBjjEventCommand() {
        return new CreateBjjEventCommand(getValidBjjEvent());
    }

    public static BjjEvent getValidBjjEvent() {
        return createBjjEvent(null);
    }

    public static BjjEvent createBjjEvent(Consumer<BjjEvent.Builder> configure) {

        String uniqueEventName = faker.esports().event() + " " + UUID.randomUUID().toString().substring(0, 8);

        var builder = new BjjEvent.Builder()
                .id(new ObjectId().toString())
                .createdOnUtc(LocalDateTime.now(ZoneOffset.UTC))
                .updatedOnUtc(LocalDateTime.now(ZoneOffset.UTC))
                .name(uniqueEventName)
                .description(
                        "Weekly BJJ seminars with Professor " + faker.name().fullName() + " at Dublin Grappling Hub.")
                .type(BjjEventType.SEMINAR)
                .organiser(new Organizer(
                        "Dublin Grappling Hub",
                        "https://www.dublingrappling.com"))
                .status(EventStatus.Upcoming)
                .statusReason("Event is coming soon")
                .socialMedia(new SocialMedia(
                        "https://www.instagram.com/dublingrappling",
                        "https://www.facebook.com/dublingrappling",
                        "https://x.com/dublingrappling",
                        "https://www.youtube.com/@dublingrappling"))
                .county(County.Dublin)
                .location(new Location(
                        "45 O'Connell Street, Dublin 1, Ireland",
                        "Dublin Grappling Hub",
                        new GeoCoordinates(
                                "Point", -6.260273, 53.349805, "Dublin test", faker.random().hex(20))))
                .schedule(new BjjEventSchedule(
                        ScheduleType.FixedDate,
                        LocalDate.now(ZoneOffset.UTC).plusDays(14),
                        LocalDate.now(ZoneOffset.UTC).plusDays(14),
                        List.of(new DailySchedule(DayOfWeek.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(13, 0)))))
                .pricing(new PricingModel(
                        PricingType.FlatRate, new BigDecimal("45.00"), 1, "EUR"))
                .eventUrl("https://www.dublingrappling.com/events")
                .imageUrl("https://www.dublingrappling.com/images/event_poster.jpg");

        if (configure != null) {
            configure.accept(builder);
        }

        return builder.build();
    }

    public static CreateBjjEventCommand createInvalidEvent(String invalidReason) {
        // Start with a builder from a valid event to make it mutable
        BjjEvent.Builder builder = getValidBjjEvent().toBuilder();

        // Modify the builder to make the event data invalid
        switch (invalidReason) {
            case "missing name":
                builder.name(""); // Set the name to be empty
                break;
            case "negative price":
                BjjEvent tempEventForPrice = builder.build();
                PricingModel invalidPricing = new PricingModel(
                        tempEventForPrice.pricing().type(),
                        new BigDecimal("-10.00"), // Set a negative price
                        tempEventForPrice.pricing().durationDays(),
                        tempEventForPrice.pricing().currency()
                );
                builder.pricing(invalidPricing);
                break;
            case "invalid date":
                BjjEvent tempEventForDate = builder.build();
                BjjEventSchedule invalidSchedule = new BjjEventSchedule(
                        tempEventForDate.schedule().scheduleType(),
                        LocalDate.now().minusDays(1), // Set start date to the past
                        tempEventForDate.schedule().endDate(),
                        tempEventForDate.schedule().hours()
                );
                builder.schedule(invalidSchedule);
                break;
            default:
                throw new IllegalArgumentException(
                        "Unsupported invalid reason for test data generation: " + invalidReason);
        }

        // Build the invalid event and wrap it in the command object
        return new CreateBjjEventCommand(builder.build());
    }
}