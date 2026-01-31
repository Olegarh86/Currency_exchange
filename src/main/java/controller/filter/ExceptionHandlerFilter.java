package controller.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.ExceptionDto;
import exception.*;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class ExceptionHandlerFilter extends HttpFilter {

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        try {
            filterChain.doFilter(request, response);
        } catch (BadRequestException e) {
            writeErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e);
        } catch (NotFoundException e) {
            writeErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, e);
        } catch (AlreadyExistException e) {
            writeErrorResponse(response, HttpServletResponse.SC_CONFLICT, e);
        } catch (Exception e) {
            writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
    }

    private void writeErrorResponse(HttpServletResponse res, int statusError, Exception e) throws IOException {
        log.error(e.getMessage());
        res.setStatus(statusError);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(res.getWriter(), new ExceptionDto(e.getMessage()));
    }
}
