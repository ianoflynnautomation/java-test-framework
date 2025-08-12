package solutions.bjjeire.core.data.gyms;

import java.time.LocalDateTime;
import java.util.List;

import solutions.bjjeire.core.data.common.County;
import solutions.bjjeire.core.data.common.Location;
import solutions.bjjeire.core.data.common.SocialMedia;

public record Gym(
        String id,
        LocalDateTime createdOnUtc,
        LocalDateTime updatedOnUtc,
        String name,
        String description,
        GymStatus status,
        County county,
        Affiliation affiliation,
        TrialOffer trialOffer,
        Location location,
        SocialMedia socialMedia,
        List<ClassCategory> offeredClasses,
        String website,
        String timetableUrl,
        String imageUrl) {

    public Builder toBuilder() {
        return new Builder()
                .id(this.id)
                .createdOnUtc(this.createdOnUtc)
                .updatedOnUtc(this.updatedOnUtc)
                .name(this.name)
                .description(this.description)
                .status(this.status)
                .county(this.county)
                .affiliation(this.affiliation)
                .trialOffer(this.trialOffer)
                .location(this.location)
                .socialMedia(this.socialMedia)
                .offeredClasses(this.offeredClasses)
                .website(this.website)
                .timetableUrl(this.timetableUrl)
                .imageUrl(this.imageUrl);
    }

    public static class Builder {
        private String id;
        private LocalDateTime createdOnUtc;
        private LocalDateTime updatedOnUtc;
        private String name;
        private String description;
        private GymStatus status;
        private County county;
        private Affiliation affiliation;
        private TrialOffer trialOffer;
        private Location location;
        private SocialMedia socialMedia;
        private List<ClassCategory> offeredClasses;
        private String website;
        private String timetableUrl;
        private String imageUrl;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder createdOnUtc(LocalDateTime createdOnUtc) {
            this.createdOnUtc = createdOnUtc;
            return this;
        }

        public Builder updatedOnUtc(LocalDateTime updatedOnUtc) {
            this.updatedOnUtc = updatedOnUtc;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder status(GymStatus status) {
            this.status = status;
            return this;
        }

        public Builder county(County county) {
            this.county = county;
            return this;
        }

        public Builder affiliation(Affiliation affiliation) {
            this.affiliation = affiliation;
            return this;
        }

        public Builder trialOffer(TrialOffer trialOffer) {
            this.trialOffer = trialOffer;
            return this;
        }

        public Builder location(Location location) {
            this.location = location;
            return this;
        }

        public Builder socialMedia(SocialMedia socialMedia) {
            this.socialMedia = socialMedia;
            return this;
        }

        public Builder offeredClasses(List<ClassCategory> offeredClasses) {
            this.offeredClasses = offeredClasses;
            return this;
        }

        public Builder website(String website) {
            this.website = website;
            return this;
        }

        public Builder timetableUrl(String timetableUrl) {
            this.timetableUrl = timetableUrl;
            return this;
        }

        public Builder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Gym build() {
            return new Gym(id, createdOnUtc, updatedOnUtc, name, description, status, county, affiliation, trialOffer,
                    location, socialMedia, offeredClasses, website, timetableUrl, imageUrl);
        }
    }
}