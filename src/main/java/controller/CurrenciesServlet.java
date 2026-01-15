package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dao.CurrencyDao;
import dto.CurrenciesRequestDto;
import dto.CurrenciesResponseDto;
import exception.AlreadyExistException;
import exception.DaoException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static dao.util.Validator.validateCurrency;

@WebServlet(value = "/currencies", name = "CurrenciesServlet")
public class CurrenciesServlet extends HttpServlet {
    private static final String NAME_PARAMETER = "name";
    private static final String CODE_PARAMETER = "code";
    private static final String SIGN_PARAMETER = "sign";
    private CurrencyDao instanceCurrency;
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    public void init() {
        ServletContext servletContext = getServletContext();
        this.instanceCurrency = (CurrencyDao) servletContext.getAttribute("instanceCurrency");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<CurrenciesResponseDto> currencies;
        try {
            currencies = instanceCurrency.findAll();
        } catch (DaoException e) {
            throw new RuntimeException(e.getMessage());
        }
        PrintWriter out = response.getWriter();
        response.setStatus(HttpServletResponse.SC_OK);
        mapper.writeValue(out, currencies);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = request.getParameter(NAME_PARAMETER);
        String code = request.getParameter(CODE_PARAMETER).toUpperCase();
        String sign = request.getParameter(SIGN_PARAMETER);
        validateCurrency(code, name, sign);

        CurrenciesResponseDto responseDto;
        try {
            instanceCurrency.save(new CurrenciesRequestDto(name, code, sign));
            responseDto = instanceCurrency.findCurrencyByCode(code);
        } catch (DaoException e) {
            throw new AlreadyExistException(e.getMessage());
        }
        PrintWriter out = response.getWriter();
        response.setStatus(HttpServletResponse.SC_CREATED);
        mapper.writeValue(out, responseDto);
    }
}
