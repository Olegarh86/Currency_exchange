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
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;
        long start = System.currentTimeMillis();
        long time = 0;
        try {
            String requestId = req.getHeader("X-Request-ID");

            if (requestId == null) {
                requestId = UUID.randomUUID().toString();
            }
            MDC.put("requestId", requestId);
            MDC.put("ip", req.getRemoteAddr());
            MDC.put("uri", req.getRequestURI());
            MDC.put("method", req.getMethod());
            res.setHeader("X-Request-ID", requestId);
            filterChain.doFilter(servletRequest, servletResponse);
            long finish = System.currentTimeMillis();
            time = finish - start;

            if (time > 1000) {
                log.warn("slow request {}", requestId);
            }
        } finally {
            log.info("status: {} -> ({} ms)", res.getStatus(),  time);
            MDC.clear();
        }

    }
}
