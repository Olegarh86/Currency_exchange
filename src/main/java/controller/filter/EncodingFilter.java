package controller.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class EncodingFilter extends HttpFilter {
    private static final String TYPE = "application/json;charset=UTF-8";

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        response.setContentType(TYPE);
        filterChain.doFilter(request, response);
    }
}
