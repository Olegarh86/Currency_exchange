package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dao.CurrencyDao;
import dao.ExchangeRateDao;
import dto.ExchangeRateResponseDto;
import dto.CurrenciesResponseDto;
import dto.ExchangeRateRequestDto;
import exception.AlreadyExistException;
import exception.DaoException;
import exception.NotFoundException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;

import static dao.util.Validator.validateExchangeRates;

@WebServlet(value = "/exchangeRates", name = "ExchangeRatesServlet")
public class ExchangeRatesServlet extends HttpServlet {
    private static final String BASE_CODE_PARAMETER = "baseCurrencyCode";
    private static final String TARGET_CODE_PARAMETER = "targetCurrencyCode";
    private static final String RATE_PARAMETER = "rate";
    private CurrencyDao instanceCurrency;
    private ExchangeRateDao instanceExchangeRate;
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    public void init() {
        ServletContext servletContext = getServletContext();
        this.instanceCurrency = (CurrencyDao) servletContext.getAttribute("instanceCurrency");
        this.instanceExchangeRate = (ExchangeRateDao) servletContext.getAttribute("instanceExchangeRate");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<ExchangeRateResponseDto> exchangeRateResponseDto;
        try {
            exchangeRateResponseDto = instanceExchangeRate.findAll();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        PrintWriter out = response.getWriter();
        response.setStatus(HttpServletResponse.SC_OK);
        mapper.writeValue(out, exchangeRateResponseDto);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String baseCode = request.getParameter(BASE_CODE_PARAMETER).toUpperCase();
        String targetCode = request.getParameter(TARGET_CODE_PARAMETER).toUpperCase();
        BigDecimal rate = new BigDecimal(request.getParameter(RATE_PARAMETER));

        CurrenciesResponseDto baseCurrency;
        CurrenciesResponseDto targetCurrency;
        try {
            baseCurrency = instanceCurrency.findCurrencyByCode(baseCode);
            targetCurrency = instanceCurrency.findCurrencyByCode(targetCode);
        } catch (DaoException e) {
            throw new NotFoundException(e.getMessage());
        }
        validateExchangeRates(instanceExchangeRate, baseCode, targetCode, rate);
        ExchangeRateResponseDto responseDto;

        try {
            instanceExchangeRate.save(new ExchangeRateRequestDto(baseCurrency, targetCurrency, rate));
            responseDto = instanceExchangeRate.findRateByCodes(baseCode, targetCode);
        } catch (DaoException e) {
            throw new AlreadyExistException(e.getMessage());
        }
        PrintWriter out = response.getWriter();
        response.setStatus(HttpServletResponse.SC_CREATED);
        mapper.writeValue(out, responseDto);
    }
}

