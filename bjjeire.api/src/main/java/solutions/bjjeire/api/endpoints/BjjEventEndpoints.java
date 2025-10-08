package solutions.bjjeire.api.endpoints;

public final class BjjEventEndpoints {

  public static final String BJJ_EVENTS = "/api/bjjevent";

  public static String bjjEventById(String eventId) {
    return BJJ_EVENTS + "/" + eventId;
  }
}
