package controller.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class CorsFilter implements Filter {
    private static final String ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    private static final String ORIGIN = "http://exchanger-app.ru";
    private static final String ALLOW_METHODS = "Access-Control-Allow-Methods";
    private static final String METHODS = "POST, GET, PATCH, OPTIONS";
    private static final String ALLOW_HEADERS = "Access-Control-Allow-Headers";
    private static final String OPTIONS = "OPTIONS";
    private static final String HEADERS = """
                                                     accept, accept-encoding, accept-language, connection, dnt,
                                                     host, origin, referer, user-agent, x-request-id, content-type,
                                                     authorization""";
    private static final String CONTROL_MAX_AGE = "Access-Control-Max-Age";
    private static final String MAX_AGE = "2592000";
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.setHeader(ALLOW_ORIGIN, ORIGIN);
        response.setHeader(ALLOW_METHODS, METHODS);
        response.setHeader(ALLOW_HEADERS, HEADERS);
        response.setHeader(CONTROL_MAX_AGE, MAX_AGE);
        if (OPTIONS.equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
