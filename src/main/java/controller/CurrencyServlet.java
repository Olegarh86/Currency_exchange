package controller;

import java.io.IOException;
import java.io.PrintWriter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dao.CurrencyDao;
import dto.CurrencyDto;
import dto.CurrencyRequestDto;
import exception.DaoException;
import exception.NotFoundException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import static util.Validator.validateCode;

@Slf4j
public class CurrencyServlet extends HttpServlet {
    private static final String INSTANCE_CURRENCY = "instanceCurrency";
    private static final char SEPARATOR = '/';
    private CurrencyDao instanceCurrency;
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    public void init() {
        ServletContext servletContext = getServletContext();
        this.instanceCurrency = (CurrencyDao) servletContext.getAttribute(INSTANCE_CURRENCY);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String code = getCode(request);

        validateCode(code);
        CurrencyDto currencyRequestDto = new CurrencyRequestDto(code);
        CurrencyDto result;
        try {
            result = instanceCurrency.findCurrencyByCode(currencyRequestDto);
        } catch (DaoException e) {
            throw new NotFoundException(e.getMessage());
        }
        PrintWriter out = response.getWriter();
        response.setStatus(HttpServletResponse.SC_OK);
        mapper.writeValue(out, result);
    }

    private static String getCode(HttpServletRequest request) {
        String code = request.getRequestURI();
        code = code.substring(code.lastIndexOf(SEPARATOR) + 1).toUpperCase();
        return code;
    }
}
