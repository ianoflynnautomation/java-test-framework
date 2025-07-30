package solutions.bjjeire.api.actions;

import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.api.http.ApiTestRunner;
import solutions.bjjeire.api.http.RequestSpecification;

public abstract class BaseApiActions {

    @Autowired
    protected ApiTestRunner runner;

    protected RequestSpecification given() {
        return RequestSpecification.given();
    }
}