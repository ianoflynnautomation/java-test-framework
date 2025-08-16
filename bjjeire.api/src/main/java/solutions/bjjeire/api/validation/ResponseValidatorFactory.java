package solutions.bjjeire.api.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResponseValidatorFactory {

    @Autowired
    public ResponseValidatorFactory() {
    }

    public ResponseValidator validate(ApiResponse apiResponse) {
        return new ResponseValidator(apiResponse);
    }
}