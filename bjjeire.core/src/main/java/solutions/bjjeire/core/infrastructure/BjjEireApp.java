package solutions.bjjeire.core.infrastructure;

import solutions.bjjeire.core.utilities.SingletonFactory;

public interface BjjEireApp extends AutoCloseable{

    void addDriverOptions(String key, String value);

    default <TPage extends PageObjectModel> TPage createPage(Class<TPage> pageOf, Object... args) {
        return SingletonFactory.getInstance(pageOf, args);
    }

    default <TSection extends PageObjectModel> TSection createSection(Class<TSection> pageOf, Object... args) {
        return SingletonFactory.getInstance(pageOf, args);
    }
}
