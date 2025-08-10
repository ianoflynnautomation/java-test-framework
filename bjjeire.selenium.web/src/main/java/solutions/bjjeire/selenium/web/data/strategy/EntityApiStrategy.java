package solutions.bjjeire.selenium.web.data.strategy;

public interface EntityApiStrategy<T_ENTITY, T_COMMAND, T_RESPONSE> {

    Class<T_ENTITY> getEntityType();

    String getApiPath();

    String getEntityName(T_ENTITY entity);

    T_COMMAND createCommand(T_ENTITY entity);

    Class<T_RESPONSE> getResponseClass();

    String getIdFromResponse(T_RESPONSE response);
}