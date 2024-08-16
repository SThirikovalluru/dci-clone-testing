package net.datto.dciservice.log;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.datto.dciservice.utils.LoggingMDCKeys;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;


@Component
public class LoggingRequestInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {

        final Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        if (null != pathVariables) {
            if (pathVariables.containsKey(LoggingMDCKeys.ACCOUNT_UID)) {
                final String accountUid = (String) pathVariables.get(LoggingMDCKeys.ACCOUNT_UID);
                MDC.put(LoggingMDCKeys.ACCOUNT_UID, accountUid);
            }

            if (pathVariables.containsKey(LoggingMDCKeys.DEVICE_UID)) {
                final String deviceUid = (String) pathVariables.get(LoggingMDCKeys.DEVICE_UID);
                MDC.put(LoggingMDCKeys.DEVICE_UID, deviceUid);
            }
        }

        String xRequestId = request.getHeader("x-request-id");
        MDC.put(LoggingMDCKeys.REQUEST_ID, xRequestId);
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) {

        MDC.remove(LoggingMDCKeys.ACCOUNT_UID);
        MDC.remove(LoggingMDCKeys.DEVICE_UID);
        MDC.remove(LoggingMDCKeys.REQUEST_ID);
    }
}