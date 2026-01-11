package controller.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import exception.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter("/*")
public class ExceptionHandlerFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (BadRequestException e) {
            writeErrorResponse((HttpServletResponse) servletResponse, HttpServletResponse.SC_BAD_REQUEST, e);
        } catch (NotFoundException e) {
            writeErrorResponse((HttpServletResponse) servletResponse, HttpServletResponse.SC_NOT_FOUND, e);
        } catch (AlreadyExistException e) {
            writeErrorResponse((HttpServletResponse) servletResponse, HttpServletResponse.SC_CONFLICT, e);
        } catch (Exception e) {
            writeErrorResponse((HttpServletResponse) servletResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
    }

    private void writeErrorResponse(HttpServletResponse res, int statusError, Exception e) throws IOException {
        res.setStatus(statusError);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(res.getWriter(), e);
    }
}
