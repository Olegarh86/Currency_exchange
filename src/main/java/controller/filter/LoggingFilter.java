package controller.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

@Slf4j
public class LoggingFilter implements Filter {
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
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;
        String ip = req.getHeader(FORWARDED);
        if (ip != null && !ip.isEmpty()) {
            ip = ip.split(DELIMITER)[0].trim();
        } else {
            ip = req.getRemoteAddr();
        }
        long start = System.currentTimeMillis();
        long time = 0;
        try {
            String requestId = req.getHeader(REQUEST_ID_HEADER);

            if (requestId == null) {
                requestId = UUID.randomUUID().toString();
            }
            MDC.put(REQUEST_ID_MDC, requestId);
            MDC.put(IP, ip);
            MDC.put(URI, req.getRequestURI());
            MDC.put(METHOD, req.getMethod());
            MDC.put(USER_AGENT, req.getHeader(USER_AGENT));
            res.setHeader(REQUEST_ID_HEADER, requestId);
            filterChain.doFilter(servletRequest, servletResponse);
            long finish = System.currentTimeMillis();
            time = finish - start;

            if (time > MAX_PENDING_MS) {
                log.warn(SLOW_REQUEST, requestId);
            }
        } finally {
            log.info(STATUS_AND_SPEED, res.getStatus(), time);
            MDC.clear();
        }
    }
}
