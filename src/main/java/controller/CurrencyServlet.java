package controller;

import java.io.IOException;
import java.io.PrintWriter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dao.CurrencyDao;
import dto.CurrenciesResponseDto;
import exception.DaoException;
import exception.NotFoundException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static dao.util.Validator.validateCode;

@WebServlet(value = "/currency/*", name = "CurrencyServlet")
public class CurrencyServlet extends HttpServlet {
    private final CurrencyDao instanceCurrency = CurrencyDao.getInstance();
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String code = getCode(request);
        validateCode(code);

        CurrenciesResponseDto result;
        try {
            result = instanceCurrency.findCurrencyByCode(code);
        } catch (DaoException e) {
            throw new NotFoundException(e.getMessage());
        }
        PrintWriter out = response.getWriter();
        response.setStatus(HttpServletResponse.SC_OK);
        mapper.writeValue(out, result);
    }

    private static String getCode(HttpServletRequest request) {
        String code = request.getRequestURI();
        code = code.substring(code.lastIndexOf('/') + 1).toUpperCase();
        return code;
    }
}
