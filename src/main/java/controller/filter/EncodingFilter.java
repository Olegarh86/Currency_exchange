package controller.filter;

import jakarta.servlet.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class EncodingFilter implements Filter {
    private static final String TYPE = "application/json;charset=UTF-8";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        servletResponse.setContentType(TYPE);
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
