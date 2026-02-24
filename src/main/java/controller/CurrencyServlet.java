package controller;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import dao.JdbcCurrencyDao;
import exception.NotFoundException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import mapper.CurrencyMapper;
import model.Currency;

import static util.Validator.validateCode;

@Slf4j
public class CurrencyServlet extends HttpServlet {
    private static final char SEPARATOR = '/';
    private final JdbcCurrencyDao instanceCurrency = new JdbcCurrencyDao();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getPathInfo();
        String code = path.substring(path.lastIndexOf(SEPARATOR) + 1).toUpperCase();

        validateCode(code);

        Currency result = instanceCurrency.findByCode(code).orElseThrow(() -> new NotFoundException(code));

        response.setStatus(HttpServletResponse.SC_OK);
        mapper.writeValue(response.getWriter(), CurrencyMapper.INSTANCE.currencyToDto(result));
    }
}
