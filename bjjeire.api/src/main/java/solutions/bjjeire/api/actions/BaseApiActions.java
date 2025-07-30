package solutions.bjjeire.api.actions;

import org.springframework.beans.factory.annotation.Autowired;
import solutions.bjjeire.api.http.ApiTestRunner;
import solutions.bjjeire.api.http.ApiRequest;

public abstract class BaseApiActions {

    @Autowired
    protected ApiTestRunner runner;

    protected ApiRequest.Builder request() {
        return ApiRequest.builder();
    }
}