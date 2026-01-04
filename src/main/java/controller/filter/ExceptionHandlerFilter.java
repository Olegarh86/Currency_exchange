package controller.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import exception.AlreadyExistException;
import exception.BadRequestException;
import exception.CurrencyExchangeException;
import exception.NotFoundException;
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
            writeErrorResponse((HttpServletResponse) servletResponse, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (NotFoundException e) {
            writeErrorResponse((HttpServletResponse) servletResponse, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (AlreadyExistException e) {
            writeErrorResponse((HttpServletResponse) servletResponse, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (Exception e) {
            writeErrorResponse((HttpServletResponse) servletResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void writeErrorResponse(HttpServletResponse res, int statusError, String e) throws IOException {
        res.setStatus(statusError);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(res.getWriter(), new CurrencyExchangeException("").getMessage() + e);
    }
}
