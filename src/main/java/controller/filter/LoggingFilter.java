package controller.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

@Slf4j
public class LoggingFilter extends HttpFilter {
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String REQUEST_ID_MDC = "requestId";
    private static final String IP = "ip";
    private static final String URI = "uri";
    private static final String METHOD = "method";
    private static final String USER_AGENT = "User-Agent";
    private static final String SLOW_REQUEST = "slow request {}";
    private static final String FORWARDED = "X-Forwarded-For";
    private static final String STATUS_AND_SPEED = "status: {} -> ({} ms)";
    private static final String DELIMITER = ",";
    private static final int MAX_PENDING_MS = 1000;

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String ip = request.getHeader(FORWARDED);
        if (ip != null && !ip.isEmpty()) {
            ip = ip.split(DELIMITER)[0].trim();
        } else {
            ip = request.getRemoteAddr();
        }
        long start = System.currentTimeMillis();
        long time = 0;
        try {
            String requestId = request.getHeader(REQUEST_ID_HEADER);

            if (requestId == null) {
                requestId = UUID.randomUUID().toString();
            }
            MDC.put(REQUEST_ID_MDC, requestId);
            MDC.put(IP, ip);
            MDC.put(URI, request.getRequestURI());
            MDC.put(METHOD, request.getMethod());
            MDC.put(USER_AGENT, request.getHeader(USER_AGENT));
            response.setHeader(REQUEST_ID_HEADER, requestId);
            filterChain.doFilter(request, response);
            long finish = System.currentTimeMillis();
            time = finish - start;

            if (time > MAX_PENDING_MS) {
                log.warn(SLOW_REQUEST, requestId);
            }
        } finally {
            log.info(STATUS_AND_SPEED, response.getStatus(), time);
            MDC.clear();
        }
    }
}
