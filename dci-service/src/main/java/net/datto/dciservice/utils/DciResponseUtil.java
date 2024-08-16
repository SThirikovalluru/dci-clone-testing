package net.datto.dciservice.utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

public class DciResponseUtil {
    private static final Logger logger = LoggerFactory.getLogger(DciResponseUtil.class);

    public static void handleHttpExceptionError(HttpStatusCodeException statusException) throws NotFoundException, UnauthorizedException, BadRequestException {
        if (statusException instanceof HttpClientErrorException.Unauthorized) {
            throw new UnauthorizedException(((HttpClientErrorException.Unauthorized)statusException).getMessage());
        } else if (statusException instanceof HttpClientErrorException.Forbidden || statusException instanceof HttpClientErrorException.BadRequest) {
            throw new BadRequestException(((HttpClientErrorException)statusException).getMessage());
        }

        String responseBody = statusException.getResponseBodyAsString();
        try {
            JSONObject jsonObj = new JSONObject(responseBody);
            if ("security.authorization_invalid".equalsIgnoreCase(jsonObj.getString("code"))) {
                throw new UnauthorizedException(jsonObj.getString("message"));
            } else {
                throw new BadRequestException("Code: " + jsonObj.getString("code") +", Message: " + jsonObj.getString("message"));
            }

        } catch (JSONException e) {
            logger.error("Cannot get 'message' from ResponseBody: {}", responseBody, statusException);
            throw new NotFoundException("Cannot get 'message' from ResponseBody: " + responseBody);
        }
    }
}
