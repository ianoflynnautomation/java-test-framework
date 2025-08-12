package solutions.bjjeire.core.data.gyms;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.bson.types.ObjectId;

import com.github.javafaker.Faker;

import solutions.bjjeire.core.data.common.County;
import solutions.bjjeire.core.data.common.GeoCoordinates;
import solutions.bjjeire.core.data.common.Location;
import solutions.bjjeire.core.data.common.SocialMedia;

public class GymFactory {

    private static final Faker faker = new Faker();

    public static CreateGymCommand getValidCreateGymCommand() {
        return new CreateGymCommand(getValidGym());

    }

    public static Gym getValidGym() {
        return createGym(null);
    }

    public static Gym createGym(Consumer<Gym.Builder> configure) {

        String uniqueEventName = faker.esports().event() + " " + UUID.randomUUID().toString().substring(0, 8);

        var builder = new Gym.Builder()
                .id(new ObjectId().toString())
                .createdOnUtc(LocalDateTime.now(ZoneOffset.UTC))
                .updatedOnUtc(LocalDateTime.now(ZoneOffset.UTC))
                .name(uniqueEventName)
                .description("Valid gym description, not too long.")
                .status(GymStatus.ACTIVE)
                .county(County.Dublin)
                .affiliation(new Affiliation("Valid Affiliation Name", "https://www.validaffiliation.com"))
                .trialOffer(new TrialOffer(true, 3, null, "Valid trial notes."))
                .location(new Location(
                        "123 Valid Street, Valid Town",
                        "Valid Venue Hall",
                        new GeoCoordinates(
                                "Point", -6.260273, 53.349805, "Dublin test", faker.random().hex(20))))
                .socialMedia(new SocialMedia(
                        "https://www.instagram.com/validgymprofile",
                        "https://www.facebook.com/validgympage",
                        "https://www.x.com/validgymhandle",
                        "https://www.youtube.com/c/validgymchannel"))
                .website("https://www.validgymsite.com")
                .timetableUrl("https://www.validgymsite.com/schedule")
                .imageUrl("https://www.validgymsite.com/images/main_logo.png")
                .offeredClasses(List.of(ClassCategory.BJJGiAllLevels, ClassCategory.BJJGiFundamentals));

        if (configure != null) {
            configure.accept(builder);
        }

        return builder.build();
    }

    public static CreateGymCommand createInvalidGym(String invalidReason) {
     
        Gym.Builder builder = getValidGym().toBuilder();


        switch (invalidReason) {
            case "missing name" -> builder.name(null);
            case "long description" -> builder.description(faker.lorem().characters(1001));
            case "invalid website" -> builder.website("not-a-valid-url");
            default -> throw new IllegalArgumentException(
                        "Unsupported invalid reason for test data generation: " + invalidReason);
        }


        return new CreateGymCommand(builder.build());
    }

}