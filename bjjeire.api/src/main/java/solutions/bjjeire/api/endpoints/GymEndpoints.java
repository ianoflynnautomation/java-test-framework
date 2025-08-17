package solutions.bjjeire.api.endpoints;

public final class GymEndpoints {

    public static final String GYMS = "/api/gym";

    public static String gymById(String gymId) {
        return GYMS + "/" + gymId;
    }
}
