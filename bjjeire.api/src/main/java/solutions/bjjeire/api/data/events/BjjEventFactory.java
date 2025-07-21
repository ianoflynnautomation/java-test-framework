package solutions.bjjeire.api.data.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.javafaker.Faker;
import org.bson.types.ObjectId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


public class BjjEventFactory {

    private static final Faker faker = new Faker();
    // Register the JavaTimeModule and configure enum serialization
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(SerializationFeature.WRITE_ENUMS_USING_INDEX, true)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .registerModule(new JavaTimeModule());

    public static CreateBjjEventCommand getValidBjjEventCommand() {
        return new CreateBjjEventCommand(getValidBjjEvent());
    }

    public static BjjEvent createBjjEvent() {
        return createBjjEvent(null);
    }

    public static BjjEvent getValidBjjEvent() {
        return new BjjEvent(
                new ObjectId().toString(),
                LocalDateTime.now(), // Changed from Instant.now()
                LocalDateTime.now(),
                "Dublin BJJ Masterclass Series",
                "Weekly BJJ seminars with Professor " + faker.name().fullName() + " at Dublin Grappling Hub.",
                BjjEventType.Seminar,
                new Organizer(
                        "Dublin Grappling Hub",
                        "https://www.dublingrappling.com"
                ),
                EventStatus.Upcoming,
                "Event is coming soon",
                new SocialMedia(
                        "https://www.instagram.com/dublingrappling",
                        "https://www.facebook.com/dublingrappling",
                        "https://x.com/dublingrappling",
                        "https://www.youtube.com/@dublingrappling"
                ),
                County.Dublin,
                new Location(
                        "45 O'Connell Street, Dublin 1, Ireland",
                        "Dublin Grappling Hub",
                        new GeoCoordinates(
                                "Point",
                                -6.260273,
                                53.349805,
                                "Dublin test",
                                faker.random().hex(20)
                        )
                ),
                new BjjEventSchedule(
                        ScheduleType.FixedDate,
                        LocalDate.now().plusDays(14),
                        LocalDate.now().plusDays(14),
                        List.of(new DailySchedule(
//                                DayOfWeek.WEDNESDAY,
                                "Wednesday",
                                LocalTime.of(9, 0),
                                LocalTime.of(13, 0)
                        ))
                ),
                new PricingModel(
                        PricingType.FlatRate,
                        new BigDecimal("45.00"),
                        1,
                        "EUR"
                ),
                "https://www.dublingrappling.com/events",
                "https://www.dublingrappling.com/images/event_poster.jpg"
        );
    }

    public static BjjEvent createBjjEvent(Consumer<BjjEvent.Builder> configure) {
        var builder = new BjjEvent.Builder()
                .id(new ObjectId().toString())
                .createdOnUtc(LocalDateTime.now()) // Changed from Instant.now()
                .updatedOnUtc(LocalDateTime.now())
                .name("Dublin BJJ Masterclass Series")
                .description("Weekly BJJ seminars with Professor " + faker.name().fullName() + " at " + faker.company().name() + ".")
                .type(BjjEventType.Seminar)
                .organiser(new Organizer(
                        faker.company().name(),
                        faker.internet().url()
                ))
                .status(EventStatus.Upcoming)
                .statusReason("Event is coming soon")
                .socialMedia(new SocialMedia(
                        "https://www.instagram.com/" + faker.lorem().word(),
                        "https://www.facebook.com/" + faker.lorem().word(),
                        "https://x.com/" + faker.lorem().word(),
                        "https://www.youtube.com/@" + faker.lorem().word()
                ))
                .county(County.Dublin)
                .location(new Location(
                        faker.address().fullAddress(),
                        faker.company().name() + " Venue",
                        new GeoCoordinates(
                                "Point",
                                Double.parseDouble(faker.address().latitude()),
                                Double.parseDouble(faker.address().longitude()),
                                faker.address().streetName(),
                                "ChIJ" + faker.random().hex(20)
                        )
                ))
                .schedule(new BjjEventSchedule(
                        ScheduleType.FixedDate,
                        LocalDate.now().plusDays(14),
                        LocalDate.now().plusDays(14),
                        List.of(new DailySchedule(
//                          ß
                                "Wednesday",
                                LocalTime.of(9, 0),
                                LocalTime.of(13, 0)
                        ))
                ))
                .pricing(new PricingModel(
                        PricingType.FlatRate,
                        new BigDecimal("45.00"),
                        1,
                        "EUR"
                ))
                .eventUrl(faker.internet().url())
                .imageUrl(faker.internet().avatar());

        if (configure != null) {
            configure.accept(builder);
        }

        return builder.build();
    }

    /**
     * Creates an invalid event payload based on a reason string.
     * This is used for negative testing scenarios.
     * @param invalidReason A string describing why the payload is invalid.
     * @return A Map representing the invalid JSON payload.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> createInvalidEvent(String invalidReason) {
        // Start with a valid object and then break it
        BjjEvent validEvent = getValidBjjEvent();
        Map<String, Object> payload = objectMapper.convertValue(validEvent, Map.class);

        switch (invalidReason) {
            case "a missing name":
                payload.remove("name");
                break;
            case "a negative price":
                // The structure is nested, so we need to navigate it.
                if (payload.get("pricing") instanceof Map) {
                    ((Map<String, Object>) payload.get("pricing")).put("Amount", -10.00);
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported invalid reason for test data generation: " + invalidReason);
        }
        return payload;
    }
}

