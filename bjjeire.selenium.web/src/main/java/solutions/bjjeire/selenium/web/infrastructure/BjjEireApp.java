package solutions.bjjeire.selenium.web.infrastructure;

import solutions.bjjeire.selenium.web.services.*;
import solutions.bjjeire.selenium.web.services.*;

public interface BjjEireApp extends AutoCloseable {

    NavigationService navigate();
    BrowserService browser();
    CookiesService cookies();
    JavaScriptService script();
    ComponentCreateService create();
    ComponentWaitService waitFor();

    /**
     * Creates a new instance of a page object from the Spring context.
     * @param pageClass The class of the page to create.
     * @param args Arguments to pass to the page's constructor if needed.
     * @param <TPage> The type of the page object.
     * @return A fully initialized page object instance.
     */
    <TPage extends PageObjectModel> TPage createPage(Class<TPage> pageClass, Object... args);

    /**
     * Creates a new instance of a page section/component from the Spring context.
     * @param sectionClass The class of the section to create.
     * @param args Arguments to pass to the section's constructor if needed.
     * @param <TSection> The type of the page section.
     * @return A fully initialized page section instance.
     */
    <TSection extends PageObjectModel> TSection createSection(Class<TSection> sectionClass, Object... args);

    @Override
    void close();
}