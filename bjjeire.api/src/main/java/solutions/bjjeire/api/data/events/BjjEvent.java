package solutions.bjjeire.api.data.events;

import solutions.bjjeire.api.data.common.County;
import solutions.bjjeire.api.data.common.Location;
import solutions.bjjeire.api.data.common.SocialMedia;

import java.time.LocalDateTime;

public record BjjEvent(
        String id,
        LocalDateTime createdOnUtc,
        LocalDateTime updatedOnUtc,
        String name,
        String description,
        BjjEventType type,
        Organizer organiser,
        EventStatus status,
        String statusReason,
        SocialMedia socialMedia,
        County county,
        Location location,
        BjjEventSchedule schedule,
        PricingModel pricing,
        String eventUrl,
        String imageUrl
) {
    // Custom builder to allow for optional configuration
    public static class Builder {
        private String id;
        private LocalDateTime createdOnUtc; // Changed from Instant
        private LocalDateTime updatedOnUtc;
        private String name;
        private String description;
        private BjjEventType type;
        private Organizer organiser;
        private EventStatus status;
        private String statusReason;
        private SocialMedia socialMedia;
        private County county;
        private Location location;
        private BjjEventSchedule schedule;
        private PricingModel pricing;
        private String eventUrl;
        private String imageUrl;

        public Builder id(String id) { this.id = id; return this; }
        public Builder createdOnUtc(LocalDateTime createdOnUtc) { this.createdOnUtc = createdOnUtc; return this; }
        public Builder updatedOnUtc(LocalDateTime updatedOnUtc) { this.updatedOnUtc = updatedOnUtc; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder type(BjjEventType type) { this.type = type; return this; }
        public Builder organiser(Organizer organiser) { this.organiser = organiser; return this; }
        public Builder status(EventStatus status) { this.status = status; return this; }
        public Builder statusReason(String statusReason) { this.statusReason = statusReason; return this; }
        public Builder socialMedia(SocialMedia socialMedia) { this.socialMedia = socialMedia; return this; }
        public Builder county(County county) { this.county = county; return this; }
        public Builder location(Location location) { this.location = location; return this; }
        public Builder schedule(BjjEventSchedule schedule) { this.schedule = schedule; return this; }
        public Builder pricing(PricingModel pricing) { this.pricing = pricing; return this; }
        public Builder eventUrl(String eventUrl) { this.eventUrl = eventUrl; return this; }
        public Builder imageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }

        public BjjEvent build() {
            return new BjjEvent(id, createdOnUtc, updatedOnUtc, name, description, type, organiser, status, statusReason, socialMedia,
                    county, location, schedule, pricing, eventUrl, imageUrl);
        }
    }
}
