package solutions.bjjeire.selenium.web.services;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import solutions.bjjeire.selenium.web.infrastructure.DriverService;

import java.util.Set;

@Service
public class CookiesService extends WebService {

    @Autowired
    public CookiesService(DriverService driverService) {
        super(driverService);
    }

    public void addCookie(String cookieName, String cookieValue, String path) {
        getWrappedDriver().manage().addCookie(new Cookie(cookieName, cookieValue, path));
    }

    public void addCookie(String cookieName, String cookieValue) {
        addCookie(cookieName, cookieValue, "/");
    }

    public void addCookie(Cookie cookieToAdd) {
        getWrappedDriver().manage().addCookie(cookieToAdd);
    }

    public void deleteAllCookies() {
        getWrappedDriver().manage().deleteAllCookies();
    }

    public void deleteCookie(String cookieName) {
        getWrappedDriver().manage().deleteCookieNamed(cookieName);
    }

    public Set<Cookie> getAllCookies() {
        return getWrappedDriver().manage().getCookies();
    }

    public Cookie getCookie(String cookieName) {
        return getWrappedDriver().manage().getCookieNamed(cookieName);
    }
}